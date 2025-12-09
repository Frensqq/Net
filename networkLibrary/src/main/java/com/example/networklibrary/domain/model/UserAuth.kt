package com.example.networklibrary.domain.model


data class UserAuth(

    val collectionId: String,
    val collectionName: String,
    val id: String,
    val collectionRef: String,
    val recordRef: String,
    val fingerprint: String,
    val created: String,
    val updated: String
)
