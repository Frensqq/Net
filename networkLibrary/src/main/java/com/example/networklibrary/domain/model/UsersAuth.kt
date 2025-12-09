package com.example.networklibrary.domain.model

data class UsersAuth(

    val page:Int,
    val perPage:Int,
    val totalPages: Int,
    val totalItems:Int,
    val items: List<UserAuth>

)
