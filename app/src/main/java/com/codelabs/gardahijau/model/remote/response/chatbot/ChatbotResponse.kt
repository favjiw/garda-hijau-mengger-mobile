package com.codelabs.gardahijau.model.remote.response.chatbot

/**
 * Balasan POST /api/chatbot/tanya.
 * Hanya `data.jawaban` yang ditampilkan; `data.sumber` berisi potongan mentah PDF jadi tidak dipakai.
 */
data class ChatbotResponse(
    val success: Boolean? = null,
    val data: ChatbotData? = null,
)

data class ChatbotData(
    val jawaban: String? = null,
)
