package com.qihoo.koimg


import com.qihoo.koimg.action.DeleteRepeatAciton
import com.qihoo.koimg.action.OptimizeImgAction.initTools
import com.qihoo.koimg.action.OptimizeImgAction.optimizeImg
import com.qihoo.koimg.action.ProguardAction
import com.qihoo.koimg.config.ClearRepeatResourcesConfig
import com.qihoo.koimg.config.OptimizeImgConfig
import com.qihoo.koimg.config.ProguardResourcesConfig
import com.qihoo.koimg.config.QOptimizeConfig
import com.qihoo.koimg.extension.setString
import com.qihoo.koimg.extension.unZipFiles
import com.qihoo.koimg.extension.zip
import com.qihoo.koimg.utils.FileOperation
import com.qihoo.koimg.utils.log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking
import pink.madis.apk.arsc.ResourceFile
import pink.madis.apk.arsc.ResourceTableChunk
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.concurrent.CopyOnWriteArrayList
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

/**
 * 开始图片进行操作
 */

fun start(
    mappingDir: String,
    apFile: File,
    config: QOptimizeConfig,
    clearRepeatConfig: ClearRepeatResourcesConfig,
    optimizeImgConfig: OptimizeImgConfig,
    proguardResourcesConfig: ProguardResourcesConfig
) {

    if (!config.enable) {
        return
    }

    if (!(optimizeImgConfig.enable || proguardResourcesConfig.enable || clearRepeatConfig.enable)) {
        println("啥都不干开启毛?")
        return
    }

    // 记录下原来的大小
    val originalLength = apFile.length()

    // 解压ZIP
    val unZipDir = "${apFile.parent}${File.separator}$DIR_NAME"

    ZipFile(apFile).unZipFiles(unZipDir)

    if (clearRepeatConfig.enable) {
        /**
         * 删除重复资源
         */
        val mappingFile = File(mappingDir, CLEAR_REPEAT_RESOURCES_MAPPING)

        DeleteRepeatAciton.deleteRepeatResources(mappingFile, clearRepeatConfig, apFile, unZipDir)
    }

    if (optimizeImgConfig.enable) {
        /**
         * 压缩图片
         * 压缩算法,参考逻辑  https://github.com/smallSohoSolo/McImage
         */

        // TODO 考虑从网上下载插件
        initTools()

        // 优化图片
        val mappingFile = File(mappingDir, OPTIMIZE_IMG_MAPPING)

        runBlocking {
            log("开始优化图片:$unZipDir")
            optimizeImg(mappingFile, optimizeImgConfig, unZipDir, CopyOnWriteArrayList())
        }
    }

    if (proguardResourcesConfig.enable) {
        /**
         * 混淆资源
         */
        val mappingFile = File(mappingDir, PROGUARD_RESOURCES_MAPPING)
        ProguardAction.proguard(mappingFile, unZipDir)
    }

    // 处理战场,并且打包
    val okLength = restore(apFile, unZipDir)
    println("优化完成节省:${originalLength - okLength}")

    // 删除多余数据
    FileOperation.deleteDir(File(unZipDir))
}


private fun restore(apFile: File, unZipDir: String): Long {
    /**
     * 删除原来的
     */
    apFile.delete()

    /**
     * 处理重复资源完毕,从新压缩
     */
    ZipOutputStream(apFile.outputStream()).use {
        it.zip(unZipDir, File(unZipDir))
    }
    return apFile.length()
}






