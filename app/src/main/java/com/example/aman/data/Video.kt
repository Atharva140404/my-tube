package com.example.aman.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Video(
    @SerialName("id")
    val id: String? = null,
    @SerialName("title")
    val title: String,
    @SerialName("url")
    val url: String,
    @SerialName("category_id")
    val categoryId: String,
    @SerialName("thumbnail_url")
    val thumbnailUrl: String? = null,
    @SerialName("channel_name")
    val channelName: String = "",
    @SerialName("view_count")
    val viewCount: String = "0",
    @SerialName("upload_date")
    val uploadDate: String = ""
)