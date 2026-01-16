package com.example.networklibrary.domain.model

sealed class NetworkResult<out T> {
    data class Success<T>(val data: T) : NetworkResult<T>()
    data class Error(val error: ApiError) : NetworkResult<Nothing>()

    //object Loading: NetworkResult<Nothing>()
    object NoInternet : NetworkResult<Nothing>()
}


data class ApiError(
    val code: Int,
    val message: String,
    val details: Map<String, Any> = emptyMap()
)