package com.codelabs.gardahijau

import com.codelabs.gardahijau.ui.widget.FaseSop
import com.codelabs.gardahijau.ui.widget.faseSopDari
import com.codelabs.gardahijau.ui.widget.tanggalFase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/** Uji perhitungan fase/umur siklus maggot sesuai SOP (hari 0-12, 13-20, 21-27, 28-34, 35-41). */
class SiklusSopTest {

    private fun tanggal(y: Int, m: Int, d: Int): Date =
        Calendar.getInstance().apply {
            clear()
            set(y, m - 1, d, 12, 0, 0)
        }.time

    private fun format(date: Date): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(date)

    @Test
    fun fasePerHari_sesuaiSop() {
        assertEquals(FaseSop.TELUR_LARVA, FaseSop.dariHari(0))
        assertEquals(FaseSop.TELUR_LARVA, FaseSop.dariHari(12))
        assertEquals(FaseSop.PEMBESARAN, FaseSop.dariHari(13))
        assertEquals(FaseSop.PEMBESARAN, FaseSop.dariHari(20))
        assertEquals(FaseSop.PREPUPA, FaseSop.dariHari(21))
        assertEquals(FaseSop.PREPUPA, FaseSop.dariHari(27))
        assertEquals(FaseSop.PUPA, FaseSop.dariHari(28))
        assertEquals(FaseSop.PUPA, FaseSop.dariHari(34))
        assertEquals(FaseSop.LALAT_BSF, FaseSop.dariHari(35))
        assertEquals(FaseSop.LALAT_BSF, FaseSop.dariHari(41))
        assertNull(FaseSop.dariHari(42))
    }

    @Test
    fun faseDariTanggalMulai() {
        val mulai = tanggal(2026, 11, 10)
        assertEquals(FaseSop.TELUR_LARVA, faseSopDari(mulai, tanggal(2026, 11, 10))) // hari 0
        assertEquals(FaseSop.TELUR_LARVA, faseSopDari(mulai, tanggal(2026, 11, 22))) // hari 12
        assertEquals(FaseSop.PEMBESARAN, faseSopDari(mulai, tanggal(2026, 11, 23))) // hari 13
        assertEquals(FaseSop.LALAT_BSF, faseSopDari(mulai, tanggal(2026, 12, 15))) // hari 35
        assertNull(faseSopDari(mulai, tanggal(2026, 12, 22))) // hari 42: selesai
        assertNull(faseSopDari(null, tanggal(2026, 11, 23)))
    }

    @Test
    fun tanggalFase_dihitungDariHariAwal() {
        val mulai = tanggal(2026, 11, 10)
        assertEquals("2026-11-10", format(tanggalFase(mulai, FaseSop.TELUR_LARVA)))
        assertEquals("2026-11-23", format(tanggalFase(mulai, FaseSop.PEMBESARAN)))
        assertEquals("2026-12-01", format(tanggalFase(mulai, FaseSop.PREPUPA)))
        assertEquals("2026-12-08", format(tanggalFase(mulai, FaseSop.PUPA)))
        assertEquals("2026-12-15", format(tanggalFase(mulai, FaseSop.LALAT_BSF)))
    }

    @Test
    fun umurTidakPernahNegatif() {
        // Tanggal mulai di masa depan tetap dianggap hari ke-0 (bukan negatif).
        val besok = tanggal(2026, 11, 24)
        assertEquals(FaseSop.TELUR_LARVA, faseSopDari(besok, tanggal(2026, 11, 23)))
    }
}
