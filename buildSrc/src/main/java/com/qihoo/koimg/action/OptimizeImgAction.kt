package com.qihoo.koimg.action

import com.qihoo.koimg.RESOURCES_NAME
import com.qihoo.koimg.config.OptimizeImgConfig
import com.qihoo.koimg.config.OptimizeImgConfig.Companion.OPTIMIZE_COMPRESS_PICTURE
import com.qihoo.koimg.config.OptimizeImgConfig.Companion.OPTIMIZE_WEBP_CONVERT
import com.qihoo.koimg.extension.setString
import com.qihoo.koimg.utils.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.apache.tools.ant.taskdefs.Execute.launch
import pink.madis.apk.arsc.ResourceFile
import pink.madis.apk.arsc.ResourceTableChunk
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.FileWriter
import java.util.concurrent.CopyOnWriteArrayList
import javax.annotation.meta.When

object OptimizeImgAction {
    /**
    优化图片
     */
    suspend fun CoroutineScope.optimizeImg(
        mappingFile: File,
        config: OptimizeImgConfig,
        unZipDir: String,
        webpsLsit: CopyOnWriteArrayList<WebpFileData>
    ) {
        compressionImg(mappingFile, unZipDir, config, webpsLsit)

        if (webpsLsit.size > 0) {
            modifyResources(webpsLsit, unZipDir)
        }
    }

    /**
     * 如果是webp 修改的图片 就要修改 resources
     */
    private fun modifyResources(
        webpOkList: CopyOnWriteArrayList<WebpFileData>,
        unZipDir: String
    ) {
        // 开始修改 webp 的 resources 路径
        val resourcesFile = File(unZipDir, RESOURCES_NAME)

        val newResouce = FileInputStream(resourcesFile).use {
            val resouce = ResourceFile.fromInputStream(it)
            // 变量修改
            webpOkList.forEach { webpFile ->
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
                        // 注意当前的 original 和 webpFile 路径是当前电脑的绝对路径 需要转化成res/开头的路径
                        val originalPath = webpFile.original.absolutePath.replace("${unZipDir}${File.separator}", "")
                        val index = stringPoolChunk.indexOf(originalPath)

                        if (index != -1) {
                            // 进行剔除重复资源
                            val webpPath = webpFile.webpFile.absolutePath.replace("${unZipDir}${File.separator}", "")
                            stringPoolChunk.setString(index, webpPath)
                        }
                    }
            }
            resouce
        }

        // 修改完成
        resourcesFile.delete()
        FileOutputStream(resourcesFile).use {
            it.write(newResouce.toByteArray())
        }

    }

    /**
     * 压缩图片
     */
    private suspend fun CoroutineScope.compressionImg(
        mappingFile: File,
        unZipDir: String,
        config: OptimizeImgConfig,
        webpsLsit: CopyOnWriteArrayList<WebpFileData>
    ) {
        val mappginWriter = FileWriter(mappingFile)
        launch {
            // 查找所有的图片
            val file = File("$unZipDir${File.separator}res")
            file
                .listFiles()
                .filter {
                    it.isDirectory && (it.name.startsWith("drawable") || it.name.startsWith("mipmap"))
                }
                .flatMap {
                    it.listFiles().toList()
                }
                .asSequence()
                .filter {
                    config.ignoreFileName?.contains(it.name)?.let { !it } ?: true
                }
                .filter {
                    ImageUtil.isImage(it)
                }
                .forEach {
                    // 进行图片压缩
                    launch(Dispatchers.Default) {
//                        if (config.checkPixels && ImageUtil.isBigSizeImage(it, config.maxSize)) {
//                            // 检测是否是大图
//                        }
                        when (config.optimizeType) {

                            OPTIMIZE_COMPRESS_PICTURE -> {
                                val originalPath = it.absolutePath.replace("${unZipDir}${File.separator}", "")
                                val reduceSize = CompressUtil.compressImg(it)
                                if (reduceSize > 0) {
                                    mappginWriter.synchronizedWriteString("$originalPath => 减少[$reduceSize]")
                                } else {
                                    mappginWriter.synchronizedWriteString("$originalPath => 压缩失败")
                                }
                            }
                            OPTIMIZE_WEBP_CONVERT -> {
                                val webp0K = WebpUtils.securityFormatWebp(it, config)
                                // 加入可以的 webbp 路径
                                webp0K?.apply {
                                    val originalPath = original.absolutePath.replace("${unZipDir}${File.separator}", "")
                                    val webpFilePath = webpFile.absolutePath.replace("${unZipDir}${File.separator}", "")
                                    mappginWriter.synchronizedWriteString("$originalPath => $webpFilePath => 减少[$reduceSize]")
                                    webpsLsit.add(this)
                                }
                            }
                            else -> {
                                println("图片优化类型 optimizeType [${config.optimizeType}] 不存在,使用 ${OPTIMIZE_COMPRESS_PICTURE} 类型压缩图片!")
                            }
                        }
                    }
                }


        }.join()
        mappginWriter.close()
    }


    fun initTools() {
        if (Tools.isLinux()) {
            Tools.chmod()
        }
    }

}