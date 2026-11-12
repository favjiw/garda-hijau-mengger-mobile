package com.codelabs.gardahijau.model.remote.response.prediksi

/** Balasan GET /api/prediksi/siklus/{siklusId}. Kosong jika siklus belum punya fase PANEN. */
data class GetHasilPanenSiklusResponse(
	val data: List<DataItemPanenSiklus>,
	val success: Boolean
)

data class DataItemPanenSiklus(
	val createdAt: String? = null,
	val estimatedValue: Double? = null,
	val feedCost: Double? = null,
	val faseId: Int? = null,
	val hasilGram: Any? = null,
	val id: Int? = null,
	val hasilKg: Any? = null,
	val conversionRate: Any? = null,
	val conversionLabel: String? = null,
	val roiEstimate: Any? = null
)
