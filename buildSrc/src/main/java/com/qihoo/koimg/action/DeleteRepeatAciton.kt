package com.qihoo.koimg.action

import com.qihoo.koimg.RESOURCES_NAME
import com.qihoo.koimg.config.ClearRepeatResourcesConfig
import com.qihoo.koimg.extension.groupsResources
import com.qihoo.koimg.extension.setString
import com.qihoo.koimg.utils.synchronizedWriteString
import pink.madis.apk.arsc.ResourceFile
import pink.madis.apk.arsc.ResourceTableChunk
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.FileWriter
import java.util.zip.ZipFile

object DeleteRepeatAciton {
    fun deleteRepeatResources(mappingFile: File, config: ClearRepeatResourcesConfig, apFile: File, unZipDir: String) {
        val fileWriter = FileWriter(mappingFile)

        // 查询重复资源
        val groupResources = ZipFile(apFile).groupsResources()

        // 获取
        val resourcesFile = File(unZipDir, RESOURCES_NAME)
        val newResouce = FileInputStream(resourcesFile).use {
            val resouce = ResourceFile.fromInputStream(it)
            groupResources
                .asSequence()
                .filter {
                    it.value.size > 1
                }
                .filter {
                    // 过滤
                    val name = File(it.value[0].name).name
                    config.ignoreFileName?.contains(name)?.let { !it } ?: true
                }
                .forEach {
                    // 删除多余资源
                    val zips = it.value

                    // 所有的重复资源都指定到这个第一个文件上
                    val coreResources = zips[0]

                    for (index in 1 until zips.size) {
                        // 重复的资源
                        val repeatZipFile = zips[index]
                        fileWriter.synchronizedWriteString("${repeatZipFile.name} => ${coreResources.name}")

                        // 删除解压的路径的重复文件
                        File(unZipDir, repeatZipFile.name).delete()

                        // 将这些重复的资源都重定向到同一个文件上

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
                                val index = stringPoolChunk.indexOf(repeatZipFile.name)
                                if (index != -1) {
                                    // 进行剔除重复资源
                                    stringPoolChunk.setString(index, coreResources.name)
                                }
                            }
                    }
                }

            fileWriter.close()
            resouce
        }

        // 资源剔除完毕
        resourcesFile.delete()

        FileOutputStream(resourcesFile).use {
            it.write(newResouce.toByteArray())
        }

    }
}