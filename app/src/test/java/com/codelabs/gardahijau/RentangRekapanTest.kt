package com.codelabs.gardahijau

import com.codelabs.gardahijau.ui.pencatatan_history.RentangRekapan
import org.junit.Assert.assertEquals
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/** Uji batas rentang rekapan sampah: 7 hari, 30 hari, bulan ini, dan kustom. */
class RentangRekapanTest {

    private fun tanggal(y: Int, m: Int, d: Int): Date =
        Calendar.getInstance().apply {
            clear()
            set(y, m - 1, d, 12, 0, 0)
        }.time

    private fun format(date: Date): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(date)

    @Test
    fun tujuhHari_mulaiEnamHariSebelumHariIni() {
        val (awal, akhir) = RentangRekapan(RentangRekapan.Jenis.TUJUH_HARI)
            .batas(tanggal(2026, 11, 23))
        assertEquals("2026-11-17", format(awal))
        assertEquals("2026-11-23", format(akhir))
    }

    @Test
    fun tigaPuluhHari_mulaiDuaPuluhSembilanHariSebelumHariIni() {
        val (awal, akhir) = RentangRekapan(RentangRekapan.Jenis.TIGA_PULUH_HARI)
            .batas(tanggal(2026, 11, 23))
        assertEquals("2026-10-25", format(awal))
        assertEquals("2026-11-23", format(akhir))
    }

    @Test
    fun bulanIni_mulaiTanggalSatu() {
        val (awal, akhir) = RentangRekapan(RentangRekapan.Jenis.BULAN_INI)
            .batas(tanggal(2026, 11, 23))
        assertEquals("2026-11-01", format(awal))
        assertEquals("2026-11-23", format(akhir))
    }

    @Test
    fun kustom_terbalikDibetulkan() {
        val (awal, akhir) = RentangRekapan(
            RentangRekapan.Jenis.KUSTOM,
            dari = tanggal(2026, 11, 20),
            sampai = tanggal(2026, 11, 10),
        ).batas(tanggal(2026, 11, 23))
        assertEquals("2026-11-10", format(awal))
        assertEquals("2026-11-20", format(akhir))
    }

    @Test
    fun batas_selaluAwalDanAkhirHari() {
        val (awal, akhir) = RentangRekapan(RentangRekapan.Jenis.TUJUH_HARI)
            .batas(tanggal(2026, 11, 23))
        val jamAwal = Calendar.getInstance().apply { time = awal }
        val jamAkhir = Calendar.getInstance().apply { time = akhir }
        assertEquals(0, jamAwal.get(Calendar.HOUR_OF_DAY))
        assertEquals(0, jamAwal.get(Calendar.MINUTE))
        assertEquals(23, jamAkhir.get(Calendar.HOUR_OF_DAY))
        assertEquals(59, jamAkhir.get(Calendar.MINUTE))
    }
}
