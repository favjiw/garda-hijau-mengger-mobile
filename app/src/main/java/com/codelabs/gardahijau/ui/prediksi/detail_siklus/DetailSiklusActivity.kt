package com.codelabs.gardahijau.ui.prediksi.detail_siklus

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.codelabs.gardahijau.databinding.ActivityDetailSiklusBinding
import com.codelabs.gardahijau.model.remote.network.ApiResponse
import com.codelabs.gardahijau.ui.adapter.DetailSiklusAdapter
import com.codelabs.gardahijau.ui.prediksi.hasil_prediksi.HasilPrediksiActivity
import com.codelabs.gardahijau.ui.prediksi.tambah_fase.TambahFaseActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DetailSiklusActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailSiklusBinding
    private val viewModel: DetailSiklusViewModel by viewModels()
    private lateinit var detailSiklusAdapter: DetailSiklusAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailSiklusBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        setupToolbar()
        setupRecyclerView()
        loadFaseData()
        observeFaseData()
    }

    private fun setupToolbar() {
        binding.topAppBar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        detailSiklusAdapter = DetailSiklusAdapter(
            onFaseClick = { faseItem ->
                if (faseItem.jenis.uppercase() == "PANEN" && faseItem.prediksiPanen != null) {
                    val intent = Intent(this, HasilPrediksiActivity::class.java)

                    // Parse prediksiPanen jika berupa JSON object
                    val prediksiPanen = faseItem.prediksiPanen
                    var hasilKg = "0"

                    try {
                        if (prediksiPanen is Map<*, *>) {
                            hasilKg = prediksiPanen["hasilKg"]?.toString() ?: "0"
                        }
                    } catch (e: Exception) {
                        hasilKg = prediksiPanen.toString()
                    }

                    intent.putExtra("FASE_ID", faseItem.id)
                    intent.putExtra("PREDIKSI_PANEN", prediksiPanen.toString())
                    intent.putExtra("HASIL_KG", hasilKg)
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "Data prediksi belum tersedia", Toast.LENGTH_SHORT).show()
                }
            },
            onEmptyFaseClick = { jenisFase ->
                when (jenisFase.uppercase()) {
                    "PEMBESARAN" -> {
                        val intent = Intent(this, TambahFaseActivity::class.java)
                        val siklusId = this.intent.getIntExtra("EXTRA_ID", -1)
                        val jumlahTelur = this.intent.getStringExtra("EXTRA_JUMLAH")?.toIntOrNull() ?: 0
                        val mediaTelur = this.intent.getStringExtra("EXTRA_MEDIA") ?: ""

                        intent.putExtra("SIKLUS_ID", siklusId)
                        intent.putExtra("JENIS_FASE", "PEMBESARAN")
                        intent.putExtra("JUMLAH_TELUR", jumlahTelur)
                        intent.putExtra("MEDIA_TELUR", mediaTelur)
                        intent.putExtra("MODE", "ADD")
                        startActivityForResult(intent, REQUEST_ADD_FASE)
                    }
                    "PANEN" -> {
                        Toast.makeText(this, "Fase pembesaran harus diselesaikan terlebih dahulu", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )

        binding.rvSiklus.apply {
            layoutManager = LinearLayoutManager(this@DetailSiklusActivity)
            adapter = detailSiklusAdapter
        }
    }

    companion object {
        private const val REQUEST_ADD_FASE = 100
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ADD_FASE && resultCode == RESULT_OK) {
            loadFaseData()
        }
    }

    override fun onResume() {
        super.onResume()
        loadFaseData()
    }

    private fun loadFaseData() {
        val siklusId = intent.getIntExtra("EXTRA_ID", -1)
        if (siklusId != -1) {
            viewModel.getFase(siklusId)
        } else {
            Toast.makeText(this, "ID Siklus tidak valid", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun observeFaseData() {
        viewModel.faseData.observe(this) { response ->
            when (response) {
                is ApiResponse.Empty -> {
                    // Show loading if needed
                }
                is ApiResponse.Success -> {
                    val faseItems = response.data.data
                    if (faseItems.isNotEmpty()) {
                        detailSiklusAdapter.setItems(faseItems)
                    } else {
                        Toast.makeText(this, "Tidak ada data fase", Toast.LENGTH_SHORT).show()
                    }
                }
                is ApiResponse.Error -> {
                    Toast.makeText(this, response.errorMessage, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
