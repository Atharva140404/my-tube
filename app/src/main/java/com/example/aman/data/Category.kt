package com.example.aman.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Category(
    @SerialName("id")
    val id: String? = null,
    @SerialName("name")
    val name: String
) 