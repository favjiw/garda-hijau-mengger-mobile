package com.codelabs.gardahijau.model.local.data

data class Chat(
    val text: String,
    val author: Author,
    val isTyping: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
) {
    val isFromMe = author == Author.SELF
}

enum class Author {
    AI, SELF
}
