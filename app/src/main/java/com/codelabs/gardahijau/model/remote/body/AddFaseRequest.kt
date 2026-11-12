package com.codelabs.gardahijau.model.remote.body

data class AddFaseRequest(
    val jenis: String,
    val tanggal: String ,
    val jumlahTelur: Int,
    val mediaTelur: String,
    val catatan: String
)

data class AddFasePembesaranRequest(
    val jenis: String,
    val tanggal: String ,
    val jumlahMakanan: Int,
    val catatan: String
)

