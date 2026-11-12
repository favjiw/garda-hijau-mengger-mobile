package com.codelabs.gardahijau.ui.chatbot

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.codelabs.gardahijau.R
import com.codelabs.gardahijau.databinding.FragmentChatbotBinding
import com.codelabs.gardahijau.model.local.data.Author
import com.codelabs.gardahijau.model.local.data.Chat
import com.codelabs.gardahijau.model.remote.body.ChatBody
import com.codelabs.gardahijau.model.remote.network.ApiConfig
import com.codelabs.gardahijau.ui.adapter.ChatAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Tab GardaBot (dulu ChatbotActivity). Navigasi bawah & tinggi status bar diurus MainActivity.
 * Riwayat chat disimpan di fragment ini, jadi hilang saat aplikasi ditutup (sama seperti sebelumnya).
 */
@AndroidEntryPoint
class ChatbotFragment : Fragment() {

    private var _binding: FragmentChatbotBinding? = null
    private val binding get() = _binding!!

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
     * Kotak tulis pesan naik di atas keyboard. Bagian keyboard yang sudah tertutup
     * navigasi bawah (dan navigation bar) tidak dihitung dua kali.
     */
    private fun setupKeyboardInset() {
        val paddingAwal = binding.chatContainer.paddingBottom
        ViewCompat.setOnApplyWindowInsetsListener(binding.chatContainer) { container, insets ->
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
            val root = binding.root
            val lokasi = IntArray(2)
            root.getLocationInWindow(lokasi)
            val jarakKeBawahLayar = root.rootView.height - (lokasi[1] + root.height)
            container.updatePadding(bottom = paddingAwal + maxOf(0, ime - jarakKeBawahLayar))
            insets
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
            val service = ApiConfig.getChatbotApiService()
            val response = runCatching {
                withContext(Dispatchers.IO) {
                    service.getChatResponse(ChatBody(question = text))
                }
            }

            if (chatHistory.isNotEmpty() && chatHistory.last().isTyping) {
                chatHistory.removeAt(chatHistory.lastIndex)
            }
            response.onSuccess { res ->
                chatHistory.add(Chat(text = res.answer, author = Author.AI))
            }.onFailure {
                chatHistory.add(Chat(text = getString(R.string.ghm_gardabot_galat), author = Author.AI))
            }
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
