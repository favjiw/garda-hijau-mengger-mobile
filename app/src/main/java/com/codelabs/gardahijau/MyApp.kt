package com.codelabs.gardahijau

import android.app.Application
import com.codelabs.gardahijau.model.remote.network.PesanGalat
import com.codelabs.gardahijau.ui.pengingat.PengingatSiklus
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        PesanGalat.init(this)
        PengingatSiklus.buatKanal(this)
    }
}
