package com.codelabs.gardahijau.model.remote.response.prediksi

data class GetHasilPanenDashboardResponse(
	val success: Boolean,
	val totalGram: Int,
	val totalKg: Any,
	val jumlahPrediksi: Int
)

