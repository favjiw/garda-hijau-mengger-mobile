package com.codelabs.gardahijau.model.remote.response.prediksi

data class GetFaseResponse(
	val data: List<DataGetFase>,
	val success: Boolean
)

data class DataGetFase(
	val createdAt: String,
	val keterangan: String,
	val siklusId: Int,
	val jenis: String,
	val prediksiPanen: Any,
	val id: Int,
	val tanggal: String,
	val jumlahMakanan: Int,
	val jumlahTelur: Any
)

data class PrediksiPanen(
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

