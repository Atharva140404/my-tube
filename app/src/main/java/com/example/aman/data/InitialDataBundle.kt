package com.example.aman.data

/**
 * Data class for bundling initial data loaded in parallel
 */
data class InitialDataBundle(
    val slider: List<SliderItem>,
    val categories: List<Category>,
    val videos: List<Video>
)
