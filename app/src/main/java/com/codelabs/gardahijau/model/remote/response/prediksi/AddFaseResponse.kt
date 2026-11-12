package com.codelabs.gardahijau.model.remote.response.prediksi

data class AddFaseResponse(
	val data: Data,
	val success: Boolean
)

data class Data(
	val createdAt: String,
	val keterangan: String,
	val siklusId: Int,
	val jenis: String,
	val id: Int,
	val tanggal: String,
	val jumlahMakanan: Int,
	val jumlahTelur: Any
)

