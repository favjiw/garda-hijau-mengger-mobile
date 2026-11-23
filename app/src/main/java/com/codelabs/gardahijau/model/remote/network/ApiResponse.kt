package com.codelabs.gardahijau.model.remote.network

sealed class ApiResponse<out R> {
    data class Success<out T>(val data: T) : ApiResponse<T>()
    /** [penyebab] dipakai untuk memutuskan antrean offline (mis. galat jaringan). */
    data class Error(val errorMessage: String, val penyebab: Throwable? = null) : ApiResponse<Nothing>()
    object Empty : ApiResponse<Nothing>()
}