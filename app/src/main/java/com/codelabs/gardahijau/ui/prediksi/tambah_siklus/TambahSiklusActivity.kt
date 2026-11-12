package com.codelabs.gardahijau.ui.prediksi.tambah_siklus

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.codelabs.gardahijau.R
import com.codelabs.gardahijau.databinding.ActivityTambahSiklusBinding
import com.codelabs.gardahijau.model.remote.network.ApiResponse
import com.codelabs.gardahijau.ui.MainActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@AndroidEntryPoint
class TambahSiklusActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTambahSiklusBinding
    private val viewModel: TambahSiklusViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityTambahSiklusBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        setupToolbar()
        setupDatePicker()
        setupDropdownMedia()
        setupSaveButton()
        observeAddSiklus()
    }

    private fun setupToolbar() {
        binding.topAppBar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupDatePicker() {
        binding.inputTanggal.setOnClickListener {
            val calendar = Calendar.getInstance()
            val datePicker = DatePickerDialog(
                this,
                { _, year, month, day ->
                    val selectedDate = Calendar.getInstance()
                    selectedDate.set(year, month, day)

                    // Format untuk tampilan
                    val displayFormat = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
                    binding.inputTanggal.setText(displayFormat.format(selectedDate.time))

                    // Simpan format API (yyyy-MM-dd) sebagai tag
                    val apiFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    binding.inputTanggal.tag = apiFormat.format(selectedDate.time)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePicker.show()
        }
    }

    private fun setupDropdownMedia() {
        val options = listOf("Serbuk Kelapa", "Tanah", "Pasir", "Campuran")
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, options)
        binding.inputMediaTelur.setAdapter(adapter)
    }

    private fun setupSaveButton() {
        binding.btnSimpanSiklus.setOnClickListener {
            val tanggalDisplay = binding.inputTanggal.text.toString()
            val tanggalApi = binding.inputTanggal.tag?.toString() ?: ""
            val jumlahTelur = binding.inputJumlahTelur.text.toString()
            val media = binding.inputMediaTelur.text.toString()
            val catatan = binding.inputCatatan.text.toString()

            if (tanggalDisplay.isEmpty() || jumlahTelur.isEmpty() || media.isEmpty()) {
                Toast.makeText(this, "Harap isi semua data wajib", Toast.LENGTH_SHORT).show()
            } else {
                try {
                    val jumlah = jumlahTelur.toInt()
                    viewModel.addSiklus(tanggalApi, jumlah, media, catatan)
                } catch (e: NumberFormatException) {
                    Toast.makeText(this, "Jumlah telur harus berupa angka", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun observeAddSiklus() {
        viewModel.addSiklusResult.observe(this) { response ->
            when (response) {
                is ApiResponse.Empty -> {
                    binding.btnSimpanSiklus.isEnabled = false
                    binding.btnSimpanSiklus.text = "Menyimpan..."
                }
                is ApiResponse.Success -> {
                    binding.btnSimpanSiklus.isEnabled = true
                    binding.btnSimpanSiklus.text = "Simpan Siklus"
                    Toast.makeText(this, "Siklus berhasil disimpan", Toast.LENGTH_SHORT).show()

                    setResult(RESULT_OK)
                    finish()
                }
                is ApiResponse.Error -> {
                    binding.btnSimpanSiklus.isEnabled = true
                    binding.btnSimpanSiklus.text = "Simpan Siklus"
                    Toast.makeText(this, response.errorMessage, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

}
