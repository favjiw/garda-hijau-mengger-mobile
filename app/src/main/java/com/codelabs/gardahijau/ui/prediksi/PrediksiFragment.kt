package com.codelabs.gardahijau.ui.prediksi

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.codelabs.gardahijau.R
import com.codelabs.gardahijau.databinding.FragmentPrediksiBinding
import com.codelabs.gardahijau.model.local.data.SiklusItem
import com.codelabs.gardahijau.model.local.data.UserPreferences
import com.codelabs.gardahijau.model.remote.network.ApiResponse
import com.codelabs.gardahijau.model.remote.response.prediksi.DataItem
import com.codelabs.gardahijau.ui.adapter.SiklusAdapter
import com.codelabs.gardahijau.ui.auth.login.LoginActivity
import com.codelabs.gardahijau.ui.auth.login.LoginViewModel
import com.codelabs.gardahijau.ui.notifications.NotificationsActivity
import com.codelabs.gardahijau.ui.prediksi.detail_siklus.DetailSiklusActivity
import com.codelabs.gardahijau.ui.prediksi.tambah_siklus.TambahSiklusActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class PrediksiFragment : Fragment() {

    private var _binding: FragmentPrediksiBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PrediksiViewModel by viewModels()
    private val loginViewModel: LoginViewModel by viewModels()
    private var siklusAdapter = SiklusAdapter()

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
        loadSiklusData()
        observeSiklusData()
        setupClickListeners()
        setupLogout()
        observeLogout()
    }

    private fun observeDashboardTotal() {
        viewModel.getHasilPanenDashboard()
        viewModel.hasilDashboard.observe(viewLifecycleOwner) { response ->
            when (response) {
                is ApiResponse.Success -> {
                    val totalKg = response.data.totalKg
                    val text = formatHasilKgFromAny(totalKg)
                    val totalSiklus = response.data.jumlahPrediksi

                    Log.d("PrediksiFragment", "totalKg: $totalKg, text: $text, totalSiklus: $totalSiklus")

                    updateDashboardResult(text, totalSiklus)
                }
                is ApiResponse.Error -> {
                    updateDashboardResult("Belum tersedia", 0)
                }
                else -> { /* loading state */ }
            }
        }
    }

    private fun updateDashboardResult(text: String, totalsiklus: Int) {
        binding.includePrediksiDashboard.root.findViewById<AppCompatTextView>(R.id.tvResultPrediksiTotal)?.text = text
        binding.includePrediksiDashboard.root.findViewById<AppCompatTextView>(R.id.JumlahWadahAktif)?.text = "Jumlah Wadah Aktif : $totalsiklus"

        val smallSp = 20f
        val largeSp = 35f

        binding.includePrediksiDashboard.tvResultPrediksiTotal?.let { textView ->
            if (text.equals("Belum tersedia", ignoreCase = true)) {
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, smallSp)
            } else {
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, largeSp)
            }
        }
    }

    private fun loadSiklusData() {
        viewModel.getSiklus()
    }

    private fun observeSiklusData() {
        viewModel.siklusData.observe(viewLifecycleOwner) { response ->
            when (response) {
                is ApiResponse.Empty -> {
                    binding.rvPrediksiSiklus.visibility = View.VISIBLE
                }
                is ApiResponse.Success -> {
                    val loadingSiklusItems = response.data.data.map { dataItem ->
                        SiklusItem(
                            title = "Siklus ${dataItem.id}",
                            label = "Prediksi Hasil Panen",
                            result = "Memuat...",
                            date = "Tanggal Mulai: ${formatDate(dataItem.tanggalMulai)}"
                        )
                    }

                    siklusAdapter.setItems(loadingSiklusItems, response.data.data)
                    binding.rvPrediksiSiklus.visibility = View.VISIBLE

                    loadAllHasilPanen(response.data.data)
                }
                is ApiResponse.Error -> {
                    Toast.makeText(requireContext(), response.errorMessage, Toast.LENGTH_SHORT).show()
                    binding.rvPrediksiSiklus.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ADD_SIKLUS && resultCode == Activity.RESULT_OK) {
            loadSiklusData()
            observeDashboardTotal()
        }
    }

    private fun loadAllHasilPanen(dataItems: List<DataItem>) {
        val updatedItems = mutableListOf<SiklusItem>()
        var completedCount = 0

        dataItems.forEachIndexed { index, dataItem ->
            lifecycleScope.launch {
                val apiRes = viewModel.fetchHasilPanenSiklus(dataItem.id)
                val hasilText = if (apiRes is ApiResponse.Success) {
                    val panenList = apiRes.data.data
                    val first = panenList.firstOrNull()
                    if (first != null) {
                        formatHasilKgFromAny(first.hasilKg ?: first.hasilGram)
                    } else {
                        "Belum tersedia"
                    }
                } else {
                    "Belum tersedia"
                }

                val siklusItem = SiklusItem(
                    title = "Siklus ${dataItem.id}",
                    label = "Prediksi Hasil Panen",
                    result = hasilText,
                    date = "Tanggal Mulai: ${formatDate(dataItem.tanggalMulai)}"
                )

                synchronized(updatedItems) {
                    if (updatedItems.size <= index) {
                        repeat(index + 1 - updatedItems.size) {
                            updatedItems.add(siklusItem)
                        }
                    }
                    updatedItems[index] = siklusItem
                    completedCount++

                    if (completedCount == dataItems.size) {
                        siklusAdapter.setItems(updatedItems, dataItems)
                    }
                }
            }
        }
    }

    private fun formatDate(dateString: String): String {
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
            dateString
        }
    }



    private fun observeLogout() {
        loginViewModel.logoutResult.observe(viewLifecycleOwner) { res ->
            when (res) {
                is ApiResponse.Empty -> {
                    binding.imgLogout.isEnabled = false
                }
                is ApiResponse.Success -> {
                    val intent = Intent(requireContext(), LoginActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    startActivity(intent)
                    requireActivity().finish()
                }
                is ApiResponse.Error -> {
                    binding.imgLogout.isEnabled = true
                    Toast.makeText(requireContext(), res.errorMessage, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupLogout() {
        binding.imgLogout.isClickable = true
        binding.imgLogout.setOnClickListener {
            val confirmDialog = AlertDialog.Builder(requireContext())
                .setTitle("Logout")
                .setMessage("Yakin ingin logout?")
                .setNegativeButton("Batal", null)
                .setPositiveButton("Ya") { _, _ ->
                    val progressBar = ProgressBar(requireContext()).apply {
                        isIndeterminate = true
                        val pad = (16 * resources.displayMetrics.density).toInt()
                        setPadding(pad, pad, pad, pad)
                    }
                    val loadingDialog = AlertDialog.Builder(requireContext())
                        .setView(progressBar)
                        .setCancelable(false)
                        .create()
                    loadingDialog.show()

                    lifecycleScope.launch {
                        try {
                            val prefs = UserPreferences(requireContext())
                            prefs.clear()
                        } catch (e: Exception) {
                        } finally {
                            loadingDialog.dismiss()
                            val intent = Intent(requireContext(), LoginActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                        }
                    }
                }
                .create()
            confirmDialog.show()
        }
    }

    private fun setupClickListeners() {
        binding.fabAddPrediksi.setOnClickListener {
            val intent = Intent(requireContext(), TambahSiklusActivity::class.java)
            startActivityForResult(intent, REQUEST_ADD_SIKLUS)
        }

        binding.imgNotification.setOnClickListener {
            val intent = Intent(requireContext(), NotificationsActivity::class.java)
            startActivity(intent)
        }
    }

    companion object {
        private const val REQUEST_ADD_SIKLUS = 200
    }

    private fun setupRecyclerView() {
        siklusAdapter = SiklusAdapter { dataItem ->
            // Handle click pada item siklus
            val intent = Intent(requireContext(), DetailSiklusActivity::class.java).apply {
                putExtra("EXTRA_TANGGAL", formatDate(dataItem.tanggalMulai))
                putExtra("EXTRA_JUMLAH", dataItem.jumlahTelur.toString())
                putExtra("EXTRA_MEDIA", dataItem.mediaTelur)
                putExtra("EXTRA_CATATAN", dataItem.catatan)
                putExtra("EXTRA_ID", dataItem.id)
            }
            startActivity(intent)
        }

        binding.rvPrediksiSiklus.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = siklusAdapter
        }
    }

//    private fun loadDummyData() {
//        val dummy = listOf(
//            SiklusItem(
//                title = "Siklus 1",
//                label = "Prediksi Hasil Panen",
//                result = "25 kg",
//                date = "Estimasi Tanggal Panen : 24 Oktober 2025"
//            ),
//            SiklusItem(
//                title = "Siklus 2",
//                label = "Prediksi Hasil Panen",
//                result = "18 kg",
//                date = "Estimasi Tanggal Panen : 10 November 2025"
//            ),
//            SiklusItem(
//                title = "Siklus 3",
//                label = "Prediksi Hasil Panen",
//                result = "30 kg",
//                date = "Estimasi Tanggal Panen : 02 Desember 2025"
//            )
//        )
//        siklusAdapter.setItems(dummy)
//    }

    private fun parseAnyToDouble(value: Any?): Double? {
        return when (value) {
            null -> null
            is Number -> value.toDouble()
            is String -> {
                val s = value.trim()
                if (s.equals("null", ignoreCase = true) || s.isEmpty()) return null
                s.toDoubleOrNull()
            }
            is Map<*, *> -> {
                val candidates = listOf("hasilKg", "hasil_kg", "hasilKG")
                for (k in candidates) {
                    val v = value[k]
                    val d = parseAnyToDouble(v)
                    if (d != null) return d
                }
                // try hasilGram if present
                val gram = parseAnyToDouble(value["hasilGram"] ?: value["hasil_gram"])
                gram?.let { return it / 1000.0 }
                null
            }
            else -> null
        }
    }

    private fun formatHasilKgFromAny(any: Any?): String {
        val kg = when {
            any == null -> null
            any is Map<*, *> -> {
                parseAnyToDouble(any["hasilKg"] ?: any["hasil_kg"]) ?: parseAnyToDouble(any["hasilGram"] ?: any["hasil_gram"])?.let { it / 1000.0 }
            }
            else -> {
                // try direct hasilKg or numeric
                parseAnyToDouble(any) ?: null
            }
        }

        if (kg == null || kg <= 0.0) return "Belum tersedia"
        val df = DecimalFormat("#.##")
        val formatted = if (kg % 1.0 == 0.0) kg.toInt().toString() else df.format(kg)
        return "$formatted KG"
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}