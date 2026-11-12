package com.codelabs.gardahijau.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.codelabs.gardahijau.databinding.ItemCardDetailSiklusBinding
import com.codelabs.gardahijau.model.remote.response.prediksi.DataGetFase
import java.text.SimpleDateFormat
import java.util.*


class DetailSiklusAdapter(
    private val onFaseClick: (DataGetFase) -> Unit = {},
    private val onEmptyFaseClick: (String) -> Unit = {}
) : RecyclerView.Adapter<DetailSiklusAdapter.ViewHolder>() {

    private val faseList = mutableListOf<DataGetFase>()

    data class FaseItem(
        val data: DataGetFase? = null,
        val jeniFase: String,
        val isEmpty: Boolean = false
    )

    private val displayList = mutableListOf<FaseItem>()

    fun setItems(newItems: List<DataGetFase>) {
        faseList.clear()
        faseList.addAll(newItems)

        // Daftar fase yang harus ditampilkan
        val requiredFases = listOf("PENETASAN", "PEMBESARAN", "PANEN")
        displayList.clear()

        requiredFases.forEach { requiredFase ->
            val existingFase = faseList.find { it.jenis.uppercase() == requiredFase }
            if (existingFase != null) {
                displayList.add(FaseItem(data = existingFase, jeniFase = requiredFase, isEmpty = false))
            } else {
                // Tambahkan fase kosong
                displayList.add(FaseItem(data = null, jeniFase = requiredFase, isEmpty = true))
            }
        }

        notifyDataSetChanged()
    }

    inner class ViewHolder(private val binding: ItemCardDetailSiklusBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: FaseItem, position: Int) {
            if (item.isEmpty) {
                binding.tvTitle.text = item.jeniFase
                binding.tvDate.text = "Belum diatur"
                binding.tvDesc.text = "Klik untuk menambah data ${item.jeniFase.lowercase()}"
                binding.tvDesc.setTextColor(binding.root.context.getColor(android.R.color.darker_gray))

                if (item.jeniFase != "PENETASAN") {
                    binding.arrowIcon.visibility = View.VISIBLE
                    binding.arrowIcon.setOnClickListener {
                        onEmptyFaseClick(item.jeniFase)
                    }
                } else {
                    binding.arrowIcon.visibility = View.GONE
                }
            } else {
                val faseData = item.data!!
                binding.tvTitle.text = faseData.jenis
                binding.tvDate.text = formatDate(faseData.tanggal)
                binding.tvDesc.text = faseData.keterangan
                binding.tvDesc.setTextColor(binding.root.context.getColor(android.R.color.black))

                when (faseData.jenis.uppercase()) {
                    "PANEN" -> {
                        binding.arrowIcon.visibility = View.VISIBLE
                        binding.arrowIcon.setOnClickListener {
                            onFaseClick(faseData)
                        }
                    }
                    "PEMBESARAN" -> {
                        binding.arrowIcon.visibility = View.GONE
                    }
                    "PENETASAN" -> {
                        binding.arrowIcon.visibility = View.GONE
                    }
                    else -> {
                        binding.arrowIcon.visibility = View.GONE
                    }
                }
            }

            // Hide line connector for last item
            if (position == displayList.size - 1) {
                binding.lineConnector.visibility = View.GONE
            } else {
                binding.lineConnector.visibility = View.VISIBLE
            }
        }


        private fun formatDate(dateString: String): String {
            return try {
                val inputFormat = if (dateString.contains("T")) {
                    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                } else {
                    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                }

                val outputFormat = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
                val date = inputFormat.parse(dateString)
                outputFormat.format(date ?: Date())
            } catch (e: Exception) {
                dateString
            }
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
        holder.bind(displayList[position], position)
    }

    override fun getItemCount() = displayList.size
}


