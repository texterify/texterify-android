package com.texterify.android.sample

import android.app.Application
import com.texterify.android.ota.Texterify

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        val config = Texterify.config(this)
            .host(BuildConfig.HOST)
            .projectId(BuildConfig.PROJECT_ID)
            .exportConfigId(BuildConfig.EXPORT_CONFIG_ID)

        Texterify.init(config)
    }
}
