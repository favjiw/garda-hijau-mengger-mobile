package com.codelabs.gardahijau.ui.pencatatan_history

import android.app.Activity
import android.app.DatePickerDialog
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
import java.util.Date

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

    /** Semua catatan dari server; rekapan difilter di sisi klien sesuai rentang terpilih. */
    private var semuaItem: List<DataItem> = emptyList()

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
        setupChipRentang()
        setupObservers()
        viewModel.getHistoryPencacahan()
    }

    private fun setupObservers() {
        viewModel.historyData.observe(viewLifecycleOwner) { response ->
            @Suppress("USELESS_ELVIS")
            val items: List<DataItem> = response.data ?: emptyList() // Gson bisa mengisi null
            semuaItem = items
            historyAdapter.setItems(items)
            binding.tvRiwayatKosong.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
            binding.rvPrediksiSiklus.visibility = if (items.isEmpty()) View.GONE else View.VISIBLE
            renderRekapan()
        }

        viewModel.rentang.observe(viewLifecycleOwner) { rentang ->
            terapkanChip(rentang)
            renderRekapan()
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { _ ->
            // Tambahkan indikator memuat jika diperlukan
        }
    }

    /**
     * Mengisi kartu rekapan: total sesuai rentang yang dipilih (7 hari / 30 hari / bulan ini /
     * kustom), grafik batang per hari (atau per minggu bila rentangnya panjang), dan hari
     * dengan sampah terbanyak.
     */
    private fun renderRekapan() {
        val rentang = viewModel.rentang.value ?: return
        val (awal, akhir) = rentang.batas()
        val dalamRentang = semuaItem.filter { item ->
            val tanggal = parseServerDate(item.tanggalWaktu)
            tanggal != null && !tanggal.before(awal) && !tanggal.after(akhir)
        }

        val dayKey = SimpleDateFormat("yyyyMMdd", LOCALE_ID)
        val totalsByDay = HashMap<String, Double>()
        dalamRentang.forEach { item ->
            val tanggal = parseServerDate(item.tanggalWaktu) ?: return@forEach
            val key = dayKey.format(tanggal)
            totalsByDay[key] = (totalsByDay[key] ?: 0.0) + (item.totalSampah ?: 0.0)
        }

        // Semua hari dalam rentang, inklusif.
        val hari = mutableListOf<Date>()
        val kursor = Calendar.getInstance().apply { time = awal }
        val batasAkhir = Calendar.getInstance().apply { time = akhir }
        while (!kursor.after(batasAkhir)) {
            hari += kursor.time
            kursor.add(Calendar.DAY_OF_YEAR, 1)
        }

        val bars: List<WeeklyBarChartView.Bar>
        val highlight: Int
        if (hari.size <= 31) {
            bars = hari.map { tanggal ->
                WeeklyBarChartView.Bar(
                    label = if (hari.size <= 7) {
                        SimpleDateFormat("EEE", LOCALE_ID).format(tanggal).removeSuffix(".")
                    } else {
                        SimpleDateFormat("d", LOCALE_ID).format(tanggal)
                    },
                    value = totalsByDay[dayKey.format(tanggal)] ?: 0.0,
                )
            }
            highlight = if (samaHari(hari.last(), Date())) bars.lastIndex else -1
        } else {
            // Rentang panjang: gabung per minggu (7 hari) supaya batang tetap terbaca.
            bars = hari.chunked(7).map { minggu ->
                WeeklyBarChartView.Bar(
                    label = SimpleDateFormat("d/M", LOCALE_ID).format(minggu.first()),
                    value = minggu.sumOf { totalsByDay[dayKey.format(it)] ?: 0.0 },
                )
            }
            highlight = -1
        }
        binding.chartMingguan.setData(bars, highlight)

        binding.tvTotalMinggu.text = formatKg(bars.sumOf { it.value })
        binding.tvRentangMinggu.text = getString(
            R.string.ghm_rentang_rekap,
            formatRange(awal, akhir),
            dalamRentang.size,
        )

        val hariTerbanyak = hari.maxByOrNull { totalsByDay[dayKey.format(it)] ?: 0.0 }
        val nilaiTerbanyak = hariTerbanyak?.let { totalsByDay[dayKey.format(it)] ?: 0.0 } ?: 0.0
        binding.tvInsight.text = if (nilaiTerbanyak <= 0) {
            getString(R.string.ghm_belum_ada_sampah_rentang)
        } else {
            val nama = SimpleDateFormat("EEEE, d MMM", LOCALE_ID)
                .format(hariTerbanyak!!)
                .replaceFirstChar { it.titlecase(LOCALE_ID) }
            getString(R.string.ghm_hari_terbanyak, nama, formatKg(nilaiTerbanyak))
        }
    }

    /** Tombol pilihan rentang rekapan (7 hari / 30 hari / bulan ini / kustom). */
    private fun setupChipRentang() {
        binding.chip7Hari.setOnClickListener {
            viewModel.setRentang(RentangRekapan(RentangRekapan.Jenis.TUJUH_HARI))
        }
        binding.chip30Hari.setOnClickListener {
            viewModel.setRentang(RentangRekapan(RentangRekapan.Jenis.TIGA_PULUH_HARI))
        }
        binding.chipBulanIni.setOnClickListener {
            viewModel.setRentang(RentangRekapan(RentangRekapan.Jenis.BULAN_INI))
        }
        binding.chipKustom.setOnClickListener { tanyaRentangKustom() }
    }

    /** Menandai chip yang sesuai dengan rentang aktif. */
    private fun terapkanChip(rentang: RentangRekapan) {
        val aktif = when (rentang.jenis) {
            RentangRekapan.Jenis.TUJUH_HARI -> binding.chip7Hari
            RentangRekapan.Jenis.TIGA_PULUH_HARI -> binding.chip30Hari
            RentangRekapan.Jenis.BULAN_INI -> binding.chipBulanIni
            RentangRekapan.Jenis.KUSTOM -> binding.chipKustom
        }
        listOf(binding.chip7Hari, binding.chip30Hari, binding.chipBulanIni, binding.chipKustom).forEach {
            it.isChecked = it == aktif
        }
    }

    /** "Kustom": pilih tanggal awal lalu tanggal akhir (dua langkah). */
    private fun tanyaRentangKustom() {
        val hariIni = Calendar.getInstance()
        val pilihAwal = DatePickerDialog(
            requireContext(),
            { _, tahun, bulan, tanggal ->
                val dari = Calendar.getInstance().apply { set(tahun, bulan, tanggal) }.time
                pilihTanggalAkhir(dari)
            },
            hariIni.get(Calendar.YEAR),
            hariIni.get(Calendar.MONTH),
            hariIni.get(Calendar.DAY_OF_MONTH),
        )
        pilihAwal.setTitle(getString(R.string.ghm_pilih_tanggal_awal))
        pilihAwal.setOnCancelListener { viewModel.rentang.value?.let(::terapkanChip) }
        pilihAwal.show()
    }

    private fun pilihTanggalAkhir(dari: Date) {
        val mulai = Calendar.getInstance().apply { time = dari }
        val pilihAkhir = DatePickerDialog(
            requireContext(),
            { _, tahun, bulan, tanggal ->
                val sampai = Calendar.getInstance().apply { set(tahun, bulan, tanggal) }.time
                viewModel.setRentang(RentangRekapan(RentangRekapan.Jenis.KUSTOM, dari, sampai))
            },
            mulai.get(Calendar.YEAR),
            mulai.get(Calendar.MONTH),
            mulai.get(Calendar.DAY_OF_MONTH),
        )
        pilihAkhir.setTitle(getString(R.string.ghm_pilih_tanggal_akhir))
        pilihAkhir.setOnCancelListener { viewModel.rentang.value?.let(::terapkanChip) }
        pilihAkhir.show()
    }

    /** Apakah dua tanggal jatuh di hari kalender yang sama (zona waktu HP). */
    private fun samaHari(a: Date, b: Date): Boolean {
        val ca = Calendar.getInstance().apply { time = a }
        val cb = Calendar.getInstance().apply { time = b }
        return ca.get(Calendar.YEAR) == cb.get(Calendar.YEAR) &&
            ca.get(Calendar.DAY_OF_YEAR) == cb.get(Calendar.DAY_OF_YEAR)
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
