package com.codelabs.gardahijau.ui.chatbot

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.codelabs.gardahijau.R
import com.codelabs.gardahijau.databinding.ActivityChatbotBinding
import com.codelabs.gardahijau.model.local.data.Author
import com.codelabs.gardahijau.model.local.data.Chat
import com.codelabs.gardahijau.model.remote.body.ChatBody
import com.codelabs.gardahijau.model.remote.network.ApiConfig
import com.codelabs.gardahijau.ui.MainActivity
import com.codelabs.gardahijau.ui.adapter.ChatAdapter
import com.codelabs.gardahijau.ui.util.applySystemBarPadding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class ChatbotActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatbotBinding
    private val adapter = ChatAdapter()
    private val chatHistory = ArrayList<Chat>()
    private var menunggu = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
        )
        binding = ActivityChatbotBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        binding.layoutKepala.applySystemBarPadding(top = true)
        // Kotak tulis pesan naik di atas keyboard.
        binding.chatContainer.applySystemBarPadding(bottom = true, includeIme = true)

        binding.sapaan.tvMessage.setText(R.string.ghm_gardabot_sapaan)
        binding.rvChat.layoutManager = LinearLayoutManager(this)
        binding.rvChat.adapter = adapter

        binding.btnKembali.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }

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
    }

    private fun kirim(pesan: String) {
        val text = pesan.trim()
        if (text.isEmpty() || menunggu) return
        menunggu = true
        binding.btnSend.isEnabled = false
        binding.layoutSaran.visibility = View.GONE
        binding.edtMessage.setText("")

        chatHistory.add(Chat(text = text, author = Author.SELF))
        chatHistory.add(Chat(text = getString(R.string.ghm_gardabot_mengetik), author = Author.AI, isTyping = true))
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
            binding.btnSend.isEnabled = true
            binding.layoutSaran.visibility = View.VISIBLE
            tampilkanPesan()
        }
    }

    /** Perbarui daftar pesan lalu gulir ke pesan terakhir. */
    private fun tampilkanPesan() {
        adapter.submitList(ArrayList(chatHistory)) {
            binding.scrollChat.post {
                val lastIndex = adapter.itemCount - 1
                val last = binding.rvChat.layoutManager?.findViewByPosition(lastIndex)
                val target = if (last != null) binding.rvChat.top + last.top else binding.rvChat.bottom
                binding.scrollChat.smoothScrollTo(0, target)
            }
        }
    }
}
