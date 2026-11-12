package com.codelabs.gardahijau.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.codelabs.gardahijau.R
import com.codelabs.gardahijau.databinding.ItemCardPrediksiSiklusBinding
import com.codelabs.gardahijau.model.remote.response.prediksi.DataItem
import com.codelabs.gardahijau.ui.util.LOCALE_ID
import com.codelabs.gardahijau.ui.util.formatKg
import com.codelabs.gardahijau.ui.util.parseServerDate
import com.codelabs.gardahijau.ui.widget.TahapSiklus
import com.codelabs.gardahijau.ui.widget.tampilkan
import java.text.SimpleDateFormat

data class KartuSiklus(
    val data: DataItem,
    /** Selisih hari kalender sejak tanggalMulai; null bila tanggal tidak terbaca. */
    val hariKe: Int?,
    /** null bila fase belum dimuat / gagal dimuat: pita disembunyikan. */
    val tahap: TahapSiklus?,
    /** Hasil dari GET prediksi/siklus/{id}; null bila belum ada. */
    val hasilKg: Double?,
    val dimuat: Boolean,
)

class SiklusAdapter(
    private val onItemClick: (DataItem) -> Unit = {}
) : RecyclerView.Adapter<SiklusAdapter.ViewHolder>() {

    private val items = mutableListOf<KartuSiklus>()

    fun setItems(newItems: List<KartuSiklus>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val binding: ItemCardPrediksiSiklusBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: KartuSiklus) {
            val context = binding.root.context
            binding.tvJudulSiklus.text = context.getString(R.string.ghm_siklus_ke, item.data.id)

            binding.tvHariKe.visibility = if (item.hariKe == null) View.GONE else View.VISIBLE
            binding.tvHariKe.text = when {
                item.hariKe == null -> ""
                item.hariKe <= 0 -> context.getString(R.string.ghm_mulai_hari_ini)
                else -> context.getString(R.string.ghm_hari_ke, item.hariKe)
            }

            if (item.tahap == null) {
                binding.pita.root.visibility = View.GONE
            } else {
                binding.pita.root.visibility = View.VISIBLE
                binding.pita.tampilkan(item.tahap)
            }

            val mulai = parseServerDate(item.data.tanggalMulai)
                ?.let { SimpleDateFormat("d MMM yyyy", LOCALE_ID).format(it) }
            val keteranganMulai = mulai?.let { context.getString(R.string.ghm_mulai_tanggal, it) }.orEmpty()

            when {
                !item.dimuat -> {
                    binding.tvPerkiraan.text = context.getString(R.string.ghm_memuat)
                    binding.tvKeterangan.text = keteranganMulai
                }
                item.hasilKg != null && item.hasilKg > 0 -> {
                    binding.tvPerkiraan.text = context.getString(R.string.ghm_perkiraan_kg, formatKg(item.hasilKg))
                    binding.tvKeterangan.text = keteranganMulai
                }
                item.tahap == TahapSiklus.TELUR || item.tahap == TahapSiklus.TETAS -> {
                    binding.tvPerkiraan.text = context.getString(R.string.ghm_belum_ada_perkiraan)
                    binding.tvKeterangan.text = context.getString(R.string.ghm_catat_fase_besar_dulu)
                }
                else -> {
                    binding.tvPerkiraan.text = context.getString(R.string.ghm_belum_ada_perkiraan)
                    binding.tvKeterangan.text = keteranganMulai
                }
            }
            binding.tvKeterangan.visibility =
                if (binding.tvKeterangan.text.isNullOrEmpty()) View.GONE else View.VISIBLE

            binding.root.setOnClickListener { onItemClick(item.data) }
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
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size
}
