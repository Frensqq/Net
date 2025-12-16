package com.example.networklibrary.domain.model

data class RequestRegister(

    val email: String,
    val password: String,
    val passwordConfirm: String,
    val firstname: String,
    val secondname: String,
    val lastname: String,
    val datebirthday: String,
    val gender: String,
)
