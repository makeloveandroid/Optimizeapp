package com.qihoo.koimg.utils

import com.qihoo.koimg.Const
import com.qihoo.koimg.config.OptimizeImgConfig
import org.gradle.api.Project
import java.io.File

data class WebpFileData(val original: File, val webpFile: File, val reduceSize: Long)

class WebpUtils {

    companion object {
        const val VERSION_SUPPORT_WEBP = 14 //api>=14设设备支持webp
        const val TAG = "Webp"


        private fun formatWebp(imgFile: File): WebpFileData? {
            if (ImageUtil.isImage(imgFile)) {
                val webpFile = File("${imgFile.path.substring(0, imgFile.path.lastIndexOf("."))}.webp")
                Tools.cmd("cwebp", "${imgFile.path} -o ${webpFile.path} -m 6 -quiet")
                val reduceSize = imgFile.length() - webpFile.length()

                if (reduceSize > 0) {
                    LogUtil.log(TAG, imgFile.path, imgFile.length().toString(), webpFile.length().toString())
                    log("webp 成功${imgFile.name}  ${webpFile.absolutePath}")
                    if (imgFile.exists()) {
                        imgFile.delete()
                    }
                    return WebpFileData(imgFile, webpFile, reduceSize)
                } else {
                    //如果webp的大的话就抛弃
                    if (webpFile.exists()) {
                        webpFile.delete()
                    }
                    log("webp 过大${imgFile.name}  ${webpFile.absolutePath}")
                    LogUtil.log("[${TAG}][${imgFile.name}] do not convert webp because the size become larger!")
                    return null
                }
            }
            return null
        }

        fun securityFormatWebp(imgFile: File, config: OptimizeImgConfig): WebpFileData? {
            if (ImageUtil.isImage(imgFile)) {
                if (config.supportAlphaWebp) {
                    return formatWebp(imgFile)
                } else {
                    if (imgFile.name.endsWith(Const.JPG) || imgFile.name.endsWith(Const.JPEG)) {
                        //jpg
                        return formatWebp(imgFile)
                    } else if (imgFile.name.endsWith(Const.PNG)) {
                        //png
                        if (!ImageUtil.isAlphaPNG(imgFile)) {
                            //不包含透明通道
                            return formatWebp(imgFile)
                        } else {
                            //包含透明通道的png，进行压缩
                            CompressUtil.compressImg(imgFile)
                        }
                    }
                }
            }
            return null
        }

    }

}