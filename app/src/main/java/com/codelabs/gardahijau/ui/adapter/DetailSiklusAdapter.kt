package com.codelabs.gardahijau.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.codelabs.gardahijau.databinding.ItemCardDetailSiklusBinding

/** Satu baris di linimasa "Catatan fase". Teks sudah siap tampil. */
data class BarisFase(
    val judul: String,
    val tanggal: String,
    val keterangan: String,
)

class DetailSiklusAdapter : RecyclerView.Adapter<DetailSiklusAdapter.ViewHolder>() {

    private val items = mutableListOf<BarisFase>()

    fun setItems(newItems: List<BarisFase>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val binding: ItemCardDetailSiklusBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: BarisFase, isLast: Boolean) {
            binding.tvTitle.text = item.judul
            binding.tvDate.text = item.tanggal
            binding.tvDesc.text = item.keterangan
            binding.tvDesc.visibility = if (item.keterangan.isBlank()) View.GONE else View.VISIBLE
            // Garis rel tidak diteruskan setelah fase terakhir.
            binding.lineConnector.visibility = if (isLast) View.INVISIBLE else View.VISIBLE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCardDetailSiklusBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], isLast = position == items.lastIndex)
    }

    override fun getItemCount() = items.size
}
