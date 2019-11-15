package com.qihoo.koimg.config

import com.qihoo.koimg.CLEAR_REPEAT_RESOURCES_CONFIG_NAME
import org.gradle.api.Named

open class ClearRepeatResourcesConfig : Named, BaseConfig() {
    override fun getName(): String {
        return CLEAR_REPEAT_RESOURCES_CONFIG_NAME
    }
}
