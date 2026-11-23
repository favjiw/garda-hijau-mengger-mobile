package com.codelabs.gardahijau.ui.pengingat

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

/** Worker pengingat siklus: menampilkan notifikasi lokal (pindah fase / pakan). */
class PengingatWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {
        val siklusId = inputData.getInt(KEY_SIKLUS, -1)
        val hari = inputData.getInt(KEY_HARI, -1)
        val jenis = inputData.getString(KEY_JENIS)
        if (siklusId == -1 || hari == -1 || jenis == null) return Result.failure()
        PengingatSiklus.tampilkan(
            applicationContext,
            siklusId,
            hari,
            jenis,
            inputData.getString(KEY_TANGGAL_MULAI),
        )
        return Result.success()
    }

    companion object {
        const val KEY_SIKLUS = "siklus_id"
        const val KEY_HARI = "hari"
        const val KEY_JENIS = "jenis"
        const val KEY_TANGGAL_MULAI = "tanggal_mulai"
    }
}
