package com.example.networklibrary.domain.model

data class Project(

    val id: String,
    val collectionId: String,
    val collectionName: String,
    val created: String,
    val updated: String,
    val title: String,
    val dateStart: String,
    val dateEnd: String,
    val gender: String,
    val description_source: String,
    val category: String,
    val image: String,
    val user_id: String

)
