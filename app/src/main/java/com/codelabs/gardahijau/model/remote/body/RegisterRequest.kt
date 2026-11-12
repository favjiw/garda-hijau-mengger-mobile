package com.codelabs.gardahijau.model.remote.body

data class RegisterRequest(
    val username: String,
    val rw: String,
    val password: String
)