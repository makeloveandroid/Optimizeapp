package com.qihoo.koimg.config

import org.gradle.api.Named
import org.gradle.api.NamedDomainObjectContainer

open class QOptimizeConfig(
    var enable: Boolean = true){
    var enableWhenDebug = true
}
