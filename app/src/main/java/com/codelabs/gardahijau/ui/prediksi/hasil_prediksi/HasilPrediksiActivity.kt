package com.codelabs.gardahijau.ui.prediksi.hasil_prediksi

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.codelabs.gardahijau.databinding.ActivityHasilPrediksiBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HasilPrediksiActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHasilPrediksiBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHasilPrediksiBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        setupToolbar()
        displayHasilPrediksi()
    }

    private fun setupToolbar() {
        binding.topAppBar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun displayHasilPrediksi() {
        val faseId = intent.getIntExtra("FASE_ID", -1)
        val prediksiPanen = intent.getStringExtra("PREDIKSI_PANEN") ?: "0"
        val hasilKg = intent.getStringExtra("HASIL_KG") ?: "0"

        val hasilPrediksi = if (hasilKg != "null" && hasilKg.isNotEmpty() && hasilKg != "0") {
            "$hasilKg KG"
        } else if (prediksiPanen != "null" && prediksiPanen.isNotEmpty()) {
            "$prediksiPanen KG"
        } else {
            "Belum tersedia"
        }

        binding.cardHasilPanen.tvPredictionResult.text = hasilPrediksi
    }

}
