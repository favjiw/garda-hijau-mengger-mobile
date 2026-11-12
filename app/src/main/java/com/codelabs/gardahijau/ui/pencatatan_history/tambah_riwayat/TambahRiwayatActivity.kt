package com.codelabs.gardahijau.ui.pencatatan_history.tambah_riwayat

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
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
import com.codelabs.gardahijau.databinding.ActivityTambahRiwayatBinding
import com.codelabs.gardahijau.ui.util.LOCALE_ID
import com.codelabs.gardahijau.ui.util.applySystemBarPadding
import dagger.hilt.android.AndroidEntryPoint
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

@AndroidEntryPoint
class TambahRiwayatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTambahRiwayatBinding
    private val viewModel: TambahRiwayatViewModel by viewModels()

    /** Tanggal & pukul yang dipilih (zona waktu HP). Awalnya: sekarang. */
    private val waktuDipilih: Calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Latar kertas terang di belakang status bar -> ikon status bar gelap.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
        )
        binding = ActivityTambahRiwayatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        binding.layoutBilahAtas.applySystemBarPadding(top = true)
        binding.layoutFooter.applySystemBarPadding(bottom = true, includeIme = true)

        savedInstanceState?.getLong(KEY_WAKTU, -1L)?.takeIf { it > 0 }?.let {
            waktuDipilih.timeInMillis = it
        }

        setupToolbar()
        setupDatePicker()
        setupBerat()
        setupObservers()
        setupClickListeners()
        tampilkanWaktu()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putLong(KEY_WAKTU, waktuDipilih.timeInMillis)
    }

    private fun setupToolbar() {
        binding.btnKembali.setOnClickListener {
            finish()
        }
    }

    private fun setupDatePicker() {
        val pilihTanggal = View.OnClickListener {
            DatePickerDialog(
                this,
                { _, year, month, day ->
                    waktuDipilih.set(year, month, day)
                    tampilkanWaktu()
                },
                waktuDipilih.get(Calendar.YEAR),
                waktuDipilih.get(Calendar.MONTH),
                waktuDipilih.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
        binding.inputTanggalRiwayat.setOnClickListener(pilihTanggal)
        binding.edtTanggalWaktu.setEndIconOnClickListener(pilihTanggal)

        val pilihPukul = View.OnClickListener {
            TimePickerDialog(
                this,
                { _, hour, minute ->
                    waktuDipilih.set(Calendar.HOUR_OF_DAY, hour)
                    waktuDipilih.set(Calendar.MINUTE, minute)
                    tampilkanWaktu()
                },
                waktuDipilih.get(Calendar.HOUR_OF_DAY),
                waktuDipilih.get(Calendar.MINUTE),
                true
            ).show()
        }
        binding.inputPukul.setOnClickListener(pilihPukul)
        binding.edtPukul.setEndIconOnClickListener(pilihPukul)
    }

    private fun tampilkanWaktu() {
        binding.inputTanggalRiwayat.setText(
            SimpleDateFormat("d MMM yyyy", LOCALE_ID).format(waktuDipilih.time)
        )
        binding.inputPukul.setText(
            SimpleDateFormat("HH.mm", LOCALE_ID).format(waktuDipilih.time)
        )
    }

    /** Tanggal untuk API (ISO-8601, UTC) — sama dengan yang dibaca parseServerDate. */
    private fun tanggalUntukApi(): String {
        val apiFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        return apiFormat.format(waktuDipilih.time)
    }

    private fun setupBerat() {
        binding.btnKurang.setOnClickListener { ubahBerat(-1) }
        binding.btnTambah.setOnClickListener { ubahBerat(1) }
        binding.inputTotalSampah.doAfterTextChanged { teks ->
            binding.tvGalatBerat.visibility = View.GONE
            val isi = teks?.toString().orEmpty()
            // "055" -> "55" (nol di depan tidak berarti), tapi "0,5" tetap.
            val tanpaNolDepan = isi.replaceFirst(Regex("""^0+(?=\d)"""), "")
            if (tanpaNolDepan != isi) {
                binding.inputTotalSampah.setText(tanpaNolDepan)
                binding.inputTotalSampah.setSelection(tanpaNolDepan.length)
                return@doAfterTextChanged
            }
            // Angka panjang memakai ukuran Angka/Sedang supaya tidak menabrak tombol - dan +.
            binding.inputTotalSampah.setTextAppearance(
                if (isi.length > 4) R.style.Text_GHM_AngkaSedang else R.style.Text_GHM_AngkaBesar
            )
        }
    }

    /** Berat dari isian. Koma (keyboard Indonesia) dianggap titik desimal. */
    private fun bacaBerat(): Double? =
        binding.inputTotalSampah.text?.toString()?.trim()?.replace(',', '.')?.toDoubleOrNull()

    private fun ubahBerat(langkahKg: Int) {
        val baru = ((bacaBerat() ?: 0.0) + langkahKg).coerceAtLeast(0.0)
        // Tulis angka polos ("12.5", "230") supaya tetap bisa dibaca toDouble.
        val teks = BigDecimal.valueOf(baru).stripTrailingZeros().toPlainString()
        binding.inputTotalSampah.setText(teks)
        binding.inputTotalSampah.setSelection(teks.length)
    }

    private fun setupObservers() {
        viewModel.isLoading.observe(this) { isLoading ->
            binding.btnSimpan.isEnabled = !isLoading
            binding.btnSimpan.text = getString(
                if (isLoading) R.string.ghm_memproses else R.string.ghm_tombol_simpan_catatan
            )
        }

        viewModel.addHistoryResult.observe(this) { response ->
            Toast.makeText(this, response.message ?: getString(R.string.ghm_catatan_tersimpan), Toast.LENGTH_SHORT).show()
            setResult(Activity.RESULT_OK)
            finish()
        }

        viewModel.errorMessage.observe(this) { error ->
            Toast.makeText(this, getString(R.string.ghm_galat_simpan, error), Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupClickListeners() {
        binding.btnSimpan.setOnClickListener {
            val totalSampah = bacaBerat() ?: 0.0
            val catatan = binding.inputKeterangan.text?.toString()?.trim().orEmpty()

            if (validateInput(totalSampah)) {
                viewModel.addHistoryPencacahan(tanggalUntukApi(), totalSampah, catatan)
            }
        }
    }

    private fun validateInput(totalSampah: Double): Boolean {
        if (totalSampah <= 0) {
            binding.tvGalatBerat.visibility = View.VISIBLE
            binding.inputTotalSampah.requestFocus()
            return false
        }
        return true
    }

    companion object {
        private const val KEY_WAKTU = "waktu_dipilih"
    }
}
