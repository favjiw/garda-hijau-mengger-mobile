package com.codelabs.gardahijau

import android.app.Application
import com.codelabs.gardahijau.model.remote.network.PesanGalat
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        PesanGalat.init(this)
    }
}
