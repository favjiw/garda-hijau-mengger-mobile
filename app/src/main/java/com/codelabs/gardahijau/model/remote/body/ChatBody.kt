package com.codelabs.gardahijau.model.remote.body

import kotlinx.serialization.Serializable

@Serializable
data class ChatBody(
    val question: String,
)
