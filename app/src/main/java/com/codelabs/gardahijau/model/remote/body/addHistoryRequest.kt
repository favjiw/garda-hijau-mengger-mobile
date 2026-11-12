package com.codelabs.gardahijau.model.remote.body

data class addHistoryRequest(
    val tanggalWaktu: String,
    val totalSampah: Double,
    val catatan: String
)