# 前介
`QOptimize` 插件是一款，在 `Apk` 编译期间对 `Apk` 进行体积优化的一款插件。  

[git地址](https://github.com/makeloveandroid/Optimizeapp)  

[博客地址](https://www.wenyingzhi.com/mu-lu-6/feng-mian)

## 功能&特色

1. 资源去重复
    - 支持动态开关
    - 支持自定义忽略文件
    - 动态生成 `mapping` 文件
    - 去除资源中相同的文件，包括 `xml` 丶 `图片` 等，并不是使用昵称做唯一性判断。


2. 资源图片压缩
    - 支持动态开关
    - 支持自定义忽略文件
    - 动态生成 `mapping` 文件
    - 支持图片压缩，每张图能节省百分之70大小
    - 支持对图片进行 `webp` 无感知格式转换

3. 资源混淆
    - 支持动态开关
    - 动态生成 `mapping` 文件
    - 支持自定义忽略文件

## 如何使用呢？

### 配置根工程
首先，修改你根目录的build.gradle.

```java
buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        classpath 'com.qihoo.optimize:QOptimize:1.2.0'
    }
}
```
### 配置项目工程
在你需要优化的 `Module`的 `build.gradle` 中应用这个插件.

```java
apply plugin: 'QOptimize'

QOptimizeConfig {
    /**
     * 总开关
     */
    enable true

    /**
     * debug 下是否可用，default true
     */
    enableWhenDebug true

    /**
     * 资源去重复配置
     */
    ClearRepeatResourcesConfig {
        enable true
        ignoreFileName = ['xxxx','xxxx']
    }

    /**
     * 图片压缩优化配置
     */
    OptimizeImgConfig {
        enable true


        //优化类型，可选"ConvertWebp"，"Compress"，转换为webp或原图压缩，默认Compress，使用ConvertWep需要min sdk >= 18.但是压缩效果更好
        optimizeType "ConvertWebp"

        //是否支持带有透明度的webp，default false,带有透明图的图片会进行压缩
        supportAlphaWebp true

        ignoreFileName = ['xxxx','xxxx']
    }


    /**
     * 资源混淆配置
     */
    ProguardResourcesConfig {
        enable true
        ignoreFileName = ['xxxx','xxxx']
    }
}

```

### 配置工具
请下载 `Qtools.zip` [下载地址](https://github.com/makeloveandroid/Optimizeapp/releases/tag/1.2)，并解压到项目的根路径。  

![](http://p0.qhimg.com/t01dbe7eeb4cfad3cad.jpg)

### 效果
当你构建项目的时候，能看到。  

![](http://p0.qhimg.com/t01bb28699cb7bfb4c4.jpg)

查看对应生成的 `mapping` 文件。

![](http://p0.qhimg.com/t01563f08c9ac882658.jpg)

## 常见问题
![](http://p0.qhimg.com/t0147a2a16460360f33.jpg)

出现这个问题是因为没有配置 `Qtools` 请参考上方的 `配置工具`.

![](http://p0.qhimg.com/t018496f0fbacaa135f.jpg)

由于 `linux` 或 `mac` 权限问题，可以通过 `chmod -R 777 /Users/wenyingzhi/Desktop/project/LiaoBei/QTools/mac/cwebp`，给与此文件可执行权限。

![](http://p0.qhimg.com/t018496f0fbacaa135f.jpg)

当前插件最低支持 `4.10.1` 的 `Gradle` 版本，请修改 `Gralde` 版本，修改 `项目工程路径/gradle/wrapper/gradle-wrapper.properties`。  
修改完可能 `Android` 的插件也需要修改版本。对应 `Gradle` 和 `Android` 插件版本映射表，请查看[官网](https://developer.android.com/studio/releases/gradle-plugin)(自备梯子)。  

![](http://p0.qhimg.com/t01821e68c9ae93e37b.jpg)
