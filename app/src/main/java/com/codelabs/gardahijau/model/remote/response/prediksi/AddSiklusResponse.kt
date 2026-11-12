package com.codelabs.gardahijau.model.remote.response.prediksi

/** Balasan POST /api/siklus. */
data class AddSiklusResponse(
	val data: DataFase? = null,
	val success: Boolean = false
)

data class DataFase(
	val createdAt: String? = null,
	val tanggalMulai: String? = null,
	val catatan: String? = null,
	val mediaTelur: String? = null,
	val id: Int = 0,
	val userId: Int? = null,
	val jumlahTelur: Int? = null
)
