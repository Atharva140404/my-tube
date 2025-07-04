package com.example.aman.data.network

import android.util.Log
import com.example.aman.data.Category
import com.example.aman.data.Video
import com.example.aman.data.VideoWithCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Example class showing how to use the REST API methods in your app
 */
class RestApiExample {
    private val TAG = "RestApiExample"
    
    // Use the SupabaseRestClient (Ktor-based implementation)
    private val restClient = SupabaseRestClient()
    
    /**
     * Example of how to fetch videos using the REST API
     * This method demonstrates both approaches (Ktor and OkHttp)
     */
    suspend fun fetchVideosExample(): Pair<List<Video>, List<Video>> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Fetching videos using both REST API approaches")
            
            // Approach 1: Using SupabaseRestClient (Ktor-based)
            val videosWithKtor = restClient.getAllVideos()
            Log.d(TAG, "Fetched ${videosWithKtor.size} videos using Ktor")
            
            // Approach 2: Using CurlStyleRequests (OkHttp-based)
            val videosWithOkHttp = CurlStyleRequests.getAllVideos()
            Log.d(TAG, "Fetched ${videosWithOkHttp.size} videos using OkHttp")
            
            // Return both results for comparison
            Pair(videosWithKtor, videosWithOkHttp)
        } catch (e: Exception) {
            Log.e(TAG, "Error in fetchVideosExample", e)
            Pair(emptyList(), emptyList())
        }
    }
    
    /**
     * Example of how to fetch videos by category using the REST API
     */
    suspend fun fetchVideosByCategoryExample(categoryId: String): List<Video> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Fetching videos for category ID: $categoryId using REST API")
            
            // Using SupabaseRestClient (Ktor-based)
            val videos = restClient.getVideosByCategory(categoryId)
            Log.d(TAG, "Fetched ${videos.size} videos for category ID: $categoryId using REST API")
            
            videos
        } catch (e: Exception) {
            Log.e(TAG, "Error in fetchVideosByCategoryExample", e)
            emptyList()
        }
    }
    
    /**
     * Example of how to fetch videos with their category information using the REST API
     */
    suspend fun fetchVideosWithCategoryInfoExample(): List<VideoWithCategory> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Fetching videos with category info using REST API")
            
            // Using SupabaseRestClient (Ktor-based)
            val videos = restClient.getVideosWithCategoryInfo()
            Log.d(TAG, "Fetched ${videos.size} videos with category info using REST API")
            
            videos
        } catch (e: Exception) {
            Log.e(TAG, "Error in fetchVideosWithCategoryInfoExample", e)
            emptyList()
        }
    }
    
    /**
     * Example of how to fetch all categories using the REST API
     */
    suspend fun fetchCategoriesExample(): List<Category> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Fetching categories using REST API")
            
            // Using SupabaseRestClient (Ktor-based)
            val categories = restClient.getAllCategories()
            Log.d(TAG, "Fetched ${categories.size} categories using REST API")
            
            categories
        } catch (e: Exception) {
            Log.e(TAG, "Error in fetchCategoriesExample", e)
            emptyList()
        }
    }
    
    /**
     * Example of how to fetch popular videos using the REST API
     */
    suspend fun fetchPopularVideosExample(limit: Int = 10): List<Video> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Fetching popular videos using REST API")
            
            // Using CurlStyleRequests (OkHttp-based)
            val videos = CurlStyleRequests.getPopularVideos(limit)
            Log.d(TAG, "Fetched ${videos.size} popular videos using REST API")
            
            videos
        } catch (e: Exception) {
            Log.e(TAG, "Error in fetchPopularVideosExample", e)
            emptyList()
        }
    }
} 