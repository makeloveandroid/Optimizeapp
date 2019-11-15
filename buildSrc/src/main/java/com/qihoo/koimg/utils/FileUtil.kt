package com.qihoo.koimg.utils

import java.io.File
import java.io.FileWriter

@Synchronized
fun FileWriter.synchronizedWriteString(string: String) {
    write("$string${System.getProperty("line.separator")}")
}

object FileUtil {
    const val PATH_NAME = "KoReTools"

    private lateinit var rootDir: String

    fun setRootDir(rootDir: String) {
        FileUtil.rootDir = rootDir
    }

    fun getRootDirPath(): String {
        return rootDir
    }

    fun getToolsDir(): File {
        return File("$rootDir/$PATH_NAME/")
    }

    fun getToolsDirPath(): String {
        return "$rootDir/$PATH_NAME/"
    }
}
