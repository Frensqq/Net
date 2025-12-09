package com.example.networklibrary.domain.model

data class Error400(
    val status:Int,
    val message: String,
    val data: Object
)
