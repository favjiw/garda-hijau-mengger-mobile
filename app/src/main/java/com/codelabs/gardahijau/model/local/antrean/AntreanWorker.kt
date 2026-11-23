package com.codelabs.gardahijau.model.local.antrean

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.codelabs.gardahijau.model.local.data.UserPreferences
import com.codelabs.gardahijau.model.remote.body.AddSiklusRequest
import com.codelabs.gardahijau.model.remote.body.FaseRequest
import com.codelabs.gardahijau.model.remote.body.addHistoryRequest
import com.codelabs.gardahijau.model.remote.network.ApiConfig
import com.codelabs.gardahijau.model.remote.network.MainApiService
import com.codelabs.gardahijau.model.remote.network.pesanGalat
import com.google.gson.Gson
import retrofit2.HttpException
import java.io.IOException

/**
 * Mengirim isi antrean offline ke server, satu per satu, urut dari yang paling lama.
 * - Berhasil -> item dihapus.
 * - Gagal karena jaringan / 5xx / 429 -> item disimpan, worker dijadwalkan ulang (retry).
 * - Gagal karena 4xx (permintaan tidak akan pernah diterima) -> item dibuang.
 * - 401 (sesi habis) -> berhenti; antrean menunggu sampai pengguna masuk lagi.
 * Catatan: server belum idempoten, jadi item hanya dikirim sekali (dihapus setelah sukses).
 */
class AntreanWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val dao = AntreanDatabase.ambil(applicationContext).antreanDao()
        val api = ApiConfig.getMainApiService(applicationContext, UserPreferences(applicationContext))
        val gson = Gson()
        var adaGagal = false

        for (item in dao.semua()) {
            val aksi = try {
                kirim(api, gson, item)
                KlasifikasiAntrean.Aksi.BERHASIL
            } catch (e: Exception) {
                Log.w(TAG, "Kirim antrean ${item.jenis} #${item.id} gagal: ${e.pesanGalat()}", e)
                KlasifikasiAntrean.aksi(e)
            }
            when (aksi) {
                KlasifikasiAntrean.Aksi.BERHASIL -> dao.hapus(item)
                KlasifikasiAntrean.Aksi.BUANG -> {
                    Log.w(TAG, "Antrean ${item.jenis} #${item.id} dibuang (ditolak server).")
                    dao.hapus(item)
                }
                KlasifikasiAntrean.Aksi.BERHENTI -> return Result.retry()
                KlasifikasiAntrean.Aksi.ULANGI -> {
                    dao.tambahPercobaan(item.id)
                    if (item.percobaan + 1 >= MAKS_PERCOBAAN) {
                        Log.w(TAG, "Antrean ${item.jenis} #${item.id} dibuang setelah $MAKS_PERCOBAAN percobaan.")
                        dao.hapus(item)
                    } else {
                        adaGagal = true
                    }
                }
            }
        }
        return if (adaGagal) Result.retry() else Result.success()
    }

    private suspend fun kirim(api: MainApiService, gson: Gson, item: AntreanItem) {
        when (item.jenis) {
            JenisAntrean.SAMPAH -> {
                val p = gson.fromJson(item.payload, AntreanSampah::class.java)
                api.addHistoryPencacahan(addHistoryRequest(p.tanggalWaktu, p.totalSampah, p.catatan))
            }

            JenisAntrean.SIKLUS -> {
                val p = gson.fromJson(item.payload, AntreanSiklus::class.java)
                val siklus = api.addSiklus(
                    AddSiklusRequest(p.tanggalMulai, p.jumlahTelur, p.mediaTelur, p.catatan)
                )
                val siklusId = siklus.data?.id ?: return
                // Sama seperti PrediksiRepository.addSiklus: pastikan fase PENETASAN ada.
                val sudahAda = api.getFase(siklusId).data.orEmpty()
                    .any { it.jenis.equals("PENETASAN", ignoreCase = true) }
                if (!sudahAda) {
                    api.addFase(
                        siklusId,
                        FaseRequest(jenis = "PENETASAN", tanggal = p.tanggalMulai, jumlahTelur = p.jumlahTelur),
                    )
                }
            }

            JenisAntrean.FASE_BESAR -> {
                val p = gson.fromJson(item.payload, AntreanFaseBesar::class.java)
                val pakanGram = p.pakanKg * 1000.0
                api.addFase(
                    p.siklusId,
                    FaseRequest(
                        jenis = "PEMBESARAN",
                        tanggal = p.tanggal,
                        jumlahMakanan = pakanGram,
                        keterangan = p.keterangan.ifBlank { null },
                    ),
                )
                api.addFasePanen(
                    p.siklusId,
                    FaseRequest(
                        jenis = "PANEN",
                        tanggal = p.tanggal,
                        jumlahTelur = p.telurGram,
                        jumlahMakanan = pakanGram,
                        keterangan = p.keterangan.ifBlank { null },
                    ),
                )
            }

            JenisAntrean.PERKIRAAN -> {
                val p = gson.fromJson(item.payload, AntreanPerkiraan::class.java)
                api.addFasePanen(
                    p.siklusId,
                    FaseRequest(
                        jenis = "PANEN",
                        tanggal = p.tanggal,
                        jumlahTelur = p.telurGram,
                        jumlahMakanan = p.pakanGram,
                    ),
                )
            }
        }
    }

    companion object {
        private const val TAG = "AntreanWorker"
        private const val MAKS_PERCOBAAN = 5
    }
}

/** Menentukan nasib satu item antrean dari jenis galatnya. */
internal object KlasifikasiAntrean {
    enum class Aksi { BERHASIL, ULANGI, BUANG, BERHENTI }

    fun aksi(error: Throwable): Aksi = when (error) {
        is HttpException -> when (error.code()) {
            401 -> Aksi.BERHENTI
            429 -> Aksi.ULANGI
            in 500..599 -> Aksi.ULANGI
            in 400..499 -> Aksi.BUANG
            else -> Aksi.ULANGI
        }
        // Timeout juga turunan IOException.
        is IOException -> Aksi.ULANGI
        else -> Aksi.ULANGI
    }
}
