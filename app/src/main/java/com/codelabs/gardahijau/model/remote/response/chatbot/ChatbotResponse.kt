package com.codelabs.gardahijau.model.remote.response.chatbot

import com.google.gson.annotations.SerializedName

data class ChatbotResponse(

	@field:SerializedName("answer")
	val answer: String,

	@field:SerializedName("question")
	val question: String,

	@field:SerializedName("sources")
	val sources: List<Any>,

	@field:SerializedName("tokens_used")
	val tokensUsed: Int,

	@field:SerializedName("model")
	val model: String,

	@field:SerializedName("num_sources")
	val numSources: Int
)
