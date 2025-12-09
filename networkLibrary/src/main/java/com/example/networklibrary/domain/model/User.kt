package com.example.networklibrary.domain.model

data class User(
    val collectionId:String,
    val collectionName:String,
    val created:String,
    val emailVisibility:Boolean,
    val firstname: String,
    val id:String,
    val lastname: String,
    val secondname:String,
    val updated:String,
    val verified:Boolean,
    val datebirthday: String,
    val gender: String
)
