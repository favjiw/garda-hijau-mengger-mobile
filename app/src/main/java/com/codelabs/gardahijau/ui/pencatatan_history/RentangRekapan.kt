package com.codelabs.gardahijau.ui.pencatatan_history

import java.util.Calendar
import java.util.Date

/**
 * Rentang tanggal untuk rekapan sampah di beranda Sampah.
 * Batas dihitung dari hari ini (zona waktu HP), awal hari s.d. akhir hari (inklusif).
 */
data class RentangRekapan(
    val jenis: Jenis,
    val dari: Date? = null,
    val sampai: Date? = null,
) {
    enum class Jenis { TUJUH_HARI, TIGA_PULUH_HARI, BULAN_INI, KUSTOM }

    /** Awal & akhir rentang (inklusif) untuk [hariIni]. KUSTOM yang terbalik dibetulkan otomatis. */
    fun batas(hariIni: Date = Date()): Pair<Date, Date> {
        fun awalHari(date: Date) = Calendar.getInstance().apply {
            time = date
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time

        fun akhirHari(date: Date) = Calendar.getInstance().apply {
            time = date
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.time

        fun geser(date: Date, hari: Int) = Calendar.getInstance().apply {
            time = date
            add(Calendar.DAY_OF_YEAR, hari)
        }.time

        return when (jenis) {
            Jenis.TUJUH_HARI -> awalHari(geser(hariIni, -6)) to akhirHari(hariIni)
            Jenis.TIGA_PULUH_HARI -> awalHari(geser(hariIni, -29)) to akhirHari(hariIni)
            Jenis.BULAN_INI -> awalHari(
                Calendar.getInstance().apply {
                    time = hariIni
                    set(Calendar.DAY_OF_MONTH, 1)
                }.time
            ) to akhirHari(hariIni)
            Jenis.KUSTOM -> {
                val a = dari ?: hariIni
                val b = sampai ?: hariIni
                val (x, y) = if (a.after(b)) b to a else a to b
                awalHari(x) to akhirHari(y)
            }
        }
    }
}
