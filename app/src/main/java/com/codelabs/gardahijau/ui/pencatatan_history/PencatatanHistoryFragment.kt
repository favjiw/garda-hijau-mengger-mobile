package com.codelabs.gardahijau.ui.pencatatan_history

import android.app.Activity
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.codelabs.gardahijau.R
import com.codelabs.gardahijau.databinding.FragmentPencatatanHistoryBinding
import com.codelabs.gardahijau.model.local.data.UserPreferences
import com.codelabs.gardahijau.repository.LogoutRepository
import javax.inject.Inject
import com.codelabs.gardahijau.model.remote.network.ApiResponse
import com.codelabs.gardahijau.model.remote.response.hitoryPencacahan.DataItem
import com.codelabs.gardahijau.ui.adapter.HistoryAdapter
import com.codelabs.gardahijau.ui.auth.login.LoginActivity
import com.codelabs.gardahijau.ui.auth.login.LoginViewModel
import com.codelabs.gardahijau.ui.notifications.NotificationsActivity
import com.codelabs.gardahijau.ui.pencatatan_history.detail_pencatatan_history.HistoryDetailActivity
import com.codelabs.gardahijau.ui.pencatatan_history.tambah_riwayat.TambahRiwayatActivity
import com.codelabs.gardahijau.ui.util.LOCALE_ID
import com.codelabs.gardahijau.ui.util.formatKg
import com.codelabs.gardahijau.ui.util.parseServerDate
import com.codelabs.gardahijau.ui.widget.WeeklyBarChartView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar

@AndroidEntryPoint
class PencatatanHistoryFragment : Fragment() {

    private var _binding: FragmentPencatatanHistoryBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var logoutRepository: LogoutRepository

    @Inject
    lateinit var userPreferences: UserPreferences

    private val historyAdapter = HistoryAdapter()
    private val loginViewModel: LoginViewModel by viewModels()
    private val viewModel: PencatatanHistoryViewModel by viewModels()

