package com.codelabs.gardahijau.model.remote.response.prediksi

/** Balasan POST /api/siklus/{id}/fase untuk PENETASAN / PEMBESARAN: `data` = fase. */
data class AddFaseResponse(
	val data: Data? = null,
	val success: Boolean = false
)

data class Data(
	val createdAt: String? = null,
	val keterangan: String? = null,
	val siklusId: Int? = null,
	val jenis: String? = null,
	val id: Int? = null,
	val tanggal: String? = null,
	val jumlahMakanan: Double? = null,
	val jumlahTelur: Any? = null
)

/** Balasan POST /api/siklus/{id}/fase untuk PANEN: `data` = { fase, prediksiPanen }. */
data class AddFasePanenResponse(
	val data: FaseDenganPrediksi? = null,
	val success: Boolean = false
)

data class FaseDenganPrediksi(
	val fase: Data? = null,
	val prediksiPanen: PrediksiPanen? = null
)
