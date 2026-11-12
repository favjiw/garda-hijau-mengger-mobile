package com.codelabs.gardahijau.ui.prediksi.tambah_fase

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
import com.codelabs.gardahijau.databinding.ActivityTambahFaseBinding
import com.codelabs.gardahijau.model.remote.network.ApiResponse
import com.codelabs.gardahijau.ui.util.LOCALE_ID
import com.codelabs.gardahijau.ui.util.applySystemBarPadding
import com.codelabs.gardahijau.ui.util.hariSejak
import com.codelabs.gardahijau.ui.util.parseServerDate
import com.codelabs.gardahijau.ui.widget.TahapSiklus
import com.codelabs.gardahijau.ui.widget.tampilkan
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@AndroidEntryPoint
class TambahFaseActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTambahFaseBinding
    private val viewModel: TambahFaseViewModel by viewModels()

    /** Tanggal masuk fase besar. Awalnya: hari ini. */
    private val tanggalDipilih: Calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
        )
        binding = ActivityTambahFaseBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        binding.layoutBilahAtas.applySystemBarPadding(top = true)
        binding.layoutFooter.applySystemBarPadding(bottom = true, includeIme = true)

        savedInstanceState?.getLong(KEY_TANGGAL, -1L)?.takeIf { it > 0 }?.let {
            tanggalDipilih.timeInMillis = it
        }

        binding.btnKembali.setOnClickListener { finish() }
        tampilkanKartuSiklus()
        setupDatePicker()
        setupSaveButton()
        observeAddFase()
        tampilkanTanggal()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putLong(KEY_TANGGAL, tanggalDipilih.timeInMillis)
    }

    /** Kartu di atas: siklus mana, hari ke berapa, dan pita di tahap Besar (yang sedang dicatat). */
    private fun tampilkanKartuSiklus() {
        val siklusId = intent.getIntExtra("SIKLUS_ID", -1)
        binding.tvJudulSiklus.text = getString(R.string.ghm_siklus_ke, siklusId)
        parseServerDate(intent.getStringExtra("TANGGAL_MULAI"))?.let { mulai ->
            val hari = hariSejak(mulai)
            binding.tvHariKe.visibility = View.VISIBLE
            binding.tvHariKe.text = if (hari <= 0) {
                getString(R.string.ghm_mulai_hari_ini)
            } else {
                getString(R.string.ghm_hari_ke, hari)
            }
        }
        binding.pita.tampilkan(TahapSiklus.BESAR)
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
        binding.inputTanggalPembesaran.setOnClickListener(pilihTanggal)
        binding.edtTanggalPembesaran.setEndIconOnClickListener(pilihTanggal)
    }

    private fun tampilkanTanggal() {
        binding.inputTanggalPembesaran.setText(
            SimpleDateFormat("EEEE, d MMMM yyyy", LOCALE_ID).format(tanggalDipilih.time)
        )
    }

    private fun setupSaveButton() {
        binding.inputJumlahMakanan.doAfterTextChanged { binding.edtJumlahPakan.error = null }

        binding.btnSimpanFase.setOnClickListener {
            val jumlah = binding.inputJumlahMakanan.text?.toString()?.trim()?.toIntOrNull()
            val catatan = binding.inputKeterangan.text?.toString()?.trim().orEmpty()

            if (jumlah == null || jumlah <= 0) {
                binding.edtJumlahPakan.error = getString(R.string.ghm_galat_jumlah_pakan)
                return@setOnClickListener
            }

            val mode = intent.getStringExtra("MODE") ?: "ADD"
            val siklusId = intent.getIntExtra("SIKLUS_ID", -1)
            if (mode !in listOf("ADD", MODE_HANYA_PANEN) || siklusId == -1) {
                Toast.makeText(this, R.string.ghm_galat_id_siklus, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val telurGram = intent.getIntExtra("JUMLAH_TELUR", 0)
            if (telurGram < 1) {
                // Prediksi panen butuh jumlah telur siklus (gram); tanpa itu server menolak.
                Toast.makeText(this, R.string.ghm_galat_telur_siklus, Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            // Format API sama seperti sebelumnya: yyyy-MM-dd.
            val tanggalApi = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(tanggalDipilih.time)
            viewModel.simpanFaseBesar(
                siklusId = siklusId,
                tanggal = tanggalApi,
                pakanKg = jumlah.toDouble(),
                telurGram = telurGram,
                keterangan = catatan,
                denganFaseBesar = mode != MODE_HANYA_PANEN,
            )
        }
    }

    private fun observeAddFase() {
        viewModel.status.observe(this) { status ->
            when (status) {
                is StatusFaseBesar.MenyimpanFase -> tampilkanMemuat(getString(R.string.ghm_memproses))
                // Prediksi AI bisa ±15 detik: beri tahu pengguna supaya tidak mengira aplikasi macet.
                is StatusFaseBesar.MenghitungPerkiraan -> tampilkanMemuat(getString(R.string.ghm_menghitung_perkiraan))
                is StatusFaseBesar.Selesai -> {
                    Toast.makeText(this, R.string.ghm_fase_tersimpan, Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK)
                    finish()
                }
                is StatusFaseBesar.PerkiraanGagal -> {
                    if (intent.getStringExtra("MODE") == MODE_HANYA_PANEN) {
                        // Tidak ada yang tersimpan; biarkan form terbuka supaya bisa dicoba lagi.
                        selesaiMemuat()
                        Toast.makeText(this, status.pesan, Toast.LENGTH_LONG).show()
                        return@observe
                    }
                    // Fase besar sudah tersimpan; perkiraan bisa dihitung ulang di Detail siklus.
                    Toast.makeText(
                        this,
                        getString(R.string.ghm_fase_tersimpan_perkiraan_gagal, status.pesan),
                        Toast.LENGTH_LONG
                    ).show()
                    setResult(RESULT_OK)
                    finish()
                }
                is StatusFaseBesar.Gagal -> {
                    selesaiMemuat()
                    Toast.makeText(this, getString(R.string.ghm_galat_simpan, status.pesan), Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun tampilkanMemuat(teks: String) {
        binding.btnSimpanFase.isEnabled = false
        binding.btnSimpanFase.text = teks
        binding.btnKembali.isEnabled = false
    }

    private fun selesaiMemuat() {
        binding.btnSimpanFase.isEnabled = true
        binding.btnSimpanFase.text = getString(R.string.ghm_tombol_simpan_lihat_perkiraan)
        binding.btnKembali.isEnabled = true
    }

    companion object {
        private const val KEY_TANGGAL = "tanggal_dipilih"

        /** Fase besar sudah tercatat; form hanya mengirim fase PANEN untuk menghitung perkiraan. */
        const val MODE_HANYA_PANEN = "PANEN"
    }
}
