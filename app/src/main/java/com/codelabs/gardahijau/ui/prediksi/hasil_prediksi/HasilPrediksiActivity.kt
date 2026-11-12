package com.codelabs.gardahijau.ui.prediksi.hasil_prediksi

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.codelabs.gardahijau.R
import com.codelabs.gardahijau.databinding.ActivityHasilPrediksiBinding
import com.codelabs.gardahijau.ui.util.LOCALE_ID
import com.codelabs.gardahijau.ui.util.applySystemBarPadding
import com.codelabs.gardahijau.ui.util.formatKg
import dagger.hilt.android.AndroidEntryPoint

/**
 * Perkiraan panen satu siklus. Semua data datang lewat extra dari DetailSiklusActivity:
 * HASIL_KG (prediksiPanen.hasilKg), SIKLUS_ID, JUMLAH_TELUR, MEDIA_TELUR, PAKAN_KG.
 */
@AndroidEntryPoint
class HasilPrediksiActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHasilPrediksiBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
        )
        binding = ActivityHasilPrediksiBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        binding.layoutBilahAtas.applySystemBarPadding(top = true)
        binding.layoutFooter.applySystemBarPadding(bottom = true)

        binding.btnKembali.setOnClickListener { finish() }
        binding.btnKembaliKeSiklus.setOnClickListener { finish() }
        displayHasilPrediksi()
        displayDihitungDari()
    }

    private fun displayHasilPrediksi() {
        val siklusId = intent.getIntExtra("SIKLUS_ID", -1)
        binding.tvPredictionTitle.text = if (siklusId != -1) {
            getString(R.string.ghm_siklus_diperkirakan, siklusId)
        } else {
            getString(R.string.ghm_perkiraan_diperkirakan)
        }

        val hasilKg = intent.getStringExtra("HASIL_KG")?.trim()?.toDoubleOrNull()
        if (hasilKg != null && hasilKg > 0) {
            binding.layoutHasil.visibility = View.VISIBLE
            binding.tvBelumAda.visibility = View.GONE
            binding.tvPredictionResult.text = formatKg(hasilKg)
            binding.layoutHasil.contentDescription =
                "${binding.tvPredictionResult.text} ${getString(R.string.ghm_satuan_kg_maggot)}"
        } else {
            binding.layoutHasil.visibility = View.GONE
            binding.tvBelumAda.visibility = View.VISIBLE
        }
    }

    /** Kartu "Dihitung dari". Baris tanpa data disembunyikan. */
    private fun displayDihitungDari() {
        val telur = intent.getStringExtra("JUMLAH_TELUR")?.takeIf { it.isNotBlank() }
        val media = intent.getStringExtra("MEDIA_TELUR")?.takeIf { it.isNotBlank() }
        val pakan = intent.getDoubleExtra("PAKAN_KG", 0.0).takeIf { it > 0 }

        binding.barisTelur.visibility = if (telur == null) View.GONE else View.VISIBLE
        binding.tvNilaiTelur.text = telur?.let { getString(R.string.ghm_nilai_gram, it) }

        binding.barisMedia.visibility = if (media == null) View.GONE else View.VISIBLE
        // "Kotoran Ternak" -> "Kotoran ternak" (huruf kalimat)
        binding.tvNilaiMedia.text = media?.lowercase(LOCALE_ID)?.replaceFirstChar { it.titlecase(LOCALE_ID) }

        binding.barisPakan.visibility = if (pakan == null) View.GONE else View.VISIBLE
        binding.tvNilaiPakan.text = pakan?.let { getString(R.string.ghm_nilai_kg, formatKg(it)) }

        binding.cardDihitungDari.visibility =
            if (telur == null && media == null && pakan == null) View.GONE else View.VISIBLE
    }
}
