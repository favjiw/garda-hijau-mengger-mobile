package com.codelabs.gardahijau.ui.pencatatan_history.tambah_riwayat

import android.app.Activity
import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.codelabs.gardahijau.databinding.ActivityTambahRiwayatBinding
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.text.format
import kotlin.text.get
import kotlin.text.set

@AndroidEntryPoint
class TambahRiwayatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTambahRiwayatBinding
    private val viewModel: TambahRiwayatViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityTambahRiwayatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        setupToolbar()
        setupDatePicker()
        setupObservers()
        setupClickListeners()
    }

    private fun setupToolbar() {
        binding.topAppBar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupDatePicker() {
        binding.inputTanggalRiwayat.setOnClickListener {
            val calendar = Calendar.getInstance()
            val datePicker = DatePickerDialog(
                this,
                { _, year, month, day ->
                    val selectedDate = Calendar.getInstance()
                    selectedDate.set(year, month, day)

                    // Format untuk tampilan
                    val displayFormat = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
                    binding.inputTanggalRiwayat.setText(displayFormat.format(selectedDate.time))

                    // Format API dengan waktu saat ini (ISO-8601)
                    val apiFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                    binding.inputTanggalRiwayat.tag = apiFormat.format(selectedDate.time)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePicker.show()
        }
    }


    private fun setupObservers() {
        viewModel.isLoading.observe(this) { isLoading ->
            binding.btnSimpan.isEnabled = !isLoading
            binding.btnSimpan.text = if (isLoading) "Menyimpan..." else "Simpan Penjadwalan"
        }

        viewModel.addHistoryResult.observe(this) { response ->
            Toast.makeText(this, response.message, Toast.LENGTH_SHORT).show()
            setResult(Activity.RESULT_OK)
            finish()
        }

        viewModel.errorMessage.observe(this) { error ->
            Toast.makeText(this, "Error: $error", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupClickListeners() {
        binding.btnSimpan.setOnClickListener {
            val tanggalDisplay = binding.inputTanggalRiwayat.text.toString()
            val tanggalApi = binding.inputTanggalRiwayat.tag?.toString() ?: ""
            val totalSampah = binding.inputTotalSampah.text.toString().toDoubleOrNull() ?: 0.0
            val catatan = binding.inputKeterangan.text.toString()

            if (validateInput(tanggalDisplay, totalSampah, catatan)) {
                viewModel.addHistoryPencacahan(tanggalApi, totalSampah, catatan)
            }
        }
    }

    private fun validateInput(tanggalWaktu: String, totalSampah: Double, catatan: String): Boolean {
        return when {
            tanggalWaktu.isEmpty() -> {
                Toast.makeText(this, "Tanggal waktu tidak boleh kosong", Toast.LENGTH_SHORT).show()
                false
            }
            totalSampah <= 0 -> {
                Toast.makeText(this, "Total sampah harus lebih dari 0", Toast.LENGTH_SHORT).show()
                false
            }
            catatan.isEmpty() -> {
                Toast.makeText(this, "Catatan tidak boleh kosong", Toast.LENGTH_SHORT).show()
                false
            }
            else -> true
        }
    }
}
