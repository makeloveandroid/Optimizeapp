package com.qihoo.koimg.action

import com.qihoo.koimg.RESOURCES_NAME
import com.qihoo.koimg.decoder.ResguardStringBuilder
import com.qihoo.koimg.extension.getStrings
import com.qihoo.koimg.utils.FileOperation
import com.qihoo.koimg.utils.synchronizedWriteString
import pink.madis.apk.arsc.ResourceFile
import pink.madis.apk.arsc.ResourceTableChunk
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.FileWriter
import java.util.*

/**
 * 混淆器
 */
object ProguardAction {
    fun proguard(mappingFile: File, unZipDir: String) {
        // 获取
        val mappingWriter = FileWriter(mappingFile)

        val resguardStringBuilder = ResguardStringBuilder()
        resguardStringBuilder.reset(null)

        val resourcesFile = File(unZipDir, RESOURCES_NAME)
        val newResouce = FileInputStream(resourcesFile).use {
            val resouce = ResourceFile.fromInputStream(it)
            /**
             * 原始的路径
             */
            val mappings = mutableMapOf<String, String>()
            resouce
                .chunks
                .asSequence()
                .filter {
                    it is ResourceTableChunk
                }
                .map {
                    it as ResourceTableChunk
                }
                .forEach { chunk ->
                    val stringPoolChunk = chunk.stringPool
                    // 获取所有的路径
                    val strings = stringPoolChunk.getStrings() ?: return@forEach

                    for (index in 0 until stringPoolChunk.stringCount) {
                        val v = strings[index]
                        if (v.startsWith("res")) {
                            // 判断是否有相同的
                            val newPath = if (mappings[v] == null) {
                                val newPath = createProcessPath(v, resguardStringBuilder)
                                mappings[v] = newPath


                                // 创建路径
                                val parent = File("$unZipDir${File.separator}$newPath").parentFile
                                if (!parent.exists()) {
                                    parent.mkdirs()
                                }
                                // 移动文件
                                val isOk =
                                    File("$unZipDir${File.separator}$v").renameTo(File("$unZipDir${File.separator}$newPath"))
                                if (isOk) {
                                    mappingWriter.synchronizedWriteString("$v => $newPath")
                                }
                                newPath
                            } else {
                                mappings[v]
                            }
                            strings[index] = newPath!!
                        }
                    }

                }

            // 擦屁股操作删除以前的文件
            FileOperation.deleteDir(File("$unZipDir${File.separator}res"))

            resouce
        }
        mappingWriter.close()
        // 资源剔除完毕
        resourcesFile.delete()

        FileOutputStream(resourcesFile).use {
            it.write(newResouce.toByteArray())
        }
    }

    private fun createProcessPath(
        it: String,
        resguardStringBuilder: ResguardStringBuilder
    ): String {
        val file = File(it)
        val nameSuffix = file.name.substring(file.name.lastIndexOf("."))
        return "r/${resguardStringBuilder.replaceString}/${resguardStringBuilder.replaceString}$nameSuffix"
    }

}