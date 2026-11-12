package com.codelabs.gardahijau.model.remote.body

data class AddSiklusRequest(
    val tanggalMulai: String,
    val jumlahTelur: Int,
    val mediaTelur: String,
    val catatan: String
)
