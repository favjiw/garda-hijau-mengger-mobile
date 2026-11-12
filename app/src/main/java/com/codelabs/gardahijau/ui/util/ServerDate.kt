package com.codelabs.gardahijau.ui.util

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

val LOCALE_ID: Locale = Locale("id", "ID")

private val SERVER_PATTERNS = listOf(
    "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
    "yyyy-MM-dd'T'HH:mm:ss'Z'",
)

/** Tanggal dari API berakhiran 'Z' (UTC). Hasilnya ditampilkan dalam zona waktu HP. */
fun parseServerDate(value: String?): Date? {
    if (value.isNullOrBlank()) return null
    for (pattern in SERVER_PATTERNS) {
        val parser = SimpleDateFormat(pattern, Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        try {
            return parser.parse(value)
        } catch (e: Exception) {
        }
    }
    return null
}

/** 1240.0 -> "1.240", 12.5 -> "12,5" (format Indonesia). */
fun formatKg(value: Double?): String {
    val format = NumberFormat.getNumberInstance(LOCALE_ID).apply {
        maximumFractionDigits = 1
        minimumFractionDigits = 0
    }
    return format.format(value ?: 0.0)
}
