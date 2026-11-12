package com.codelabs.gardahijau.model.remote.response.auth

/** Balasan GET /api/auth/me. Dipakai saat aplikasi dibuka untuk memastikan token masih berlaku. */
data class MeResponse(
    val success: Boolean? = null,
    val user: MeUser? = null,
)

data class MeUser(
    val id: Int? = null,
    val username: String? = null,
    val rw: String? = null,
)
