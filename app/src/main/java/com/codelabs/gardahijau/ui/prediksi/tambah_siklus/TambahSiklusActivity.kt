package com.codelabs.gardahijau.ui.prediksi.tambah_siklus

import android.app.DatePickerDialog
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
import com.codelabs.gardahijau.utils.enumClass.MediaTelurType
import com.google.android.material.button.MaterialButton
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@AndroidEntryPoint
class TambahSiklusActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTambahSiklusBinding
    private val viewModel: TambahSiklusViewModel by viewModels()

    /** Tanggal mulai yang dipilih. Awalnya: hari ini. */
    private val tanggalDipilih: Calendar = Calendar.getInstance()
    private var mediaDipilih: MediaTelurType? = null

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

        savedInstanceState?.let { state ->
            state.getLong(KEY_TANGGAL, -1L).takeIf { it > 0 }?.let { tanggalDipilih.timeInMillis = it }
            state.getString(KEY_MEDIA)?.let { nama -> mediaDipilih = MediaTelurType.values().find { it.name == nama } }
        }

        binding.btnKembali.setOnClickListener { finish() }
        setupDatePicker()
        setupPilihanMedia()
        setupSaveButton()
        observeAddSiklus()
        tampilkanTanggal()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putLong(KEY_TANGGAL, tanggalDipilih.timeInMillis)
        mediaDipilih?.let { outState.putString(KEY_MEDIA, it.name) }
    }

    private fun setupDatePicker() {
        val pilihTanggal = View.OnClickListener {
            DatePickerDialog(
                this,
                { _, year, month, day ->
                    tanggalDipilih.set(year, month, day)
                    tampilkanTanggal()
                },
                tanggalDipilih.get(Calendar.YEAR),
                tanggalDipilih.get(Calendar.MONTH),
                tanggalDipilih.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
        binding.inputTanggal.setOnClickListener(pilihTanggal)
        binding.edtTanggal.setEndIconOnClickListener(pilihTanggal)
    }

    private fun tampilkanTanggal() {
        binding.inputTanggal.setText(
            SimpleDateFormat("EEEE, d MMMM yyyy", LOCALE_ID).format(tanggalDipilih.time)
        )
    }

    /** Empat pilihan media telur (MediaTelurType). Hanya satu yang aktif. */
    private fun setupPilihanMedia() {
        val pilihan: Map<MaterialButton, MediaTelurType> = mapOf(
            binding.btnMediaDedak to MediaTelurType.DEDAK,
            binding.btnMediaKotoranTernak to MediaTelurType.KOTORAN_TERNAK,
            binding.btnMediaLimbahOrganik to MediaTelurType.LIMBAH_ORGANIK,
            binding.btnMediaCampuran to MediaTelurType.CAMPURAN,
        )
        pilihan.forEach { (button, media) ->
            button.isChecked = media == mediaDipilih
            button.setOnClickListener {
                pilihan.keys.forEach { it.isChecked = it == button }
                mediaDipilih = media
                binding.tvMediaError.visibility = View.GONE
            }
        }
        binding.inputJumlahTelur.doAfterTextChanged { binding.edtJumlahTelur.error = null }
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
                // Format API sama seperti sebelumnya: yyyy-MM-dd. Nilai media dari MediaTelurType.toString().
                val tanggalApi = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(tanggalDipilih.time)
                viewModel.addSiklus(tanggalApi, jumlah, media.toString(), catatan)
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
        private const val KEY_TANGGAL = "tanggal_dipilih"
        private const val KEY_MEDIA = "media_dipilih"
    }
}
