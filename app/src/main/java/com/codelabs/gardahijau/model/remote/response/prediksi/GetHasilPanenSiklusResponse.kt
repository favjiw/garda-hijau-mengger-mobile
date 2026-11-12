package com.codelabs.gardahijau.model.remote.response.prediksi

data class GetHasilPanenSiklusResponse(
	val data: List<DataItemPanenSiklus>,
	val success: Boolean
)

data class DataItemPanenSiklus(
	val createdAt: String,
	val estimatedValue: Int,
	val feedCost: Int,
	val faseId: Int,
	val hasilGram: Any,
	val id: Int,
	val hasilKg: Any,
	val conversionRate: Any,
	val conversionLabel: String,
	val roiEstimate: Any
)

