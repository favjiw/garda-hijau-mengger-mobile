package com.codelabs.gardahijau.ui.adapter

import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.codelabs.gardahijau.databinding.ItemCardPrediksiSiklusBinding
import com.codelabs.gardahijau.model.local.data.SiklusItem
import com.codelabs.gardahijau.model.remote.response.prediksi.DataItem

class SiklusAdapter(
    private val onItemClick: (DataItem) -> Unit = {}
) : RecyclerView.Adapter<SiklusAdapter.ViewHolder>() {

    private val items = mutableListOf<SiklusItem>()
    private val dataItems = mutableListOf<DataItem>()

    fun setItems(newItems: List<SiklusItem>, newDataItems: List<DataItem>) {
        items.clear()
        items.addAll(newItems)
        dataItems.clear()
        dataItems.addAll(newDataItems)
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val binding: ItemCardPrediksiSiklusBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SiklusItem, dataItem: DataItem) {
            binding.appCompatTextView4.text = item.title
            binding.tvEstimasiPanen.text = item.label
            binding.tvResultPrediksiTotal.text = item.result
            binding.tvEstimasiTanggal.text = item.date

            binding.root.setOnClickListener {
                onItemClick(dataItem)
            }

            binding.tvSeeDetail.setOnClickListener {
                onItemClick(dataItem)
            }

            val resultText = item.result ?: "Belum tersedia"
            binding.tvResultPrediksiTotal.text = resultText

            // choose sizes in SP (prefer using dimens.xml for maintainability)
            val smallSp = 18f
            val largeSp = 60f

            if (resultText.equals("Belum tersedia", ignoreCase = true)) {
                binding.tvResultPrediksiTotal.setTextSize(TypedValue.COMPLEX_UNIT_SP, smallSp)
            } else {
                binding.tvResultPrediksiTotal.setTextSize(TypedValue.COMPLEX_UNIT_SP, largeSp)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCardPrediksiSiklusBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (position < items.size && position < dataItems.size) {
            holder.bind(items[position], dataItems[position])
        }
    }

    override fun getItemCount() = minOf(items.size, dataItems.size)
}
