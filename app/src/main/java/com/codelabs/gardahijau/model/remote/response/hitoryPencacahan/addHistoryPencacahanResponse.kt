package com.codelabs.gardahijau.model.remote.response.hitoryPencacahan

data class Data(
	val tanggalWaktu: String,
	val createdAt: String,
	val totalSampah: Any,
	val catatan: String,
	val id: Int,
	val userId: Int
)

data class AddHistoryPencacahanResponse(
	val data: Data,
	val message: String
)

