package com.codelabs.gardahijau.ui.notifications

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.ColorRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.codelabs.gardahijau.R
import com.codelabs.gardahijau.databinding.ActivityNotificationsBinding
import com.codelabs.gardahijau.databinding.ItemNotificationBinding
import com.codelabs.gardahijau.databinding.ItemNotificationHeaderBinding
import com.codelabs.gardahijau.model.remote.response.notification.NotificationItem
import com.codelabs.gardahijau.ui.MainActivity
import com.codelabs.gardahijau.ui.util.LOCALE_ID
import com.codelabs.gardahijau.ui.util.applySystemBarPadding
import com.codelabs.gardahijau.ui.util.hariSejak
import com.codelabs.gardahijau.ui.util.parseServerDate
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat

@AndroidEntryPoint
class NotificationsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotificationsBinding
    private val viewModel: NotificationViewModel by viewModels()
    private val notificationAdapter = NotificationAdapter { item ->
        viewModel.markNotificationAsRead(item)
        val intent = Intent(this, NotificationDetailActivity::class.java)
        intent.putExtra("notif_data", item)
        startActivity(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
        )
        binding = ActivityNotificationsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        binding.layoutBilahAtas.applySystemBarPadding(top = true)
        binding.recyclerNotifications.applySystemBarPadding(bottom = true)

        binding.btnKembali.setOnClickListener {
            startActivity(
                Intent(this, MainActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            )
            finish()
        }

        binding.recyclerNotifications.apply {
            adapter = notificationAdapter
            layoutManager = LinearLayoutManager(this@NotificationsActivity)
        }

        viewModel.filteredData.observe(this) { list ->
            notificationAdapter.setItems(kelompokkan(list))
            binding.tvKosong.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.getNotification()
    }

    // ------------------------------------------------------------------ pengelompokan

    private sealed class Baris {
        data class Judul(val teks: String) : Baris()
        data class Notif(val item: NotificationItem, val pertama: Boolean, val terakhir: Boolean) : Baris()
    }

    /** Terbaru di atas, dikelompokkan per hari: "Hari ini", "Kemarin", lalu tanggal. */
    private fun kelompokkan(list: List<NotificationItem>): List<Baris> {
        val tanggalFormat = SimpleDateFormat("d MMMM yyyy", LOCALE_ID)
        val hariKey = SimpleDateFormat("yyyyMMdd", LOCALE_ID)
        val urut = list.sortedByDescending { parseServerDate(it.createdAt)?.time ?: 0L }

        val baris = mutableListOf<Baris>()
        urut.groupBy { item -> parseServerDate(item.createdAt)?.let { hariKey.format(it) } ?: "" }
            .forEach { (_, items) ->
                val tanggal = parseServerDate(items.first().createdAt)
                val judul = when {
                    tanggal == null -> null
                    hariSejak(tanggal) == 0 -> getString(R.string.ghm_hari_ini)
                    hariSejak(tanggal) == 1 -> getString(R.string.ghm_kemarin)
                    else -> tanggalFormat.format(tanggal)
                }
                judul?.let { baris += Baris.Judul(it) }
                items.forEachIndexed { index, item ->
                    baris += Baris.Notif(item, pertama = index == 0, terakhir = index == items.lastIndex)
                }
            }
        return baris
    }

    // ------------------------------------------------------------------ adapter

    private inner class NotificationAdapter(
        private val onItemClicked: (NotificationItem) -> Unit
    ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        private var items: List<Baris> = emptyList()

        fun setItems(newItems: List<Baris>) {
            items = newItems
            notifyDataSetChanged()
        }

        override fun getItemViewType(position: Int) = if (items[position] is Baris.Judul) TIPE_JUDUL else TIPE_NOTIF

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            return if (viewType == TIPE_JUDUL) {
                JudulViewHolder(ItemNotificationHeaderBinding.inflate(inflater, parent, false))
            } else {
                NotifViewHolder(ItemNotificationBinding.inflate(inflater, parent, false))
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            when (val baris = items[position]) {
                is Baris.Judul -> (holder as JudulViewHolder).bind(baris, position == 0)
                is Baris.Notif -> (holder as NotifViewHolder).bind(baris)
            }
        }

        override fun getItemCount() = items.size

        inner class JudulViewHolder(private val b: ItemNotificationHeaderBinding) : RecyclerView.ViewHolder(b.root) {
            fun bind(baris: Baris.Judul, pertama: Boolean) {
                b.tvJudulKelompok.text = baris.teks
                // Judul pertama lebih dekat ke bilah atas.
                b.tvJudulKelompok.setPadding(0, if (pertama) dp(8) else dp(20), 0, dp(12))
            }
        }

        inner class NotifViewHolder(private val b: ItemNotificationBinding) : RecyclerView.ViewHolder(b.root) {
            fun bind(baris: Baris.Notif) {
                val item = baris.item
                b.notificationTitle.text = item.title
                b.notificationDescription.text = item.message
                b.notificationTime.text = parseServerDate(item.createdAt)
                    ?.let { SimpleDateFormat("HH.mm", LOCALE_ID).format(it) }
                    .orEmpty()
                b.notificationUnread.visibility = if (item.isRead) View.GONE else View.VISIBLE
                b.notificationUnread.contentDescription =
                    if (item.isRead) null else getString(R.string.ghm_belum_dibaca)

                val (ikon, latar) = ikonUntuk(item.type)
                b.notificationIcon.setImageResource(ikon)
                b.notificationIcon.imageTintList = ColorStateList.valueOf(warna(
                    if (latar == R.color.ghm_terpal_100) R.color.ghm_daun_900 else R.color.ghm_daun_700
                ))
                b.notificationIconBox.backgroundTintList = ColorStateList.valueOf(warna(latar))

                // Satu kartu putih per kelompok: sudut membulat di baris pertama & terakhir.
                b.root.setBackgroundResource(
                    when {
                        baris.pertama && baris.terakhir -> R.drawable.bg_ghm_kartu
                        baris.pertama -> R.drawable.bg_ghm_baris_atas
                        baris.terakhir -> R.drawable.bg_ghm_baris_bawah
                        else -> R.drawable.bg_ghm_baris_tengah
                    }
                )
                b.notificationDivider.visibility = if (baris.terakhir) View.GONE else View.VISIBLE
                b.root.setPadding(dp(16), if (baris.pertama) dp(4) else 0, dp(16), if (baris.terakhir) dp(4) else 0)

                b.root.getChildAt(0).setOnClickListener { onItemClicked(item) }
            }
        }
    }

    /** Ikon & warna kotak dari field `type` (panen, prediksi, pencacahan). */
    private fun ikonUntuk(type: String?): Pair<Int, Int> = when (type?.lowercase()) {
        "panen" -> R.drawable.ic_ghm_maggot to R.color.ghm_terpal_100
        "prediksi" -> R.drawable.ic_ghm_grafik to R.color.ghm_tunas_100
        else -> R.drawable.ic_ghm_sampah to R.color.ghm_tunas_100
    }

    private fun warna(@ColorRes id: Int) = ContextCompat.getColor(this, id)

    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()

    companion object {
        private const val TIPE_JUDUL = 0
        private const val TIPE_NOTIF = 1
    }
}