    private val addHistoryLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.getHistoryPencacahan()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPencatatanHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupLogout()
        observeLogout()
        setupClickListeners()
        setupObservers()
        viewModel.getHistoryPencacahan()
    }

    private fun setupObservers() {
        viewModel.historyData.observe(viewLifecycleOwner) { response ->
            @Suppress("USELESS_ELVIS")
            val items: List<DataItem> = response.data ?: emptyList() // Gson bisa mengisi null
            historyAdapter.setItems(items)
            binding.tvRiwayatKosong.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
            binding.rvPrediksiSiklus.visibility = if (items.isEmpty()) View.GONE else View.VISIBLE
            bindWeeklySummary(items)
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { _ ->
            // Tambahkan indikator memuat jika diperlukan
        }
    }

    /**
     * Mengisi kartu "Sampah minggu ini": total 7 hari terakhir (termasuk hari ini),
     * grafik batang per hari, dan hari dengan sampah terbanyak.
     */
    private fun bindWeeklySummary(items: List<DataItem>) {
        val dayKey = SimpleDateFormat("yyyyMMdd", LOCALE_ID)
        val dayLabel = SimpleDateFormat("EEE", LOCALE_ID)
        val dayFull = SimpleDateFormat("EEEE", LOCALE_ID)

        val totalsByDay = HashMap<String, Double>()
        items.forEach { item ->
            val date = parseServerDate(item.tanggalWaktu) ?: return@forEach
            val key = dayKey.format(date)
            totalsByDay[key] = (totalsByDay[key] ?: 0.0) + (item.totalSampah ?: 0.0)
        }

        val days = (6 downTo 0).map { offset ->
            Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -offset) }.time
        }
        val bars = days.map { date ->
            WeeklyBarChartView.Bar(
                label = dayLabel.format(date).removeSuffix("."),
                value = totalsByDay[dayKey.format(date)] ?: 0.0,
            )
        }
        binding.chartMingguan.setData(bars, highlight = bars.lastIndex)

        val total = bars.sumOf { it.value }
        binding.tvTotalMinggu.text = formatKg(total)
        binding.tvRentangMinggu.text = getString(R.string.ghm_rentang_7_hari, formatRange(days.first(), days.last()))

        val busiest = bars.indices.maxByOrNull { bars[it].value }
        binding.tvInsight.text = if (busiest == null || bars[busiest].value <= 0) {
            getString(R.string.ghm_belum_ada_sampah_minggu_ini)
        } else {
            val name = dayFull.format(days[busiest]).replaceFirstChar { it.titlecase(LOCALE_ID) }
            getString(R.string.ghm_hari_terbanyak, name, formatKg(bars[busiest].value))
        }
    }

    /** "6–12 November" bila bulannya sama, "28 Okt – 3 Nov" bila berbeda. */
    private fun formatRange(start: java.util.Date, end: java.util.Date): String {
        val calStart = Calendar.getInstance().apply { time = start }
        val calEnd = Calendar.getInstance().apply { time = end }
        val sameMonth = calStart.get(Calendar.MONTH) == calEnd.get(Calendar.MONTH)
        return if (sameMonth) {
            "${SimpleDateFormat("d", LOCALE_ID).format(start)}–${SimpleDateFormat("d MMMM", LOCALE_ID).format(end)}"
        } else {
            val short = SimpleDateFormat("d MMM", LOCALE_ID)
            "${short.format(start)} – ${short.format(end)}"
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
        binding.imgLogout.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle(R.string.ghm_keluar_judul)
                .setMessage(R.string.ghm_keluar_pesan)
                .setNegativeButton(R.string.ghm_batal, null)
                .setPositiveButton(R.string.ghm_ya_keluar) { _, _ ->
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
                            // Token di-blacklist di server (POST /api/auth/logout), lalu dihapus di HP.
                            // Kalau server gagal dihubungi, token tetap dihapus supaya pengguna tetap keluar.
                            logoutRepository.logout()
                            userPreferences.clear()
                        } catch (e: Exception) {
                        } finally {
                            loadingDialog.dismiss()
                            val intent = Intent(requireContext(), LoginActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                        }
                    }
                }
                .show()
        }
    }

    private fun setupClickListeners() {
        binding.btnCatatSampah.setOnClickListener {
            val intent = Intent(requireContext(), TambahRiwayatActivity::class.java)
            addHistoryLauncher.launch(intent)
        }

        binding.imgNotification.setOnClickListener {
            startActivity(Intent(requireContext(), NotificationsActivity::class.java))
        }
    }

    private fun setupRecyclerView() {
        binding.rvPrediksiSiklus.apply {
            adapter = historyAdapter
            layoutManager = LinearLayoutManager(requireContext())
            addItemDecoration(RowDivider())
        }

        historyAdapter.setOnItemClickListener { historyItem ->
            val intent = Intent(requireContext(), HistoryDetailActivity::class.java)
            intent.putExtra("history_data", historyItem)
            startActivity(intent)
        }
    }

    /** Garis tipis di antara baris riwayat, tanpa garis setelah baris terakhir. */
    private inner class RowDivider : RecyclerView.ItemDecoration() {
        private val divider = ContextCompat.getDrawable(requireContext(), R.drawable.ghm_pembatas)!!

        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
            val position = parent.getChildAdapterPosition(view)
            val isLast = position == (parent.adapter?.itemCount ?: 0) - 1
            outRect.set(0, 0, 0, if (isLast) 0 else divider.intrinsicHeight)
        }

        override fun onDraw(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
            val left = parent.paddingLeft
            val right = parent.width - parent.paddingRight
            val count = parent.adapter?.itemCount ?: 0
            for (i in 0 until parent.childCount) {
                val child = parent.getChildAt(i)
                if (parent.getChildAdapterPosition(child) == count - 1) continue
                val top = child.bottom
                divider.setBounds(left, top, right, top + divider.intrinsicHeight)
                divider.draw(canvas)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        viewModel.getHistoryPencacahan()
    }
}
