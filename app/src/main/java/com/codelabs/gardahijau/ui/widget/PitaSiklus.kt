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
 * Mewarnai komponen "Pita siklus" (view_ghm_pita_siklus.xml) dengan 5 fase SOP.
 * Ruas sebelum fase sekarang: hijau. Fase sekarang: kuning + label tebal. Sesudahnya: abu.
 * [fase] null berarti siklus sudah lewat hari ke-41: semua ruas hijau (selesai).
 */
fun ViewGhmPitaSiklusBinding.tampilkan(fase: FaseSop?) {
    val ruas = listOf(
        ruasTelur to labelTelur,
        ruasBesar to labelBesar,
        ruasPrepupa to labelPrepupa,
        ruasPupa to labelPupa,
        ruasLalat to labelLalat,
    )
    val aktif = fase?.ordinal ?: ruas.size
    ruas.forEachIndexed { index, (batang, label) ->
        when {
            index < aktif -> setRuas(batang, label, R.color.ghm_daun_700, R.color.ghm_tinta, tebal = false)
            index == aktif -> setRuas(batang, label, R.color.ghm_terpal_400, R.color.ghm_tinta, tebal = true)
            else -> setRuas(batang, label, R.color.ghm_garis, R.color.ghm_tinta_redup, tebal = false)
        }
        label.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
    }
    root.contentDescription = if (fase == null) {
        root.context.getString(R.string.ghm_sop_selesai)
    } else {
        root.context.getString(R.string.ghm_tahap_sekarang, root.context.getString(fase.namaRes))
    }
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
