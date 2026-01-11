package com.example.networklibrary.domain.model

/**
 * Универсальный результат сетевых операций
 */
sealed class NetworkResult<out T> {
    data class Success<T>(val data: T) : NetworkResult<T>()
    data class Error(val error: ApiError) : NetworkResult<Nothing>()
    object NoInternet : NetworkResult<Nothing>()
}

/**
 * Модель ошибки API с использованием существующего Error400
 */
data class ApiError(
    val code: Int,
    val message: String,
    val details: Map<String, Any> = emptyMap()
)