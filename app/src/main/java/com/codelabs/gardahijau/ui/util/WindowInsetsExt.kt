package com.codelabs.gardahijau.ui.util

import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding

/**
 * Menambahkan tinggi status bar / navigation bar (dan keyboard bila [includeIme]) ke padding view,
 * di atas padding yang sudah ditulis di XML. Dipakai karena aplikasi berjalan edge-to-edge.
 */
fun View.applySystemBarPadding(
    top: Boolean = false,
    bottom: Boolean = false,
    includeIme: Boolean = false,
) {
    val initialTop = paddingTop
    val initialBottom = paddingBottom
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
        val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        val imeBottom = if (includeIme) insets.getInsets(WindowInsetsCompat.Type.ime()).bottom else 0
        view.updatePadding(
            top = if (top) initialTop + bars.top else view.paddingTop,
            bottom = if (bottom) initialBottom + maxOf(bars.bottom, imeBottom) else view.paddingBottom,
        )
        insets
    }
    ViewCompat.requestApplyInsets(this)
}
