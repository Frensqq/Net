package com.example.networklibrary.domain.model

import android.net.Uri

data class RequestProjectImage(


val title: String,
val typeProject: String,
val user_id: String,
val dateStart: String,
val dateEnd: String,
val gender: String,
val description_source: String,
val category: String,
val imageUri: Uri? = null,
val imageFileName: String = ""

)
