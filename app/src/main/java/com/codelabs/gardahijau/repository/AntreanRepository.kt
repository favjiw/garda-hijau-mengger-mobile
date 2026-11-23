package com.codelabs.gardahijau.repository

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.codelabs.gardahijau.model.local.antrean.AntreanDatabase
import com.codelabs.gardahijau.model.local.antrean.AntreanItem
import com.codelabs.gardahijau.model.local.antrean.AntreanWorker
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Antrean kirim offline: input disimpan di HP saat tidak ada internet,
 * lalu dikirim otomatis oleh [AntreanWorker] begitu jaringan tersedia.
 */
@Singleton
class AntreanRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val dao = AntreanDatabase.ambil(context).antreanDao()
    private val gson = Gson()

    /** Jumlah item yang masih menunggu dikirim (untuk penanda "menunggu sinkron" di layar). */
    val jumlahMenunggu: LiveData<Int> = dao.jumlah().asLiveData()

    /** Simpan satu permintaan ke antrean lokal lalu jadwalkan pengiriman. */
    suspend fun antrekan(jenis: String, payload: Any) {
        dao.tambah(
            AntreanItem(
                jenis = jenis,
                payload = gson.toJson(payload),
                dibuatPada = System.currentTimeMillis(),
            )
        )
        jadwalkanKirim()
    }

    /** Kosongkan antrean (dipakai saat keluar akun supaya tidak terkirim atas nama akun lain). */
    suspend fun bersihkan() {
        WorkManager.getInstance(context).cancelUniqueWork(NAMA_KERJA)
        dao.bersihkan()
    }

    private fun jadwalkanKirim() {
        val permintaan = OneTimeWorkRequestBuilder<AntreanWorker>()
            .setConstraints(
                Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
            )
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(NAMA_KERJA, ExistingWorkPolicy.KEEP, permintaan)
    }

    companion object {
        const val NAMA_KERJA = "antrean-kirim"
    }
}
