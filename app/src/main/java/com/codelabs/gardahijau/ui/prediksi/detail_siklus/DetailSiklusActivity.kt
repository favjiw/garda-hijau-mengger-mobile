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
import com.codelabs.gardahijau.ui.widget.TahapSiklus
import com.codelabs.gardahijau.ui.widget.tahapDariFase
import com.codelabs.gardahijau.ui.widget.tampilkan
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Date

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
        binding.btnCatatFase.setOnClickListener { bukaTambahFaseBesar() }
        tampilkanHariKe()

        binding.rvSiklus.apply {
            layoutManager = LinearLayoutManager(this@DetailSiklusActivity)
            adapter = detailSiklusAdapter
        }
        observeFaseData()
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
        val tahap = tahapDariFase(faseList.map { it.jenis })
        binding.pita.root.visibility = View.VISIBLE
        binding.pita.tampilkan(tahap)

        // Fase besar belum dicatat: tampilkan langkah berikutnya + tombol.
        val perluFaseBesar = tahap != TahapSiklus.BESAR
        binding.cardLangkah.visibility = if (perluFaseBesar) View.VISIBLE else View.GONE
        binding.layoutFooter.visibility = if (perluFaseBesar) View.VISIBLE else View.GONE

        tampilkanPerkiraan(faseList.find { it.jenis.uppercase() == "PANEN" })

        val baris = susunLinimasa(faseList)
        detailSiklusAdapter.setItems(baris)
        binding.tvFaseKosong.visibility = if (baris.isEmpty()) View.VISIBLE else View.GONE
    }

    /** Kartu "Perkiraan hasil panen" dari prediksiPanen di fase PANEN (dibuat otomatis). */
    private fun tampilkanPerkiraan(panen: DataGetFase?) {
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
            startActivity(intent)
        }
    }

    /**
     * Linimasa: Telur (data siklus), Tetas (fase PENETASAN), Besar (fase PEMBESARAN).
     * Fase PANEN tidak ditampilkan karena dibuat otomatis; isinya ada di kartu perkiraan.
     */
    private fun susunLinimasa(faseList: List<DataGetFase>): List<BarisFase> {
        val tanggalFormat = SimpleDateFormat("d MMM yyyy", LOCALE_ID)
        fun tanggal(value: String?) = parseServerDate(value)?.let { tanggalFormat.format(it) }.orEmpty()

        val baris = mutableListOf<BarisFase>()

        val jumlah = intent.getStringExtra("EXTRA_JUMLAH").orEmpty()
        val media = intent.getStringExtra("EXTRA_MEDIA").orEmpty()
        val catatan = intent.getStringExtra("EXTRA_CATATAN").orEmpty()
        val mulai = tanggalMulai()
        if (mulai != null || jumlah.isNotBlank()) {
            val isi = listOfNotNull(
                if (jumlah.isNotBlank() && media.isNotBlank()) {
                    getString(R.string.ghm_fase_telur_isi, jumlah, media.lowercase(LOCALE_ID))
                } else null,
                catatan.takeIf { it.isNotBlank() },
            ).joinToString(" ")
            baris += BarisFase(
                judul = getString(R.string.ghm_tahap_telur),
                tanggal = mulai?.let { tanggalFormat.format(it) }.orEmpty(),
                keterangan = isi,
            )
        }

        faseList.find { it.jenis.uppercase() == "PENETASAN" }?.let { fase ->
            baris += BarisFase(
                judul = getString(R.string.ghm_tahap_tetas),
                tanggal = tanggal(fase.tanggal),
                keterangan = fase.keterangan.orEmpty(),
            )
        }

        faseList.find { it.jenis.uppercase() == "PEMBESARAN" }?.let { fase ->
            val isi = listOfNotNull(
                fase.jumlahMakanan.takeIf { it > 0 }?.let { getString(R.string.ghm_fase_pakan, it) },
                fase.keterangan?.takeIf { it.isNotBlank() },
            ).joinToString(" ")
            baris += BarisFase(
                judul = getString(R.string.ghm_tahap_besar),
                tanggal = tanggal(fase.tanggal),
                keterangan = isi,
            )
        }
        return baris
    }

    /** Buka form fase besar dengan extra yang sama seperti sebelumnya. */
    private fun bukaTambahFaseBesar() {
        val intent = Intent(this, TambahFaseActivity::class.java)
        intent.putExtra("SIKLUS_ID", siklusId)
        intent.putExtra("JENIS_FASE", "PEMBESARAN")
        intent.putExtra("JUMLAH_TELUR", this.intent.getStringExtra("EXTRA_JUMLAH")?.toIntOrNull() ?: 0)
        intent.putExtra("MEDIA_TELUR", this.intent.getStringExtra("EXTRA_MEDIA") ?: "")
        intent.putExtra("MODE", "ADD")
        startActivity(intent)
    }

    private fun anyToDouble(value: Any?): Double? = when (value) {
        is Number -> value.toDouble()
        is String -> value.trim().toDoubleOrNull()
        else -> null
    }
}
