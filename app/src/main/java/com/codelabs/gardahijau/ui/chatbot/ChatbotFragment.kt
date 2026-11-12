package com.codelabs.gardahijau.ui.chatbot

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.codelabs.gardahijau.R
import com.codelabs.gardahijau.databinding.FragmentChatbotBinding
import com.codelabs.gardahijau.model.local.data.Author
import com.codelabs.gardahijau.model.local.data.Chat
import com.codelabs.gardahijau.model.remote.body.TanyaRequest
import com.codelabs.gardahijau.model.remote.network.MainApiService
import com.codelabs.gardahijau.model.remote.network.pesanGalat
import com.codelabs.gardahijau.ui.adapter.ChatAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Tab GardaBot (dulu ChatbotActivity). Navigasi bawah & tinggi status bar diurus MainActivity.
 * Riwayat chat disimpan di fragment ini, jadi hilang saat aplikasi ditutup (sama seperti sebelumnya).
 */
@AndroidEntryPoint
class ChatbotFragment : Fragment() {

    private var _binding: FragmentChatbotBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var mainApiService: MainApiService

    private val adapter = ChatAdapter()
    private val chatHistory = ArrayList<Chat>()
    private var menunggu = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatbotBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupKeyboardInset()

        binding.sapaan.tvMessage.setText(R.string.ghm_gardabot_sapaan)
        binding.rvChat.layoutManager = LinearLayoutManager(requireContext())
        binding.rvChat.adapter = adapter
        if (chatHistory.isNotEmpty()) tampilkanPesan()

        // Saran langsung dikirim sebagai pertanyaan.
        listOf(binding.btnSuggestion1, binding.btnSuggestion2, binding.btnSuggestion3).forEach { saran ->
            saran.setOnClickListener { kirim(saran.text.toString()) }
        }

        binding.btnSend.setOnClickListener { kirim(binding.edtMessage.text.toString()) }
        binding.edtMessage.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                kirim(binding.edtMessage.text.toString())
                true
            } else {
                false
            }
        }
        tampilkanStatus()
    }

    /**
     * Keyboard ditangani MainActivity (navigasi bawah disembunyikan, isi tab menyempit).
     * Di sini: saat area chat menyempit, gulir ke pesan terakhir supaya tetap terlihat.
     */
    private fun setupKeyboardInset() {
        binding.scrollChat.addOnLayoutChangeListener { scroll, _, top, _, bottom, _, oldTop, _, oldBottom ->
            if (bottom - top < oldBottom - oldTop) {
                scroll.post {
                    val isi = binding.scrollChat.getChildAt(0) ?: return@post
                    binding.scrollChat.scrollTo(0, isi.height)
                }
            }
        }
    }

    private fun kirim(pesan: String) {
        val text = pesan.trim()
        if (text.isEmpty() || menunggu) return
        menunggu = true
        binding.edtMessage.setText("")

        chatHistory.add(Chat(text = text, author = Author.SELF))
        chatHistory.add(Chat(text = getString(R.string.ghm_gardabot_mengetik), author = Author.AI, isTyping = true))
        tampilkanStatus()
        tampilkanPesan()

        lifecycleScope.launch {
            // POST /api/chatbot/tanya. Hanya data.jawaban yang ditampilkan (data.sumber tidak).
            val jawaban = try {
                mainApiService.tanyaChatbot(TanyaRequest(pertanyaan = text)).data?.jawaban
                    ?.takeIf { it.isNotBlank() }
                    ?: getString(R.string.ghm_gardabot_galat)
            } catch (e: Exception) {
                // 429: terlalu banyak pertanyaan; 503: layanan AI tidak tersedia; lainnya: jaringan.
                e.pesanGalat()
            }

            if (chatHistory.isNotEmpty() && chatHistory.last().isTyping) {
                chatHistory.removeAt(chatHistory.lastIndex)
            }
            chatHistory.add(Chat(text = jawaban, author = Author.AI))
            menunggu = false

            // Tab bisa saja sudah ditinggal saat jawaban datang.
            if (_binding != null) {
                tampilkanStatus()
                tampilkanPesan()
            }
        }
    }

    /** Tombol kirim & saran tidak aktif selama menunggu jawaban. */
    private fun tampilkanStatus() {
        binding.btnSend.isEnabled = !menunggu
        binding.layoutSaran.visibility = if (menunggu) View.GONE else View.VISIBLE
    }

    /** Perbarui daftar pesan lalu gulir ke pesan terakhir. */
    private fun tampilkanPesan() {
        adapter.submitList(ArrayList(chatHistory)) {
            val b = _binding ?: return@submitList
            b.scrollChat.post {
                val lastIndex = adapter.itemCount - 1
                val last = b.rvChat.layoutManager?.findViewByPosition(lastIndex)
                val target = if (last != null) b.rvChat.top + last.top else b.rvChat.bottom
                b.scrollChat.smoothScrollTo(0, target)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.rvChat.adapter = null
        _binding = null
    }
}
