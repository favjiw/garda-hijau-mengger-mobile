package com.codelabs.gardahijau.ui.widget

import android.content.res.ColorStateList
import android.view.View
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.codelabs.gardahijau.R
import com.codelabs.gardahijau.databinding.ViewGhmPitaSiklusBinding

/**
 * Tahap siklus yang bisa dibaca dari daftar fase.
 * Fase PANEN dibuat otomatis bersama PEMBESARAN (lihat TambahFaseViewModel), jadi tidak
 * menandakan larva siap panen. Karena itu tahap "Panen" di pita tidak pernah aktif.
 */
enum class TahapSiklus { TELUR, TETAS, BESAR }

/** Tahap dari kolom `jenis` semua fase satu siklus. */
fun tahapDariFase(jenisFase: List<String>): TahapSiklus {
    val jenis = jenisFase.map { it.uppercase() }
    return when {
        "PEMBESARAN" in jenis -> TahapSiklus.BESAR
        "PENETASAN" in jenis -> TahapSiklus.TETAS
        else -> TahapSiklus.TELUR
    }
}

/**
 * Mewarnai komponen "Pita siklus" (view_ghm_pita_siklus.xml).
 * Ruas sebelum tahap sekarang: hijau. Tahap sekarang: kuning + label tebal. Sesudahnya: abu.
 */
fun ViewGhmPitaSiklusBinding.tampilkan(tahap: TahapSiklus) {
    val ruas = listOf(
        ruasTelur to labelTelur,
        ruasTetas to labelTetas,
        ruasBesar to labelBesar,
        ruasPanen to labelPanen,
    )
    val aktif = tahap.ordinal
    ruas.forEachIndexed { index, (batang, label) ->
        when {
            index < aktif -> setRuas(batang, label, R.color.ghm_daun_700, R.color.ghm_tinta, tebal = false)
            index == aktif -> setRuas(batang, label, R.color.ghm_terpal_400, R.color.ghm_tinta, tebal = true)
            else -> setRuas(batang, label, R.color.ghm_garis, R.color.ghm_tinta_redup, tebal = false)
        }
        label.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
    }
    root.contentDescription = root.context.getString(R.string.ghm_tahap_sekarang, ruas[aktif].second.text)
    root.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
}

private fun setRuas(batang: View, label: TextView, @ColorRes warnaBatang: Int, @ColorRes warnaLabel: Int, tebal: Boolean) {
    val context = batang.context
    batang.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, warnaBatang))
    label.setTextColor(ContextCompat.getColor(context, warnaLabel))
    label.typeface = ResourcesCompat.getFont(
        context,
        if (tebal) R.font.atkinson_bold else R.font.atkinson_regular
    )
}
