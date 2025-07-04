package com.example.aman.data

/**
 * Enum class for defining loading priorities
 */
enum class LoadPriority {
    HIGH,    // Critical UI elements that should load first (slider, categories)
    MEDIUM,  // Important content that should load soon after (first batch of videos)
    LOW      // Non-critical content that can load later (additional videos during pagination)
}

/**
 * Data class for associating components with their loading priorities
 */
data class PriorityComponent(
    val priority: LoadPriority,
    val id: String
)

/**
 * Default priority order for MyTube app components
 */
val defaultPriorityOrder = listOf(
    PriorityComponent(LoadPriority.HIGH, "slider"),
    PriorityComponent(LoadPriority.HIGH, "categories"),
    PriorityComponent(LoadPriority.MEDIUM, "first_videos"),
    PriorityComponent(LoadPriority.LOW, "additional_videos")
)
