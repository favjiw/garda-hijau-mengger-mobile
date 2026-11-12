package com.codelabs.gardahijau.ui.pencatatan_history.detail_pencatatan_history

import android.graphics.Color
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.codelabs.gardahijau.R
import com.codelabs.gardahijau.databinding.ActivityHistoryDetailBinding
import com.codelabs.gardahijau.model.remote.response.hitoryPencacahan.DataItem
import com.codelabs.gardahijau.ui.util.LOCALE_ID
import com.codelabs.gardahijau.ui.util.applySystemBarPadding
import com.codelabs.gardahijau.ui.util.formatKg
import com.codelabs.gardahijau.ui.util.parseServerDate
import java.text.SimpleDateFormat

/** Detail satu catatan sampah. Data datang lewat extra "history_data" dari tab Sampah. */
class HistoryDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
        )
        binding = ActivityHistoryDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        binding.layoutBilahAtas.applySystemBarPadding(top = true)
        binding.scrollIsi.applySystemBarPadding(bottom = true)

        binding.btnKembali.setOnClickListener { finish() }

        val historyItem = intent.getParcelableExtra<DataItem>("history_data")
        if (historyItem != null) {
            setupUI(historyItem)
        }
    }

    private fun setupUI(item: DataItem) {
        // Tanggal dari server dalam UTC; tampilkan dalam zona waktu HP.
        val waktu = parseServerDate(item.tanggalWaktu)
        binding.tvTanggalWaktu.text = waktu
            ?.let { SimpleDateFormat("EEEE, d MMMM yyyy", LOCALE_ID).format(it) }
            ?: item.tanggalWaktu.orEmpty()
        binding.tvWaktu.text = waktu
            ?.let { getString(R.string.ghm_riwayat_pukul, SimpleDateFormat("HH.mm", LOCALE_ID).format(it)) }
            .orEmpty()

        binding.tvTotalSampah.text = formatKg(item.totalSampah)
        binding.tvCatatan.text = item.catatan?.takeIf { it.isNotBlank() } ?: getString(R.string.ghm_tidak_ada_catatan)

        val dibuat = parseServerDate(item.createdAt)
            ?.let { SimpleDateFormat("d MMMM yyyy", LOCALE_ID).format(it) }
        binding.tvCreatedAt.text = dibuat?.let { getString(R.string.ghm_dicatat_pada, it) }.orEmpty()
    }
}
