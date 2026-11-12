package com.codelabs.gardahijau.ui.chatbot

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.codelabs.gardahijau.R
import com.codelabs.gardahijau.databinding.ActivityChatbotBinding
import com.codelabs.gardahijau.model.local.data.Author
import com.codelabs.gardahijau.model.local.data.Chat
import com.codelabs.gardahijau.model.remote.body.ChatBody
import com.codelabs.gardahijau.model.remote.network.ApiConfig
import com.codelabs.gardahijau.ui.adapter.ChatAdapter
import com.google.android.material.appbar.MaterialToolbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.toString

@AndroidEntryPoint
class ChatbotActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var adapter: ChatAdapter
    private lateinit var rvChat: RecyclerView
    private lateinit var binding: ActivityChatbotBinding
    private val chatHistory = ArrayList<Chat>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityChatbotBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        ViewCompat.setOnApplyWindowInsetsListener(binding.chatContainer) { view, insets ->
            val imeHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
            if (imeHeight > 0) {
                binding.materialCardView.visibility = View.GONE
                binding.rvChat.visibility = View.VISIBLE
            } else {
                if (chatHistory.isEmpty()) {
                    binding.materialCardView.visibility = View.VISIBLE
                    binding.rvChat.visibility = View.GONE
                }
            }
            view.translationY = if (imeHeight > 0) -imeHeight.toFloat() else 0f
            insets
        }


        toolbar = findViewById(R.id.topAppBar)
        toolbar.setNavigationOnClickListener {
            val intent = Intent(this, com.codelabs.gardahijau.ui.MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }

        rvChat = binding.rvChat
        adapter = ChatAdapter()
        rvChat.layoutManager = LinearLayoutManager(this)
        rvChat.adapter = adapter

        binding.rvChat.visibility = View.GONE

        binding.btnSuggestion1.setOnClickListener {
            binding.edtMessage.setText(binding.btnSuggestion1.text.toString())
            binding.edtMessage.setSelection(binding.edtMessage.text.length)
        }

        binding.btnSuggestion2.setOnClickListener {
            binding.edtMessage.setText(binding.btnSuggestion2.text.toString())
            binding.edtMessage.setSelection(binding.edtMessage.text.length)
        }

        binding.btnSuggestion3.setOnClickListener {
            binding.edtMessage.setText(binding.btnSuggestion3.text.toString())
            binding.edtMessage.setSelection(binding.edtMessage.text.length)
        }

        binding.btnSend.setOnClickListener {
            val text = binding.edtMessage.text.toString().trim()
            if (text.isEmpty()) return@setOnClickListener

            binding.logoSection.visibility = View.GONE
            binding.materialCardView.visibility = View.GONE
            binding.rvChat.visibility = View.VISIBLE

            chatHistory.add(Chat(text = text, author = Author.SELF))

            chatHistory.add(Chat(text = "Typing...", author = Author.AI, isTyping = true))

            adapter.submitList(ArrayList(chatHistory))

            binding.edtMessage.setText("")
            rvChat.scrollToPosition(chatHistory.size - 1)

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
                }.onFailure { err ->
                    chatHistory.add(Chat(text = "Error: ${err.message}", author = Author.AI))
                }

                adapter.submitList(ArrayList(chatHistory))
                rvChat.scrollToPosition(chatHistory.size - 1)
            }
        }
    }
}
