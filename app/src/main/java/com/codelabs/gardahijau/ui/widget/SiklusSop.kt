package com.codelabs.gardahijau.ui.widget

import androidx.annotation.StringRes
import com.codelabs.gardahijau.R
import com.codelabs.gardahijau.ui.util.hariSejak
import java.util.Calendar
import java.util.Date

/**
 * Fase budidaya maggot sesuai SOP (gambar SOP hal. 5-6).
 * Fase berpindah otomatis dari tanggal mulai siklus; operator tidak mencatat perpindahan fase.
 */
enum class FaseSop(
    /** Label pendek untuk pita siklus. */
    @StringRes val labelRes: Int,
    /** Nama lengkap fase (kartu "Fase sekarang" & jadwal fase). */
    @StringRes val namaRes: Int,
    /** Panduan tindakan tim sesuai SOP. */
    @StringRes val panduanRes: Int,
    val hariAwal: Int,
    val hariAkhir: Int,
) {
    TELUR_LARVA(
        R.string.ghm_sop_pita_telur,
        R.string.ghm_sop_fase_telur,
        R.string.ghm_sop_panduan_telur,
        0,
        12,
    ),
    PEMBESARAN(
        R.string.ghm_sop_pita_besar,
        R.string.ghm_sop_fase_pembesaran,
        R.string.ghm_sop_panduan_pembesaran,
        13,
        20,
    ),
    PREPUPA(
        R.string.ghm_sop_pita_prepupa,
        R.string.ghm_sop_fase_prepupa,
        R.string.ghm_sop_panduan_prepupa,
        21,
        27,
    ),
    PUPA(
        R.string.ghm_sop_pita_pupa,
        R.string.ghm_sop_fase_pupa,
        R.string.ghm_sop_panduan_pupa,
        28,
        34,
    ),
    LALAT_BSF(
        R.string.ghm_sop_pita_lalat,
        R.string.ghm_sop_fase_lalat,
        R.string.ghm_sop_panduan_lalat,
        35,
        41,
    );

    companion object {
        /** Fase untuk umur [hari] (0 = hari mulai). null bila sudah lewat hari ke-41. */
        fun dariHari(hari: Int): FaseSop? = entries.firstOrNull { hari in it.hariAwal..it.hariAkhir }
    }
}

/** Tanggal mulai fase [fase] (hari ke-[FaseSop.hariAwal]) dihitung dari [tanggalMulai]. */
fun tanggalFase(tanggalMulai: Date, fase: FaseSop): Date =
    Calendar.getInstance().apply {
        time = tanggalMulai
        add(Calendar.DAY_OF_YEAR, fase.hariAwal)
    }.time

/** Fase SOP sekarang dari tanggal mulai; null bila tanggal tidak terbaca atau siklus sudah selesai. */
fun faseSopDari(tanggalMulai: Date?, hariIni: Date = Date()): FaseSop? {
    if (tanggalMulai == null) return null
    return FaseSop.dariHari(hariSejak(tanggalMulai, hariIni))
}
