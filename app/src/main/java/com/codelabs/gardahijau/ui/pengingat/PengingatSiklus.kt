package com.codelabs.gardahijau.ui.pengingat

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.codelabs.gardahijau.R
import com.codelabs.gardahijau.ui.prediksi.detail_siklus.DetailSiklusActivity
import com.codelabs.gardahijau.ui.util.parseServerDate
import com.codelabs.gardahijau.ui.widget.FaseSop
import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeUnit

/**
 * Pengingat lokal berbasis tanggal siklus (WorkManager):
 * - pindah fase (hari ke-13, 21, 28, 35),
 * - pakan harian selama fase Pembesaran (hari ke-13 s.d. 20).
 * Dijadwalkan ulang setiap daftar siklus dimuat; nama kerja unik supaya tidak dobel.
 */
object PengingatSiklus {

    const val KANAL_ID = "pengingat_siklus"
    private const val JAM_PENGINGAT = 8 // 08.00 waktu HP

    /** Buat kanal notifikasi (dipanggil sekali dari MyApp). */
    fun buatKanal(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val kanal = NotificationChannel(
            KANAL_ID,
            context.getString(R.string.ghm_kanal_pengingat),
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = context.getString(R.string.ghm_kanal_pengingat_deskripsi)
        }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(kanal)
    }

    /** Jadwalkan pengingat untuk satu siklus. Aman dipanggil berulang (nama kerja unik + KEEP). */
    fun jadwalkan(context: Context, siklusId: Int, tanggalMulaiIso: String?) {
        val mulai = parseServerDate(tanggalMulaiIso) ?: return
        val wm = WorkManager.getInstance(context)

        // Pindah fase: hari pertama tiap fase selain fase pertama.
        FaseSop.entries.drop(1).forEach { fase ->
            jadwalkanSatu(wm, siklusId, fase.hariAwal, mulai, tanggalMulaiIso, JenisPengingat.FASE)
        }
        // Pakan harian selama Pembesaran (hari 13 s.d. 20).
        (FaseSop.PEMBESARAN.hariAwal..FaseSop.PEMBESARAN.hariAkhir).forEach { hari ->
            jadwalkanSatu(wm, siklusId, hari, mulai, tanggalMulaiIso, JenisPengingat.PAKAN)
        }
    }

    private fun jadwalkanSatu(
        wm: WorkManager,
        siklusId: Int,
        hari: Int,
        tanggalMulai: Date,
        tanggalMulaiIso: String?,
        jenis: JenisPengingat,
    ) {
        val waktu = Calendar.getInstance().apply {
            time = tanggalMulai
            add(Calendar.DAY_OF_YEAR, hari)
            set(Calendar.HOUR_OF_DAY, JAM_PENGINGAT)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val tunda = waktu - System.currentTimeMillis()
        if (tunda <= 0) return // sudah lewat: tidak perlu dijadwalkan

        val data = Data.Builder()
            .putInt(PengingatWorker.KEY_SIKLUS, siklusId)
            .putInt(PengingatWorker.KEY_HARI, hari)
            .putString(PengingatWorker.KEY_JENIS, jenis.name)
            .putString(PengingatWorker.KEY_TANGGAL_MULAI, tanggalMulaiIso)
            .build()
        val permintaan = OneTimeWorkRequestBuilder<PengingatWorker>()
            .setInitialDelay(tunda, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .build()
        wm.enqueueUniqueWork(namaKerja(siklusId, jenis, hari), ExistingWorkPolicy.KEEP, permintaan)
    }

    /** Batalkan semua pengingat satu siklus (mis. saat keluar akun). */
    fun batalkan(context: Context, siklusId: Int) {
        WorkManager.getInstance(context).cancelAllWorkByTag(tagSiklus(siklusId))
    }

    /** Tampilkan notifikasi (dipanggil worker). */
    @SuppressLint("MissingPermission")
    fun tampilkan(context: Context, siklusId: Int, hari: Int, jenis: String, tanggalMulaiIso: String?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        val fase = FaseSop.dariHari(hari)
        val (judul, isi) = if (jenis == JenisPengingat.FASE.name) {
            context.getString(
                R.string.ghm_pengingat_fase_judul,
                siklusId,
                context.getString(fase?.namaRes ?: R.string.ghm_sop_fase_lalat),
            ) to context.getString(R.string.ghm_pengingat_fase_isi, hari)
        } else {
            context.getString(R.string.ghm_pengingat_pakan_judul) to
                context.getString(R.string.ghm_pengingat_pakan_isi, siklusId, hari)
        }

        val intent = Intent(context, DetailSiklusActivity::class.java).apply {
            putExtra("EXTRA_ID", siklusId)
            putExtra("EXTRA_TANGGAL_MULAI", tanggalMulaiIso)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pending = PendingIntent.getActivity(
            context,
            idNotifikasi(siklusId, hari, jenis),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notifikasi = NotificationCompat.Builder(context, KANAL_ID)
            .setSmallIcon(R.drawable.ic_ghm_bell)
            .setContentTitle(judul)
            .setContentText(isi)
            .setStyle(NotificationCompat.BigTextStyle().bigText(isi))
            .setContentIntent(pending)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(idNotifikasi(siklusId, hari, jenis), notifikasi)
    }

    private fun namaKerja(siklusId: Int, jenis: JenisPengingat, hari: Int) =
        "siklus-$siklusId-${jenis.name.lowercase()}-$hari"

    private fun tagSiklus(siklusId: Int) = "siklus-$siklusId"

    private fun idNotifikasi(siklusId: Int, hari: Int, jenis: String) =
        siklusId * 1000 + hari * 2 + if (jenis == JenisPengingat.FASE.name) 0 else 1

    enum class JenisPengingat { FASE, PAKAN }
}
