package com.example.aman.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a Video with its associated Category information.
 * This class is designed to work with Supabase's foreign table query
 * which allows fetching related tables in a single query.
 */
@Serializable
data class VideoWithCategory(
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
    
    @SerialName("categories")
    val category: Category? = null
) {
    /**
     * Converts this VideoWithCategory to a simple Video object
     */
    fun toVideo(): Video = Video(
        id = id,
        title = title,
        url = url,
        categoryId = categoryId,
        thumbnailUrl = thumbnailUrl
    )
} 