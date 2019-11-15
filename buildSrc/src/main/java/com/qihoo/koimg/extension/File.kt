package com.qihoo.koimg.extension

import com.qihoo.koimg.RESOURCES_NAME
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipFile
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import org.gradle.internal.nativeintegration.filesystem.DefaultFileMetadata.file
import java.io.BufferedInputStream
import java.io.FileInputStream


/**
 * 将所有资源根据 crc 进行分组
 */
fun ZipFile.groupsResources(): MutableMap<String, MutableList<ZipEntry>> {
    val groups = mutableMapOf<String, MutableList<ZipEntry>>()
    use { zip ->
        zip
            .entries()
            .iterator()
            .forEach {
                val key = "${it.crc}${File(it.name).parent}"
                val list = if (groups.containsKey(key)) {
                    groups[key]!!
                } else {
                    groups[key] = mutableListOf()
                    groups[key]!!
                }
                list.add(it)
            }
    }
    return groups
}


fun ZipFile.unZipFiles(descDir: String) {
    val pathFile = File(descDir)
    if (!pathFile.exists()) {
        pathFile.mkdirs()
    }
    val zip = this
    val entries = zip.entries()
    while (entries.hasMoreElements()) {
        val entry = entries.nextElement() as ZipEntry
        val zipEntryName = entry.getName()
        val `in` = zip.getInputStream(entry)
        val outPath = (descDir + File.separator + zipEntryName).replace("\\*".toRegex(), "/")
        //判断路径是否存在,不存在则创建文件路径
        val file = File(outPath.substring(0, outPath.lastIndexOf('/')))
        if (!file.exists()) {
            file.mkdirs()
        }
        //判断文件全路径是否为文件夹,如果是上面已经上传,不需要解压
        if (File(outPath).isDirectory()) {
            continue
        }

        val out = FileOutputStream(outPath)
        `in`.use {
            it.copyTo(out)
        }
        out.close()
    }
}

fun ZipOutputStream.zip(srcRootDir: String, file: File) {
    if (file == null) {
        return
    }
    //如果是文件，则直接压缩该文件
    if (file.isFile) {
        //获取文件相对于压缩文件夹根目录的子路径
        var subPath = file.absolutePath
        val index = subPath.indexOf(srcRootDir)
        if (index != -1) {
            subPath = subPath.substring(srcRootDir.length + File.separator.length)
        }
        val entry = ZipEntry(subPath)
        putNextEntry(entry)

        FileInputStream(file).use {
            it.copyTo(this)
        }
        closeEntry()
    } else {
        //压缩目录中的文件或子目录
        val childFileList = file.listFiles()
        for (n in childFileList.indices) {
            childFileList[n].absolutePath.indexOf(file.absolutePath)
            zip(srcRootDir, childFileList[n])
        }
    }
    //如果是目录，则压缩整个目录

}
