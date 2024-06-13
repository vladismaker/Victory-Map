package com.application.victorymap.presentation

import android.app.Application
import com.yandex.mapkit.MapKitFactory

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        MapKitFactory.setApiKey("056b99e2-5ad6-42b8-a05c-9c4a210fa3e4")
        MapKitFactory.initialize(applicationContext)
    }
}