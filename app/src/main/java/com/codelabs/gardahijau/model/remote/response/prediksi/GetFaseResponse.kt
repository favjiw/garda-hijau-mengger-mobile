package com.codelabs.gardahijau.model.remote.response.prediksi

/** Balasan GET /api/siklus/{siklusId}/fase (urut tanggal, lama -> baru). */
data class GetFaseResponse(
	val data: List<DataGetFase>,
	val success: Boolean
)

/**
 * Satu fase. jumlahTelur & jumlahMakanan dalam GRAM dan bisa null (mis. untuk PEMBESARAN).
 * prediksiPanen hanya terisi untuk fase PANEN (dibaca sebagai Map oleh Gson).
 */
data class DataGetFase(
	val createdAt: String? = null,
	val keterangan: String? = null,
	val siklusId: Int? = null,
	val jenis: String = "",
	val prediksiPanen: Any? = null,
	val id: Int = 0,
	val tanggal: String? = null,
	val jumlahMakanan: Double? = null,
	val jumlahTelur: Any? = null
)

/** Hasil prediksi AI (1 per fase PANEN). Angka uang & gram bertipe number di API. */
data class PrediksiPanen(
	val createdAt: String? = null,
	val estimatedValue: Double? = null,
	val feedCost: Double? = null,
	val faseId: Int? = null,
	val hasilGram: Double? = null,
	val id: Int? = null,
	val hasilKg: Double? = null,
	val conversionRate: Double? = null,
	val conversionLabel: String? = null,
	val roiEstimate: Double? = null,
	val rekomendasi: List<String>? = null
)
