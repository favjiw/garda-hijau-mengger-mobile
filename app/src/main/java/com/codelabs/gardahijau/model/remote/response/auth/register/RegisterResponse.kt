package com.codelabs.gardahijau.model.remote.response.auth.register

import com.google.gson.annotations.SerializedName

data class RegisterResponse(

	@field:SerializedName("message")
	val message: String,

	@field:SerializedName("user")
	val user: User
)

data class User(

	@field:SerializedName("createdAt")
	val createdAt: String,

	@field:SerializedName("password")
	val password: String,

	@field:SerializedName("rw")
	val rw: String,

	@field:SerializedName("id")
	val id: Int,

	@field:SerializedName("username")
	val username: String
)
