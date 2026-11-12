package com.codelabs.gardahijau.model.remote.response.auth.login

import com.google.gson.annotations.SerializedName

data class LoginResponse(

	@field:SerializedName("message")
	val message: String,

	@field:SerializedName("user")
	val user: User,

	@field:SerializedName("token")
	val token: String
)

data class User(

	@field:SerializedName("rw")
	val rw: String,

	@field:SerializedName("id")
	val id: Int,

	@field:SerializedName("username")
	val username: String
)
