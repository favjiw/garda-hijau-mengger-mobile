package com.codelabs.gardahijau.model.remote.response.hitoryPencacahan

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

data class GetHistoryPencacahanResponse(
	val data: List<DataItem>,
	val status: String
)

@Parcelize
data class DataItem(
	val tanggalWaktu: String? = "",
	val createdAt: String? = "",
	val totalSampah: Double? = 0.0,
	val catatan: String? = "",
	val id: Int? = 0,
	val userId: Int? = 0
) : Parcelable

