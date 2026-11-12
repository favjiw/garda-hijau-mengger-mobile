package com.codelabs.gardahijau.ui.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.codelabs.gardahijau.R
import com.codelabs.gardahijau.ui.util.formatKg

/**
 * Grafik batang 7 hari di beranda Sampah (Figma: "Grafik 7 hari").
 * Batang biasa berwarna tunas, batang [highlightIndex] (hari ini) berwarna kuning terpal.
 * Angka ditampilkan di atas batang hari ini dan batang tertinggi.
 */
class WeeklyBarChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {

    data class Bar(val label: String, val value: Double)

    private var bars: List<Bar> = emptyList()
    private var highlightIndex: Int = -1

    private val regular: Typeface? = safeFont(R.font.atkinson_regular)
    private val bold: Typeface? = safeFont(R.font.atkinson_bold)

    private val barPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.ghm_tunas_100)
    }
    private val highlightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.ghm_terpal_400)
    }
    private val emptyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.ghm_garis)
    }
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.ghm_tinta_redup)
        textSize = sp(15f)
        textAlign = Paint.Align.CENTER
        typeface = regular
    }
    private val labelStrongPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.ghm_tinta)
        textSize = sp(15f)
        textAlign = Paint.Align.CENTER
        typeface = bold
    }

    private val rect = RectF()
    private val path = Path()
    private val radiiTop = dp(8f)
    private val radiiBottom = dp(3f)

    fun setData(newBars: List<Bar>, highlight: Int) {
        bars = newBars
        highlightIndex = highlight
        contentDescription = newBars.joinToString(", ") { "${it.label} ${formatKg(it.value)} kilogram" }
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredHeight = dp(190f).toInt()
        val height = resolveSize(desiredHeight, heightMeasureSpec)
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), height)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (bars.isEmpty()) return

        val gap = dp(10f)
        val valueArea = dp(24f)
        val labelArea = dp(28f)
        val chartTop = paddingTop + valueArea
        val chartBottom = height - paddingBottom - labelArea
        val chartHeight = (chartBottom - chartTop).coerceAtLeast(1f)
        val usableWidth = width - paddingLeft - paddingRight
        val barWidth = (usableWidth - gap * (bars.size - 1)) / bars.size

        val max = bars.maxOf { it.value }.takeIf { it > 0 } ?: 1.0
        val maxIndex = bars.indices.maxByOrNull { bars[it].value } ?: -1

        bars.forEachIndexed { index, bar ->
            val left = paddingLeft + index * (barWidth + gap)
            val centerX = left + barWidth / 2
            val isHighlight = index == highlightIndex
            val barHeight = if (bar.value > 0) {
                (bar.value / max * chartHeight).toFloat().coerceAtLeast(dp(6f))
            } else {
                dp(4f)
            }
            val top = chartBottom - barHeight

            rect.set(left, top, left + barWidth, chartBottom)
            path.reset()
            path.addRoundRect(
                rect,
                floatArrayOf(radiiTop, radiiTop, radiiTop, radiiTop, radiiBottom, radiiBottom, radiiBottom, radiiBottom),
                Path.Direction.CW,
            )
            val paint = when {
                bar.value <= 0 -> emptyPaint
                isHighlight -> highlightPaint
                else -> barPaint
            }
            canvas.drawPath(path, paint)

            if (bar.value > 0 && (isHighlight || index == maxIndex)) {
                val valuePaint = if (isHighlight) labelStrongPaint else labelPaint
                canvas.drawText(formatKg(bar.value), centerX, top - dp(8f), valuePaint)
            }

            val textPaint = if (isHighlight) labelStrongPaint else labelPaint
            canvas.drawText(bar.label, centerX, height - paddingBottom - dp(6f), textPaint)
        }
    }

    private fun safeFont(id: Int): Typeface? =
        try {
            ResourcesCompat.getFont(context, id)
        } catch (e: Exception) {
            null
        }

    private fun dp(value: Float) =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, resources.displayMetrics)

    private fun sp(value: Float) =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, value, resources.displayMetrics)
}
