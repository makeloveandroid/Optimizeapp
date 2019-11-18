package com.qihoo.koimg.config

import com.qihoo.koimg.CLEAR_REPEAT_RESOURCES_CONFIG_NAME
import org.gradle.api.Named

open class OptimizeImgConfig : Named, BaseConfig() {
    companion object {
        //webp化
        const val OPTIMIZE_WEBP_CONVERT = "ConvertWebp"
        //压缩图片
        const val OPTIMIZE_COMPRESS_PICTURE = "Compress"
    }


    //是否检查大像素图片
//    var checkPixels = false
//
//    var maxSize = 1 * 1024 * 1024

    var supportAlphaWebp = false

    /**
     * optimizeType "Compress" //优化类型，可选"ConvertWebp"，"Compress"，转换为webp或原图压缩，默认Compress，
     *
     * 使用ConvertWep需要 min sdk >= 18.但是压缩效果更好
     */
    var optimizeType = OPTIMIZE_COMPRESS_PICTURE


    var pass = ""

    override fun getName(): String {
        return CLEAR_REPEAT_RESOURCES_CONFIG_NAME
    }
}
