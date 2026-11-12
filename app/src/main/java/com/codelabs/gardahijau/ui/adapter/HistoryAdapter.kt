package com.codelabs.gardahijau.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.codelabs.gardahijau.databinding.ItemCardHistoryPencacahanBinding
import com.codelabs.gardahijau.model.remote.response.hitoryPencacahan.DataItem
import java.text.SimpleDateFormat
import java.util.*

class HistoryAdapter : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {
    private val items = mutableListOf<DataItem>()
    private var onItemClickListener: ((DataItem) -> Unit)? = null

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
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))

            val formattedDate = try {
                val date = inputFormat.parse(item.tanggalWaktu)
                outputFormat.format(date ?: Date())
            } catch (e: Exception) {
                item.tanggalWaktu
            }

            binding.appCompatTextView4.text = formattedDate
            binding.tvJumlahVolumeSampah.text = "Jumlah Volume Sampah : ${item.totalSampah} Kg"
            binding.tvHasilPencacahan.text = "Catatan : ${item.catatan}"

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
