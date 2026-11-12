package com.codelabs.gardahijau.ui.pencatatan_history.detail_pencatatan_history

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.codelabs.gardahijau.databinding.ActivityHistoryDetailBinding
import com.codelabs.gardahijau.model.remote.response.hitoryPencacahan.DataItem
import java.text.SimpleDateFormat
import java.util.*

class HistoryDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupActionBar()

        val historyItem = intent.getParcelableExtra<DataItem>("history_data")
        if (historyItem != null) {
            setupUI(historyItem)
        }
    }

    private fun setupActionBar() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = "Detail Riwayat Pencacahan"
    }

    private fun setupUI(item: DataItem) {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val outputDateFormat = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
        val outputTimeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        val formattedDate = try {
            val date = inputFormat.parse(item.tanggalWaktu)
            outputDateFormat.format(date ?: Date())
        } catch (e: Exception) {
            item.tanggalWaktu
        }

        val formattedTime = try {
            val date = inputFormat.parse(item.tanggalWaktu)
            outputTimeFormat.format(date ?: Date())
        } catch (e: Exception) {
            ""
        }

        binding.tvTanggalWaktu.text = formattedDate
        binding.tvWaktu.text = "Pukul: $formattedTime"
        binding.tvTotalSampah.text = "${item.totalSampah} kg"
        binding.tvCatatan.text = item.catatan ?: "Tidak ada catatan"

        val createdDate = try {
            val date = inputFormat.parse(item.createdAt)
            outputDateFormat.format(date ?: Date())
        } catch (e: Exception) {
            item.createdAt
        }
        binding.tvCreatedAt.text = "Dibuat: $createdDate"
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
