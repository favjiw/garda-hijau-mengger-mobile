package com.codelabs.gardahijau.model.remote.body

/**
 * Body POST /api/siklus/{siklusId}/fase. Field null tidak dikirim (Gson melewati null).
 *
 * - PENETASAN: jumlahTelur (gram telur)
 * - PEMBESARAN: keterangan; jumlahMakanan (gram) boleh diisi untuk catatan
 * - PANEN: jumlahTelur (gram, wajib >= 1) dan jumlahMakanan (gram, wajib >= 1) -> memicu prediksi AI
 */
data class FaseRequest(
    val jenis: String,
    val tanggal: String,
    val jumlahTelur: Int? = null,
    val jumlahMakanan: Double? = null,
    val keterangan: String? = null,
)
