package com.codelabs.gardahijau.ui.prediksi.detail_siklus

data class SiklusItem(
    val title: String,
    val date: String = "",
    val description: String,
    val isClickable: Boolean = false
)