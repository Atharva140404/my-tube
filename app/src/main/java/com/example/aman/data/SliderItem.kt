package com.example.aman.data

/**
 * Data class for slider items in the ViewPager
 */
data class SliderItem(
    val imageUrl: String,
    val title: String,
    val description: String? = null,
    val tags: List<String>? = null,
    val videoId: String? = null
)
