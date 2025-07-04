package com.example.aman.data

import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Count
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.util.Log
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class VideoRepository {
    private val client = SupabaseClient.client
    private val videosTable = "video"
    private val categoriesTable = "categories"
    private val TAG = "VideoRepository"


    suspend fun fetchAllData(): Pair<List<Category>, List<VideoWithCategory>> = coroutineScope {
        try {
            Log.d(TAG, "Fetching ALL data from Supabase in parallel")
            
            // Fetch categories and videos with their category info in parallel
            val categoriesDeferred = async(Dispatchers.IO) {
                client.postgrest.from(categoriesTable)
                    .select {
                        order("name", Order.ASCENDING)
                    }
                    .decodeList<Category>()
            }
            
            val videosDeferred = async(Dispatchers.IO) {
                getVideosWithCategoryInfo()
            }
            
            // Wait for both results
            val categories = categoriesDeferred.await()
            val videos = videosDeferred.await()
            
            Log.d(TAG, "Successfully fetched ${categories.size} categories and ${videos.size} videos with their category info")
            
            Pair(categories, videos)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching all data", e)
            e.printStackTrace()
            Pair(emptyList(), emptyList())
        }
    }
    
    suspend fun getVideosByCategory(category_id: String): List<Video> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Fetching videos for category ID: $category_id")
            
            // Define specific columns to retrieve
            val columns = Columns.list(
                "id",
                "title",
                "url",
                "category_id",
                "thumbnail_url"
            )
            
            val videos = client.postgrest.from(videosTable)
                .select(columns = columns) {
                    eq("category_id", category_id)
                }
                .decodeList<Video>()
                .sortedBy { it.title }
                
            Log.d(TAG, "Found ${videos.size} videos for category ID: $category_id")
            videos.forEach { 
                Log.d(TAG, "Video: ${it.title}, ID: ${it.id}, URL: ${it.url}, Thumbnail: ${it.thumbnailUrl}")
            }
            
            videos
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching videos for category ID: $category_id", e)
            e.printStackTrace()
            emptyList()
        }
    }
    
    suspend fun getAllVideos(): List<Video> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Fetching all videos")
            
            // Define columns to retrieve
            val columns = Columns.list(
                "id",
                "title",
                "url",
                "category_id",
                "thumbnail_url"
            )
            
            val videos = client.postgrest.from(videosTable)
                .select(columns = columns) {
                    order("title", Order.ASCENDING)
                }
                .decodeList<Video>()
                
            Log.d(TAG, "Found ${videos.size} videos in total")
            videos.forEach { 
                Log.d(TAG, "Video: ${it.title}, Category ID: ${it.categoryId}, Thumbnail: ${it.thumbnailUrl}")
            }
            
            videos
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching all videos", e)
            e.printStackTrace()
            emptyList()
        }
    }
    
    /**
     * Gets the count of all videos in the database
     */
    suspend fun getVideoCount(): Int = withContext(Dispatchers.IO) {
        try {
            // Get all videos and count them instead of using the count function
            val videos = getAllVideos()
            val count = videos.size
                
            Log.d(TAG, "Total video count: $count")
            count
        } catch (e: Exception) {
            Log.e(TAG, "Error getting video count", e)
            0
        }
    }
    
    /**
     * Gets the count of all categories in the database
     */
    suspend fun getCategoryCount(): Int = withContext(Dispatchers.IO) {
        try {
            // Get all categories and count them instead of using the count function
            val categories = client.postgrest.from(categoriesTable)
                .select()
                .decodeList<Category>()
            val count = categories.size
                
            Log.d(TAG, "Total category count: $count")
            count
        } catch (e: Exception) {
            Log.e(TAG, "Error getting category count", e)
            0
        }
    }
    
    /**
     * Fetches all videos and organizes them by category ID
     * @return Map of category ID to list of videos in that category
     */
    suspend fun getAllVideosByCategory(): Map<String, List<Video>> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Fetching and organizing all videos by category")
            val allVideos = getAllVideos()
            val videosByCategory = allVideos.groupBy { it.categoryId }
            
            Log.d(TAG, "Organized videos into ${videosByCategory.size} categories")
            videosByCategory.forEach { (categoryId, videos) ->
                Log.d(TAG, "Category $categoryId has ${videos.size} videos")
            }
            
            videosByCategory
        } catch (e: Exception) {
            Log.e(TAG, "Error organizing videos by category", e)
            e.printStackTrace()
            emptyMap()
        }
    }
    
    /**
     * Fetches videos with their category information in a single query using Supabase foreign table query
     * This is more efficient than separate queries
     */
    suspend fun getVideosWithCategoryInfo(): List<VideoWithCategory> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Fetching videos with category info")
            
            // Define a raw query to join videos with their categories
            val columns = Columns.raw("""
                id,
                title,
                url,
                category_id,
                thumbnail_url,
                categories:category_id (
                  id, 
                  name
                )
            """.trimIndent())
            
            val videosWithCategories = client.postgrest.from(videosTable)
                .select(columns = columns)
                .decodeList<VideoWithCategory>()
                .sortedBy { it.title }
                
            Log.d(TAG, "Found ${videosWithCategories.size} videos with category info")
            videosWithCategories.forEach { video ->
                Log.d(TAG, "Video: ${video.title}, Category: ${video.category?.name ?: "Unknown"}, Thumbnail: ${video.thumbnailUrl}")
            }
            
            videosWithCategories
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching videos with category info", e)
            e.printStackTrace()
            emptyList()
        }
    }
    
    /**
     * Fetches videos with pagination support for large datasets
     * @param pageSize Number of videos to fetch per page
     * @param pageNumber Page number to fetch (starting from 0)
     */
    suspend fun getPaginatedVideos(pageSize: Int = 20, pageNumber: Int = 0): List<Video> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Fetching paginated videos: page $pageNumber, size $pageSize")
            
            val offset = pageNumber * pageSize
            
            val videos = client.postgrest.from(videosTable)
                .select {
                    limit(pageSize.toString(), offset.toString())
                    order("title", Order.ASCENDING)
                }
                .decodeList<Video>()
                
            Log.d(TAG, "Fetched ${videos.size} videos for page $pageNumber")
            
            videos
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching paginated videos", e)
            e.printStackTrace()
            emptyList()
        }
    }

    private fun limit(count: String, foreignTable: String) {

    }

    /**
     * Refreshes all data and returns both videos and categories
     */
    suspend fun refreshAllData(): Pair<List<Category>, List<Video>> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Refreshing all data")
            
            // Get categories
            val categories = client.postgrest.from(categoriesTable)
                .select()
                .decodeList<Category>()
                
            // Get videos
            val videos = getAllVideos()
            
            Log.d(TAG, "Refreshed ${categories.size} categories and ${videos.size} videos")
            
            Pair(categories, videos)
        } catch (e: Exception) {
            Log.e(TAG, "Error refreshing all data", e)
            e.printStackTrace()
            Pair(emptyList(), emptyList())
        }
    }
    
    /**
     * Fetches initial data in parallel for optimal loading performance
     * Used for instant load of critical UI components
     */
    suspend fun getInitialData(): InitialDataBundle {
        return withContext(Dispatchers.IO) {
            Log.d(TAG, "Fetching initial data in parallel")
            
            // Parallel fetching
            val featuredVideosDeferred = async { getAllVideos().take(5) }
            val categoriesDeferred = async { 
                client.postgrest.from(categoriesTable)
                    .select()
                    .decodeList<Category>() 
            }
            val firstVideosDeferred = async { getPaginatedVideos(pageSize = 20, pageNumber = 0) }
            
            // Convert featured videos to slider items
            val sliderItems = featuredVideosDeferred.await().map { video ->
                SliderItem(
                    imageUrl = video.thumbnailUrl ?: "",
                    title = video.title,
                    videoId = video.id
                )
            }
            
            InitialDataBundle(
                slider = sliderItems,
                categories = categoriesDeferred.await(),
                videos = firstVideosDeferred.await()
            )
        }
    }
    
    companion object {
        suspend fun getVideosByCategory(categoryId: String): List<Video> {
            return VideoRepository().getVideosByCategory(categoryId)
        }
        
        suspend fun getAllVideos(): List<Video> {
            return VideoRepository().getAllVideos()
        }
    }
} 