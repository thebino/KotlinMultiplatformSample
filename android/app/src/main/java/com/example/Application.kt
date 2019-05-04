package com.example

import com.example.home.HomeViewModel
import org.koin.android.ext.android.startKoin
import org.koin.android.logger.AndroidLogger
import org.koin.androidx.viewmodel.ext.koin.viewModel
import org.koin.dsl.module.module


class Application : android.app.Application() {
    private val appModule = module {
        viewModel(override = true) { HomeViewModel() }
    }

    override fun onCreate() {
        super.onCreate()

        initLogger()

        startKoin(this, listOf(appModule), logger = AndroidLogger(showDebug = true))
    }
}
