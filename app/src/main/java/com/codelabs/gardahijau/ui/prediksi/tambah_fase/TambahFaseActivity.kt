package com.codelabs.gardahijau.ui.prediksi.tambah_fase

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.codelabs.gardahijau.databinding.ActivityTambahFaseBinding
import com.codelabs.gardahijau.model.remote.network.ApiResponse
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.toString

@AndroidEntryPoint
class TambahFaseActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTambahFaseBinding
    private val viewModel: TambahFaseViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTambahFaseBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        setupToolbar()
        setupDatePicker()
        setupSaveButton()
        observeAddFase()
    }

    private fun setupToolbar() {
        binding.topAppBar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupDatePicker() {
        binding.inputTanggalPembesaran.setOnClickListener {
            val calendar = Calendar.getInstance()
            val datePicker = DatePickerDialog(
                this,
                { _, year, month, day ->
                    val selectedDate = Calendar.getInstance()
                    selectedDate.set(year, month, day)

                    // Format untuk tampilan
                    val displayFormat = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
                    binding.inputTanggalPembesaran.setText(displayFormat.format(selectedDate.time))

                    // Simpan format API (yyyy-MM-dd) sebagai tag
                    val apiFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    binding.inputTanggalPembesaran.tag = apiFormat.format(selectedDate.time)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePicker.show()
        }
    }

    private fun setupSaveButton() {
        binding.btnSimpanFase.setOnClickListener {
            val tanggalDisplay = binding.inputTanggalPembesaran.text.toString()
            val tanggal = binding.inputTanggalPembesaran.tag?.toString() ?: ""
            val jumlahMakanan = binding.inputJumlahMakanan.text.toString()
            val catatan = binding.inputKeterangan.text.toString()

            if (tanggalDisplay.isEmpty()) {
                Toast.makeText(this, "Harap isi tanggal pembesaran", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (jumlahMakanan.isEmpty()) {
                Toast.makeText(this, "Harap isi jumlah makanan", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val mode = intent.getStringExtra("MODE") ?: "ADD"
            val siklusId = intent.getIntExtra("SIKLUS_ID", -1)

            if (mode == "ADD" && siklusId != -1) {
                try {
                    val jumlah = jumlahMakanan.toInt()

                    // Gunakan method baru yang menambah kedua fase
                    viewModel.addFasePembesaranWithPanen(
                        siklusId = siklusId,
                        tanggalPembesaran = tanggal,
                        jumlahMakanan = jumlah,
                        catatan = catatan
                    )
                } catch (e: NumberFormatException) {
                    Toast.makeText(this, "Jumlah makanan harus berupa angka", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Data tidak valid", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun observeAddFase() {
        viewModel.addFaseResult.observe(this) { response ->
            when (response) {
                is ApiResponse.Empty -> {
                    binding.btnSimpanFase.isEnabled = false
                    binding.btnSimpanFase.text = "Menyimpan..."
                }
                is ApiResponse.Success -> {
                    binding.btnSimpanFase.isEnabled = true
                    binding.btnSimpanFase.text = "Simpan Fase"
                    Toast.makeText(this, "Fase berhasil ditambahkan", Toast.LENGTH_SHORT).show()

                    setResult(RESULT_OK)
                    finish()
                }
                is ApiResponse.Error -> {
                    binding.btnSimpanFase.isEnabled = true
                    binding.btnSimpanFase.text = "Simpan Fase"
                    Toast.makeText(this, response.errorMessage, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
