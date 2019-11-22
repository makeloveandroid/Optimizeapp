package com.qihoo.koimg

import com.android.build.gradle.AppExtension
import com.android.build.gradle.internal.res.LinkApplicationAndroidResourcesTask
import com.qihoo.koimg.config.ClearRepeatResourcesConfig
import com.qihoo.koimg.config.OptimizeImgConfig
import com.qihoo.koimg.config.OptimizeImgConfig.Companion.OPTIMIZE_WEBP_CONVERT
import com.qihoo.koimg.config.ProguardResourcesConfig
import com.qihoo.koimg.config.QOptimizeConfig
import com.qihoo.koimg.utils.AndroidUtil
import com.qihoo.koimg.utils.FileUtil
import com.qihoo.koimg.utils.WebpUtils
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.internal.tasks.compile.JavaCompilerArgumentsBuilder.LOGGER
import java.io.File
import kotlin.system.measureTimeMillis

const val RESOURCES_NAME = "resources.arsc"
const val DIR_NAME = "QOptimize"
const val PULGIN_NAME = "QOptimizePlugin"

const val CONFIG_NAME = "QOptimizeConfig"
const val CLEAR_REPEAT_RESOURCES_CONFIG_NAME = "ClearRepeatResourcesConfig"
const val OPTIMIZE_IMG_CONFIG_NAME = "OptimizeImgConfig"
const val PROGUARD_RESOURCES_CONFIG_NAME = "ProguardResourcesConfig"

// 生成 mapping 的路径
var mappingDir = "QOptimize${File.separator}mapping${File.separator}"

var CLEAR_REPEAT_RESOURCES_MAPPING = "ClearRepeatResourcesMapping.txt"
var OPTIMIZE_IMG_MAPPING = "OptimizeImgMapping.txt"
var PROGUARD_RESOURCES_MAPPING = "ProguardResourcesMapping.txt"


class QOptimizePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        // 1. 创建配置
        project.extensions.create(CONFIG_NAME, QOptimizeConfig::class.java)
        project.extensions.create(CLEAR_REPEAT_RESOURCES_CONFIG_NAME, ClearRepeatResourcesConfig::class.java)
        project.extensions.create(OPTIMIZE_IMG_CONFIG_NAME, OptimizeImgConfig::class.java)
        project.extensions.create(PROGUARD_RESOURCES_CONFIG_NAME, ProguardResourcesConfig::class.java)


        project.afterEvaluate {
            // 项目构建完成之后

            // 2. 记录 root 路径
            FileUtil.setRootDir(project.rootDir.path)


            // 3. 得到配置
            val config = project.extensions.findByName(CONFIG_NAME) as QOptimizeConfig
            val clearRepeatConfig =
                project.extensions.findByName(CLEAR_REPEAT_RESOURCES_CONFIG_NAME) as ClearRepeatResourcesConfig
            val optimizeImgConfig = project.extensions.findByName(OPTIMIZE_IMG_CONFIG_NAME) as OptimizeImgConfig
            val proguardResourcesConfig =
                project.extensions.findByName(PROGUARD_RESOURCES_CONFIG_NAME) as ProguardResourcesConfig


            // 4. 判断是否启用插件
            if (!config.enable) {
                return@afterEvaluate
            }
            // 检测 是否是 webp 模式
            if (optimizeImgConfig.optimizeType == OPTIMIZE_WEBP_CONVERT && AndroidUtil.getMinSdkVersion(project) < WebpUtils.VERSION_SUPPORT_WEBP) {
                // webp 模式只能在 sdk >14 上使用
                throw GradleException("minSDK < 14, Webp 优化图片模式不能使用! 请更改模式 optimizeType=Compress 或 修改 minSdkVersion>=14")
            }

            // 5. 只允许在 application 使用插件
            val hasPlugin = project.plugins.hasPlugin("com.android.application")
            if (hasPlugin) {
                // 6. 获取 andorid 配置
                val android = project.extensions.findByName("android")
                if (android is AppExtension) {
                    // 7. 遍历所有的 Variant
                    android.applicationVariants.forEach {
                        val variantName = it.name.capitalize()
                        // 判断debug模式下是否开启使用
                        if (!config.enableWhenDebug && variantName == "Debug") {
                            // debug 模式不适用
                            return@forEach
                        }
                        // 8. 寻找 processXXXResources Task
                        val processResources = project.tasks.getByName("process${variantName}Resources")
                        // 9. 添加 doLast 的 action
                        processResources.doLast {
                            val resourcesTask = it as LinkApplicationAndroidResourcesTask
                            // 10.获取输出路径
                            val resPackageOutputFolder = resourcesTask.resPackageOutputFolder

                            resPackageOutputFolder
                                .listFiles { file ->
                                    // 11. 寻找 resources-xxx.ap_
                                    file.name.endsWith(".ap_")
                                }.forEach {

                                    val time = measureTimeMillis {
                                        val outMappintDir = "${project.buildDir}${File.separator}$mappingDir"
                                        File(outMappintDir).takeIf {
                                            !it.exists()
                                        }?.apply {
                                            mkdirs()
                                        }
                                        // 12. 开始执行优化
                                        start(
                                            outMappintDir
                                            ,
                                            it,
                                            config,
                                            clearRepeatConfig,
                                            optimizeImgConfig,
                                            proguardResourcesConfig
                                        )
                                    }
                                    LOGGER.debug("优化开始了啊!!!")
                                    println("完成优化,运行时间:$time")
                                }
                        }
                    }
                }
            } else {
                throw GradleException("$PULGIN_NAME 插件只能用在 Application 项目中")
            }

        }
    }
}