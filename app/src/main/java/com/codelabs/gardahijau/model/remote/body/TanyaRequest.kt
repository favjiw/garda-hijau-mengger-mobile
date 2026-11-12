package com.codelabs.gardahijau.model.remote.body

/** Body POST /api/chatbot/tanya. Maksimal 2000 karakter. */
data class TanyaRequest(
    val pertanyaan: String,
)
