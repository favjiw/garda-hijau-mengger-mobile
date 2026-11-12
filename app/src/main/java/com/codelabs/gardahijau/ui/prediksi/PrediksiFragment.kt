package com.codelabs.gardahijau.ui.prediksi

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.codelabs.gardahijau.R
import com.codelabs.gardahijau.databinding.FragmentPrediksiBinding
import com.codelabs.gardahijau.model.remote.network.ApiResponse
import com.codelabs.gardahijau.model.remote.response.prediksi.DataItem
import com.codelabs.gardahijau.ui.adapter.KartuSiklus
import com.codelabs.gardahijau.ui.adapter.SiklusAdapter
import com.codelabs.gardahijau.ui.prediksi.detail_siklus.DetailSiklusActivity
import com.codelabs.gardahijau.ui.prediksi.tambah_siklus.TambahSiklusActivity
import com.codelabs.gardahijau.ui.util.formatKg
import com.codelabs.gardahijau.ui.util.hariSejak
import com.codelabs.gardahijau.ui.util.parseServerDate
import com.codelabs.gardahijau.ui.widget.tahapDariFase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class PrediksiFragment : Fragment() {

    private var _binding: FragmentPrediksiBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PrediksiViewModel by viewModels()
    private lateinit var siklusAdapter: SiklusAdapter
    private var muatKartuJob: Job? = null

    private val tambahSiklusLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { /* data dimuat ulang di onResume */ }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPrediksiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeDashboardTotal()
        observeSiklusData()
        setupClickListeners()
    }

    /** Muat ulang setiap kembali ke layar ini, supaya fase / siklus baru langsung terlihat. */
    override fun onResume() {
        super.onResume()
        viewModel.getHasilPanenDashboard()
        viewModel.getSiklus()
    }

    private fun observeDashboardTotal() {
        viewModel.hasilDashboard.observe(viewLifecycleOwner) { response ->
            when (response) {
                is ApiResponse.Success -> {
                    val totalKg = anyToDouble(response.data.totalKg)
                        ?: (response.data.totalGram / 1000.0)
                    tampilkanTotal(totalKg, response.data.jumlahPrediksi)
                }
                is ApiResponse.Error -> tampilkanTotal(0.0, 0)
                else -> { /* memuat */ }
            }
        }
    }

    private fun tampilkanTotal(totalKg: Double, jumlahPrediksi: Int) {
        if (totalKg > 0) {
            binding.layoutTotalPerkiraan.visibility = View.VISIBLE
            binding.tvTotalPerkiraan.text = formatKg(totalKg)
            binding.tvSubPerkiraan.text = getString(R.string.ghm_perkiraan_dari_siklus, jumlahPrediksi)
        } else {
            binding.layoutTotalPerkiraan.visibility = View.GONE
            binding.tvSubPerkiraan.text = getString(R.string.ghm_perkiraan_total_kosong)
        }
    }

    private fun observeSiklusData() {
        viewModel.siklusData.observe(viewLifecycleOwner) { response ->
            when (response) {
                is ApiResponse.Empty -> { /* memuat */ }
                is ApiResponse.Success -> {
                    @Suppress("USELESS_ELVIS")
                    val list: List<DataItem> = response.data.data ?: emptyList() // Gson bisa mengisi null
                    // Siklus terbaru di atas.
                    val sorted = list.sortedByDescending { parseServerDate(it.tanggalMulai)?.time ?: 0L }
                    tampilkanSiklus(sorted)
                }
                is ApiResponse.Error -> {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.ghm_galat_muat, response.errorMessage),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun tampilkanSiklus(list: List<DataItem>) {
        binding.tvJumlahSiklus.visibility = View.VISIBLE
        binding.tvJumlahSiklus.text = getString(R.string.ghm_jumlah_siklus, list.size)
        binding.tvSiklusKosong.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        binding.rvPrediksiSiklus.visibility = if (list.isEmpty()) View.GONE else View.VISIBLE

        // Tampilkan kartu dulu, lalu lengkapi tahap & perkiraan per siklus.
        siklusAdapter.setItems(list.map { KartuSiklus(it, parseServerDate(it.tanggalMulai)?.let(::hariSejak), null, null, dimuat = false) })

        muatKartuJob?.cancel()
        muatKartuJob = viewLifecycleOwner.lifecycleScope.launch {
            val kartu = list.map { item -> async { muatKartu(item) } }.awaitAll()
            siklusAdapter.setItems(kartu)
        }
    }

    private suspend fun muatKartu(item: DataItem): KartuSiklus {
        val tahap = when (val fase = viewModel.fetchFase(item.id)) {
            is ApiResponse.Success -> {
                @Suppress("USELESS_ELVIS")
                tahapDariFase((fase.data.data ?: emptyList()).map { it.jenis })
            }
            else -> null
        }
        val hasilKg = when (val hasil = viewModel.fetchHasilPanenSiklus(item.id)) {
            is ApiResponse.Success -> {
                @Suppress("USELESS_ELVIS")
                val first = (hasil.data.data ?: emptyList()).firstOrNull()
                first?.let { anyToDouble(it.hasilKg) ?: anyToDouble(it.hasilGram)?.div(1000.0) }
            }
            else -> null
        }
        return KartuSiklus(item, parseServerDate(item.tanggalMulai)?.let(::hariSejak), tahap, hasilKg, dimuat = true)
    }

    /** Format tanggal yang dikirim ke DetailSiklusActivity (EXTRA_TANGGAL), jangan diubah dulu. */
    private fun formatDate(dateString: String?): String {
        if (dateString.isNullOrBlank()) return ""
        return try {
            val inputFormat = if (dateString.contains("T")) {
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            } else {
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            }

            val outputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            val date = inputFormat.parse(dateString)
            outputFormat.format(date ?: Date())
        } catch (e: Exception) {
            dateString.orEmpty()
        }
    }

    private fun setupClickListeners() {
        binding.btnMulaiSiklus.setOnClickListener {
            tambahSiklusLauncher.launch(Intent(requireContext(), TambahSiklusActivity::class.java))
        }
    }

    private fun setupRecyclerView() {
        siklusAdapter = SiklusAdapter { dataItem ->
            val intent = Intent(requireContext(), DetailSiklusActivity::class.java).apply {
                putExtra("EXTRA_TANGGAL", formatDate(dataItem.tanggalMulai))
                putExtra("EXTRA_JUMLAH", dataItem.jumlahTelur.toString())
                putExtra("EXTRA_MEDIA", dataItem.mediaTelur)
                putExtra("EXTRA_CATATAN", dataItem.catatan)
                putExtra("EXTRA_ID", dataItem.id)
                // Tanggal mulai mentah (ISO, UTC) untuk "Hari ke-N" di Detail siklus.
                putExtra("EXTRA_TANGGAL_MULAI", dataItem.tanggalMulai)
            }
            startActivity(intent)
        }

        binding.rvPrediksiSiklus.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = siklusAdapter
        }
    }

    /** Angka dari field bertipe Any (Gson: Double, String, atau null). */
    private fun anyToDouble(value: Any?): Double? = when (value) {
        is Number -> value.toDouble()
        is String -> value.trim().toDoubleOrNull()
        else -> null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        muatKartuJob?.cancel()
        _binding = null
    }
}
