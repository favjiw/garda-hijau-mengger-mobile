package com.codelabs.gardahijau.ui.prediksi.detail_siklus

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.codelabs.gardahijau.R
import com.codelabs.gardahijau.databinding.ActivityDetailSiklusBinding
import com.codelabs.gardahijau.model.remote.network.ApiResponse
import com.codelabs.gardahijau.model.remote.response.prediksi.DataGetFase
import com.codelabs.gardahijau.ui.adapter.BarisFase
import com.codelabs.gardahijau.ui.adapter.DetailSiklusAdapter
import com.codelabs.gardahijau.ui.prediksi.hasil_prediksi.HasilPrediksiActivity
import com.codelabs.gardahijau.ui.prediksi.tambah_fase.TambahFaseActivity
import com.codelabs.gardahijau.ui.util.LOCALE_ID
import com.codelabs.gardahijau.ui.util.applySystemBarPadding
import com.codelabs.gardahijau.ui.util.formatKg
import com.codelabs.gardahijau.ui.util.hariSejak
import com.codelabs.gardahijau.ui.util.parseServerDate
import com.codelabs.gardahijau.ui.widget.FaseSop
import com.codelabs.gardahijau.ui.widget.faseSopDari
import com.codelabs.gardahijau.ui.widget.tampilkan
import com.codelabs.gardahijau.ui.widget.tanggalFase
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class DetailSiklusActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailSiklusBinding
    private val viewModel: DetailSiklusViewModel by viewModels()
    private val detailSiklusAdapter = DetailSiklusAdapter()

    private val siklusId by lazy { intent.getIntExtra("EXTRA_ID", -1) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
        )
        binding = ActivityDetailSiklusBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        binding.layoutBilahAtas.applySystemBarPadding(top = true)
        // Tombol bawah bisa disembunyikan, jadi tinggi navigation bar dipasang di root.
        binding.main.applySystemBarPadding(bottom = true)

        if (siklusId == -1) {
            Toast.makeText(this, R.string.ghm_galat_id_siklus, Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        binding.tvJudul.text = getString(R.string.ghm_siklus_ke, siklusId)
        binding.btnKembali.setOnClickListener { finish() }
        tampilkanHariKe()

        binding.rvSiklus.apply {
            layoutManager = LinearLayoutManager(this@DetailSiklusActivity)
            adapter = detailSiklusAdapter
        }
        observeFaseData()
        observePerkiraan()
    }

    /** Dimuat ulang setiap kembali ke layar ini (mis. setelah mencatat fase besar). */
    override fun onResume() {
        super.onResume()
        if (siklusId != -1) viewModel.getFase(siklusId)
    }

    private fun tanggalMulai(): Date? = parseServerDate(intent.getStringExtra("EXTRA_TANGGAL_MULAI"))

    private fun tampilkanHariKe() {
        val mulai = tanggalMulai() ?: return
        val hari = hariSejak(mulai)
        binding.tvHariKe.visibility = View.VISIBLE
        binding.tvHariKe.text = if (hari <= 0) {
            getString(R.string.ghm_mulai_hari_ini)
        } else {
            getString(R.string.ghm_hari_ke, hari)
        }
    }

    private fun observeFaseData() {
        viewModel.faseData.observe(this) { response ->
            when (response) {
                is ApiResponse.Empty -> { /* memuat */ }
                is ApiResponse.Success -> {
                    @Suppress("USELESS_ELVIS")
                    tampilkanFase(response.data.data ?: emptyList()) // Gson bisa mengisi null
                }
                is ApiResponse.Error -> {
                    Toast.makeText(this, getString(R.string.ghm_galat_muat, response.errorMessage), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun tampilkanFase(faseList: List<DataGetFase>) {
        val mulai = tanggalMulai()
        val fase = faseSopDari(mulai)
        binding.pita.root.visibility = View.VISIBLE
        binding.pita.tampilkan(fase)
        tampilkanKartuFase(fase)

        val pembesaran = faseList.find { it.jenis.uppercase() == "PEMBESARAN" }
        val panen = faseList.find { it.jenis.uppercase() == "PANEN" }
        // jumlahMakanan dari server dalam gram. Fase PANEN pasti menyimpannya; PEMBESARAN belum tentu.
        val pakanGram = (panen?.jumlahMakanan ?: pembesaran?.jumlahMakanan)?.takeIf { it > 0 }
        tampilkanPerkiraan(panen, pakanGram?.div(1000.0))
        tampilkanLangkahBerikutnya(fase, pembesaran, panen, pakanGram)

        val baris = susunJadwal(faseList, mulai, fase)
        detailSiklusAdapter.setItems(baris)
        binding.tvFaseKosong.visibility = if (baris.isEmpty()) View.VISIBLE else View.GONE
    }

    /** Kartu "Fase sekarang": nama fase, rentang umur, dan panduan tindakan dari SOP. */
    private fun tampilkanKartuFase(fase: FaseSop?) {
        binding.cardFase.visibility = View.VISIBLE
        if (fase == null) {
            binding.tvFaseNama.text = getString(R.string.ghm_sop_selesai)
            binding.tvFaseUmur.visibility = View.GONE
            binding.tvFasePanduan.visibility = View.GONE
            return
        }
        binding.tvFaseNama.text = getString(fase.namaRes)
        binding.tvFaseUmur.visibility = View.VISIBLE
        binding.tvFaseUmur.text = getString(R.string.ghm_sop_umur, fase.hariAwal, fase.hariAkhir)
        binding.tvFasePanduan.visibility = View.VISIBLE
        binding.tvFasePanduan.text = getString(fase.panduanRes)
    }

    /** Kartu "Perkiraan hasil panen" dari prediksiPanen di fase PANEN (dibuat otomatis). */
    private fun tampilkanPerkiraan(panen: DataGetFase?, pakanKg: Double?) {
        val prediksi = panen?.prediksiPanen as? Map<*, *>
        val hasilKg = prediksi?.let {
            anyToDouble(it["hasilKg"]) ?: anyToDouble(it["hasilGram"])?.div(1000.0)
        }
        if (panen == null || hasilKg == null || hasilKg <= 0) {
            binding.cardPerkiraan.visibility = View.GONE
            return
        }
        binding.cardPerkiraan.visibility = View.VISIBLE
        binding.tvPerkiraanKg.text = formatKg(hasilKg)
        binding.cardPerkiraan.contentDescription = getString(R.string.ghm_lihat_rincian_perkiraan, formatKg(hasilKg))
        binding.cardPerkiraan.setOnClickListener {
            // Extra sama seperti sebelumnya, dibaca HasilPrediksiActivity.
            val intent = Intent(this, HasilPrediksiActivity::class.java)
            intent.putExtra("FASE_ID", panen.id)
            intent.putExtra("PREDIKSI_PANEN", panen.prediksiPanen.toString())
            intent.putExtra("HASIL_KG", prediksi["hasilKg"]?.toString() ?: "0")
            // Untuk "Siklus N" dan kartu "Dihitung dari" di layar Perkiraan panen.
            intent.putExtra("SIKLUS_ID", siklusId)
            intent.putExtra("JUMLAH_TELUR", this.intent.getStringExtra("EXTRA_JUMLAH"))
            intent.putExtra("MEDIA_TELUR", this.intent.getStringExtra("EXTRA_MEDIA"))
            intent.putExtra("PAKAN_KG", pakanKg ?: 0.0)
            startActivity(intent)
        }
    }

    /**
     * Jadwal 5 fase SOP dihitung dari tanggal mulai (hari ke-0, 13, 21, 28, 35).
     * Tanggal fase Pembesaran memakai tanggal yang benar-benar dicatat bila ada.
     */
    private fun susunJadwal(faseList: List<DataGetFase>, mulai: Date?, faseSekarang: FaseSop?): List<BarisFase> {
        if (mulai == null) return emptyList()
        val format = SimpleDateFormat("d MMM yyyy", LOCALE_ID)
        val pembesaran = faseList.find { it.jenis.uppercase() == "PEMBESARAN" }
        val pakanGram = (faseList.find { it.jenis.uppercase() == "PANEN" }?.jumlahMakanan
            ?: pembesaran?.jumlahMakanan)?.takeIf { it > 0 }

        return FaseSop.entries.map { fase ->
            val status = when {
                faseSekarang == null || fase.ordinal < faseSekarang.ordinal ->
                    getString(R.string.ghm_sop_status_selesai)
                fase == faseSekarang -> getString(R.string.ghm_sop_status_sekarang)
                else -> getString(R.string.ghm_sop_status_belum)
            }
            val tanggal = if (fase == FaseSop.PEMBESARAN && pembesaran?.tanggal != null) {
                parseServerDate(pembesaran.tanggal)?.let { format.format(it) }
                    ?: format.format(tanggalFase(mulai, fase))
            } else {
                format.format(tanggalFase(mulai, fase))
            }
            val keterangan = if (fase == FaseSop.PEMBESARAN && pakanGram != null) {
                listOf(status, getString(R.string.ghm_sop_pakan_tercatat, formatKg(pakanGram / 1000.0)))
                    .joinToString(" · ")
            } else {
                status
            }
            BarisFase(
                judul = getString(fase.namaRes),
                tanggal = tanggal,
                keterangan = keterangan,
            )
        }
    }

    /**
     * Kartu kuning + tombol bawah:
     * - Fase besar belum dicatat dan siklus sudah masuk hari ke-13 -> "Catat fase besar".
     * - Fase besar sudah ada tapi perkiraan belum ada (mis. layanan AI sempat tidak tersedia)
     *   -> "Hitung perkiraan panen" (kirim fase PANEN lagi memakai data pakan fase besar).
     * - Perkiraan sudah ada / belum waktunya -> kartu disembunyikan.
     */
    private fun tampilkanLangkahBerikutnya(
        fase: FaseSop?,
        pembesaran: DataGetFase?,
        panen: DataGetFase?,
        pakanGram: Double?,
    ) {
        val telurGram = intent.getStringExtra("EXTRA_JUMLAH")?.toIntOrNull() ?: 0
        when {
            panen?.prediksiPanen != null -> sembunyikanLangkah()
            pembesaran != null && telurGram >= 1 -> {
                binding.cardLangkah.visibility = View.VISIBLE
                binding.tvLangkah.setText(R.string.ghm_langkah_hitung_perkiraan)
                binding.layoutFooter.visibility = View.VISIBLE
                if (!menghitung) binding.btnCatatFase.setText(R.string.ghm_tombol_hitung_perkiraan)
                binding.btnCatatFase.setOnClickListener {
                    if (pakanGram != null) {
                        // Jumlah pakan sudah tercatat di fase besar: langsung minta perkiraan.
                        val hariIni = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
                        viewModel.hitungPerkiraan(siklusId, hariIni, telurGram, pakanGram)
                    } else {
                        // Jumlah pakan belum ada: isi lewat form, tanpa membuat fase besar kedua.
                        bukaTambahFase(mode = MODE_HANYA_PANEN)
                    }
                }
            }
            pembesaran == null && fase != null && fase.ordinal >= FaseSop.PEMBESARAN.ordinal -> {
                binding.cardLangkah.visibility = View.VISIBLE
                binding.tvLangkah.setText(R.string.ghm_langkah_catat_besar)
                binding.layoutFooter.visibility = View.VISIBLE
                binding.btnCatatFase.setText(R.string.ghm_tombol_catat_fase_besar)
                binding.btnCatatFase.setOnClickListener { bukaTambahFaseBesar() }
            }
            else -> sembunyikanLangkah()
        }
    }

    private fun sembunyikanLangkah() {
        binding.cardLangkah.visibility = View.GONE
        binding.layoutFooter.visibility = View.GONE
    }

    private var menghitung = false

    private fun observePerkiraan() {
        viewModel.statusPerkiraan.observe(this) { status ->
            when (status) {
                is ApiResponse.Empty -> {
                    menghitung = true
                    binding.btnCatatFase.isEnabled = false
                    binding.btnCatatFase.setText(R.string.ghm_menghitung_perkiraan)
                }
                is ApiResponse.Success -> {
                    menghitung = false
                    binding.btnCatatFase.isEnabled = true
                    viewModel.getFase(siklusId)
                }
                is ApiResponse.Error -> {
                    menghitung = false
                    binding.btnCatatFase.isEnabled = true
                    binding.btnCatatFase.setText(R.string.ghm_tombol_hitung_perkiraan)
                    Toast.makeText(this, status.errorMessage, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun bukaTambahFaseBesar() = bukaTambahFase(mode = "ADD")

    /**
     * Buka form fase besar dengan extra yang sama seperti sebelumnya.
     * [mode] "ADD" = fase besar + perkiraan; [MODE_HANYA_PANEN] = hanya perkiraan (fase besar sudah ada).
     */
    private fun bukaTambahFase(mode: String) {
        val intent = Intent(this, TambahFaseActivity::class.java)
        intent.putExtra("SIKLUS_ID", siklusId)
        intent.putExtra("JENIS_FASE", "PEMBESARAN")
        intent.putExtra("JUMLAH_TELUR", this.intent.getStringExtra("EXTRA_JUMLAH")?.toIntOrNull() ?: 0)
        intent.putExtra("MEDIA_TELUR", this.intent.getStringExtra("EXTRA_MEDIA") ?: "")
        intent.putExtra("MODE", mode)
        // Untuk "Hari ke-N" di form fase besar.
        intent.putExtra("TANGGAL_MULAI", this.intent.getStringExtra("EXTRA_TANGGAL_MULAI"))
        startActivity(intent)
    }

    companion object {
        /** Sama dengan TambahFaseActivity.MODE_HANYA_PANEN. */
        private const val MODE_HANYA_PANEN = "PANEN"
    }

    private fun anyToDouble(value: Any?): Double? = when (value) {
        is Number -> value.toDouble()
        is String -> value.trim().toDoubleOrNull()
        else -> null
    }
}
