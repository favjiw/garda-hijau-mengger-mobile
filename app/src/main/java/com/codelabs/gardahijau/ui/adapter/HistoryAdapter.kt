package com.codelabs.gardahijau.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.codelabs.gardahijau.R
import com.codelabs.gardahijau.databinding.ItemCardHistoryPencacahanBinding
import com.codelabs.gardahijau.model.remote.response.hitoryPencacahan.DataItem
import com.codelabs.gardahijau.ui.util.LOCALE_ID
import com.codelabs.gardahijau.ui.util.formatKg
import com.codelabs.gardahijau.ui.util.parseServerDate
import java.text.SimpleDateFormat

class HistoryAdapter : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {
    private val items = mutableListOf<DataItem>()
    private var onItemClickListener: ((DataItem) -> Unit)? = null

    private val dayFormat = SimpleDateFormat("d", LOCALE_ID)
    private val monthFormat = SimpleDateFormat("MMM", LOCALE_ID)
    private val timeFormat = SimpleDateFormat("HH.mm", LOCALE_ID)

    fun setItems(newItems: List<DataItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    fun setOnItemClickListener(listener: (DataItem) -> Unit) {
        onItemClickListener = listener
    }

    inner class ViewHolder(private val binding: ItemCardHistoryPencacahanBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: DataItem) {
            val context = binding.root.context
            val date = parseServerDate(item.tanggalWaktu)

            binding.tvTanggalHari.text = date?.let { dayFormat.format(it) } ?: "-"
            binding.tvTanggalBulan.text = date?.let { monthFormat.format(it) } ?: ""
            binding.tvJumlahVolumeSampah.text =
                context.getString(R.string.ghm_riwayat_judul_item, formatKg(item.totalSampah))

            val time = date?.let { timeFormat.format(it) }
            val note = item.catatan?.trim().orEmpty()
            binding.tvHasilPencacahan.text = when {
                time == null -> note
                note.isEmpty() -> context.getString(R.string.ghm_riwayat_pukul, time)
                else -> context.getString(R.string.ghm_riwayat_pukul_catatan, time, note)
            }

            binding.root.setOnClickListener {
                onItemClickListener?.invoke(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCardHistoryPencacahanBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size
}
