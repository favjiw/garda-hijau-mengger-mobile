package com.codelabs.gardahijau.ui.prediksi.tambah_siklus

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import com.codelabs.gardahijau.R
import com.codelabs.gardahijau.databinding.ActivityTambahSiklusBinding
import com.codelabs.gardahijau.model.remote.network.ApiResponse
import com.codelabs.gardahijau.ui.util.LOCALE_ID
import com.codelabs.gardahijau.ui.util.applySystemBarPadding
import com.google.android.material.button.MaterialButton
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@AndroidEntryPoint
class TambahSiklusActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTambahSiklusBinding
    private val viewModel: TambahSiklusViewModel by viewModels()

    /** Tanggal mulai = hari ini (otomatis). Fase berpindah sendiri sesuai umur siklus. */
    private val tanggalDipilih: Calendar = Calendar.getInstance()
    /** Teks media persis seperti dari server (dikirim apa adanya ke POST /api/siklus). */
    private var mediaDipilih: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
        )
        binding = ActivityTambahSiklusBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        binding.layoutBilahAtas.applySystemBarPadding(top = true)
        binding.layoutFooter.applySystemBarPadding(bottom = true, includeIme = true)

        savedInstanceState?.getString(KEY_MEDIA)?.let { mediaDipilih = it }

        binding.btnKembali.setOnClickListener { finish() }
        setupPilihanMedia()
        setupSaveButton()
        observeAddSiklus()
        tampilkanTanggal()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mediaDipilih?.let { outState.putString(KEY_MEDIA, it) }
    }

    private fun tampilkanTanggal() {
        binding.inputTanggal.setText(
            SimpleDateFormat("EEEE, d MMMM yyyy", LOCALE_ID).format(tanggalDipilih.time)
        )
    }

    /** Pilihan media telur dari server. Hanya satu yang aktif. */
    private fun setupPilihanMedia() {
        binding.inputJumlahTelur.doAfterTextChanged { binding.edtJumlahTelur.error = null }
        binding.btnMuatUlangMedia.setOnClickListener { viewModel.muatOpsiMedia() }

        viewModel.opsiMedia.observe(this) { hasil ->
            binding.tvMediaMemuat.visibility = if (hasil is ApiResponse.Empty) View.VISIBLE else View.GONE
            binding.layoutMediaGagal.visibility = if (hasil is ApiResponse.Error) View.VISIBLE else View.GONE
            when (hasil) {
                is ApiResponse.Success -> tampilkanPilihanMedia(hasil.data)
                is ApiResponse.Error -> {
                    binding.layoutPilihanMedia.removeAllViews()
                    binding.tvMediaGagal.text = hasil.errorMessage
                }
                ApiResponse.Empty -> binding.layoutPilihanMedia.removeAllViews()
            }
        }
        if (viewModel.opsiMedia.value !is ApiResponse.Success) viewModel.muatOpsiMedia()
    }

    private fun tampilkanPilihanMedia(pilihan: List<String>) {
        val wadah = binding.layoutPilihanMedia
        wadah.removeAllViews()
        // Pilihan lama yang tidak ada lagi di server dibuang.
        if (mediaDipilih != null && mediaDipilih !in pilihan) mediaDipilih = null
        val tombol = pilihan.map { media ->
            (layoutInflater.inflate(R.layout.item_pilihan_media, wadah, false) as MaterialButton).apply {
                text = media
                isChecked = media == mediaDipilih
            }
        }
        tombol.forEachIndexed { index, button ->
            button.setOnClickListener {
                tombol.forEach { it.isChecked = it == button }
                mediaDipilih = pilihan[index]
                binding.tvMediaError.visibility = View.GONE
            }
            wadah.addView(button)
        }
    }

    private fun setupSaveButton() {
        binding.btnSimpanSiklus.setOnClickListener {
            val jumlah = binding.inputJumlahTelur.text?.toString()?.trim()?.toIntOrNull()
            val media = mediaDipilih
            val catatan = binding.inputCatatan.text?.toString()?.trim().orEmpty()

            var valid = true
            if (jumlah == null || jumlah <= 0) {
                binding.edtJumlahTelur.error = getString(R.string.ghm_galat_jumlah_telur)
                valid = false
            }
            if (media == null) {
                binding.tvMediaError.visibility = View.VISIBLE
                valid = false
            }
            if (valid && jumlah != null && media != null) {
                // Format API sama seperti sebelumnya: yyyy-MM-dd. Media dikirim persis seperti dari server.
                val tanggalApi = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(tanggalDipilih.time)
                viewModel.addSiklus(tanggalApi, jumlah, media, catatan)
            }
        }
    }

    private fun observeAddSiklus() {
        viewModel.addSiklusResult.observe(this) { response ->
            when (response) {
                is ApiResponse.Empty -> {
                    binding.btnSimpanSiklus.isEnabled = false
                    binding.btnSimpanSiklus.text = getString(R.string.ghm_memproses)
                }
                is ApiResponse.Success -> {
                    Toast.makeText(this, R.string.ghm_siklus_tersimpan, Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK)
                    finish()
                }
                is ApiResponse.Error -> {
                    binding.btnSimpanSiklus.isEnabled = true
                    binding.btnSimpanSiklus.text = getString(R.string.ghm_tombol_mulai_siklus_simpan)
                    Toast.makeText(this, getString(R.string.ghm_galat_simpan, response.errorMessage), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    companion object {
        private const val KEY_MEDIA = "media_dipilih"
    }
}
