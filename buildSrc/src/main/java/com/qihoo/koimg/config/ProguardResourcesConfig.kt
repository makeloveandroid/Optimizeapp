package com.qihoo.koimg.config

import com.qihoo.koimg.OPTIMIZE_IMG_CONFIG_NAME
import kotlinx.coroutines.flow.DEFAULT_CONCURRENCY_PROPERTY_NAME
import org.gradle.api.Named

open class ProguardResourcesConfig : Named, BaseConfig() {
    override fun getName(): String {
        return OPTIMIZE_IMG_CONFIG_NAME
    }
}
