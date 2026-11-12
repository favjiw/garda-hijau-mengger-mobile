package com.codelabs.gardahijau.model.remote.response.hitoryPencacahan

data class Data(
	val tanggalWaktu: String? = null,
	val createdAt: String? = null,
	val totalSampah: Any? = null,
	val catatan: String? = null,
	val id: Int? = null,
	val userId: Int? = null
)

/** Balasan POST /api/pencacahan. Field dibuat boleh null karena contoh balasan belum ada di dokumentasi API. */
data class AddHistoryPencacahanResponse(
	val data: Data? = null,
	val message: String? = null,
	val success: Boolean? = null
)
