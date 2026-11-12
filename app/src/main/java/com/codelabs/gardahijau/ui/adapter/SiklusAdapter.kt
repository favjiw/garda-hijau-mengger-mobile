package com.codelabs.gardahijau.ui.adapter

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.codelabs.gardahijau.R
import com.codelabs.gardahijau.databinding.ItemCardPrediksiSiklusBinding
import com.codelabs.gardahijau.databinding.ViewGhmPitaSiklusBinding
import com.codelabs.gardahijau.model.remote.response.prediksi.DataItem
import com.codelabs.gardahijau.ui.util.LOCALE_ID
import com.codelabs.gardahijau.ui.util.formatKg
import com.codelabs.gardahijau.ui.util.parseServerDate
import java.text.SimpleDateFormat

/**
 * Tahap siklus yang bisa dibaca dari daftar fase.
 * Fase PANEN dibuat otomatis bersama PEMBESARAN (lihat TambahFaseViewModel), jadi tidak
 * menandakan larva siap panen. Karena itu tahap "Panen" di pita tidak pernah aktif.
 */
enum class TahapSiklus { TELUR, TETAS, BESAR }

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
                tampilkanPita(binding.pita, item.tahap)
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

        /** Ruas sebelum tahap sekarang: hijau. Tahap sekarang: kuning + label tebal. Sesudahnya: abu. */
        private fun tampilkanPita(pita: ViewGhmPitaSiklusBinding, tahap: TahapSiklus) {
            val ruas = listOf(
                pita.ruasTelur to pita.labelTelur,
                pita.ruasTetas to pita.labelTetas,
                pita.ruasBesar to pita.labelBesar,
                pita.ruasPanen to pita.labelPanen,
            )
            val aktif = tahap.ordinal
            ruas.forEachIndexed { index, (batang, label) ->
                when {
                    index < aktif -> setRuas(batang, label, R.color.ghm_daun_700, R.color.ghm_tinta, tebal = false)
                    index == aktif -> setRuas(batang, label, R.color.ghm_terpal_400, R.color.ghm_tinta, tebal = true)
                    else -> setRuas(batang, label, R.color.ghm_garis, R.color.ghm_tinta_redup, tebal = false)
                }
            }
            val namaTahap = ruas[aktif].second.text
            pita.root.contentDescription =
                pita.root.context.getString(R.string.ghm_tahap_sekarang, namaTahap)
            pita.root.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
            ruas.forEach { (_, label) -> label.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO }
        }

        private fun setRuas(batang: View, label: TextView, @ColorRes warnaBatang: Int, @ColorRes warnaLabel: Int, tebal: Boolean) {
            val context = batang.context
            batang.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, warnaBatang))
            label.setTextColor(ContextCompat.getColor(context, warnaLabel))
            label.typeface = ResourcesCompat.getFont(
                context,
                if (tebal) R.font.atkinson_bold else R.font.atkinson_regular
            )
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
