package com.example.networklibrary.domain.model

data class ResponseProducts(
    val page:Int,
    val perPage:Int,
    val totalPages: Int,
    val totalItems :Int,
    val items: List<ProductItem>
)
