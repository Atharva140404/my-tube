package com.example.aman.data.network

import android.util.Log
import com.example.aman.data.Category
import com.example.aman.data.SupabaseClient
import com.example.aman.data.Video
import com.example.aman.data.VideoWithCategory
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

/**
 * Client for making direct REST API calls to Supabase
 * This provides an alternative way to access data using cURL-style HTTP requests
 */
class SupabaseRestClient {
    private val TAG = "SupabaseRestClient"
    
    // Use the same URL and key from the main SupabaseClient
    private val supabaseUrl = SupabaseClient.SUPABASE_URL
    private val supabaseKey = SupabaseClient.SUPABASE_ANON_KEY
    
    // Base URL for REST API
    private val restApiBaseUrl = "$supabaseUrl/rest/v1"
    
    // Reuse the HTTP client from SupabaseClient
    private val httpClient = HttpClient(io.ktor.client.engine.android.Android)
    
    // JSON parser with lenient mode
    private val json = Json { 
        ignoreUnknownKeys = true 
        isLenient = true
    }
    
    /**
     * Fetches all videos using direct REST API call
     */
    suspend fun getAllVideos(): List<Video> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Fetching all videos via REST API")
            
            val response: HttpResponse = httpClient.get("$restApiBaseUrl/video?select=*") {
                header("apikey", supabaseKey)
                header("Authorization", "Bearer $supabaseKey")
                header("Content-Type", "application/json")
            }
            
            val responseBody = response.body<String>()
            val videos = json.decodeFromString<List<Video>>(responseBody)
            
            Log.d(TAG, "Successfully fetched ${videos.size} videos via REST API")
            videos
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching videos via REST API", e)
            emptyList()
        }
    }
    
    /**
     * Fetches all categories using direct REST API call
     */
    suspend fun getAllCategories(): List<Category> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Fetching all categories via REST API")
            
            val response: HttpResponse = httpClient.get("$restApiBaseUrl/categories?select=*") {
                header("apikey", supabaseKey)
                header("Authorization", "Bearer $supabaseKey")
                header("Content-Type", "application/json")
            }
            
            val responseBody = response.body<String>()
            val categories = json.decodeFromString<List<Category>>(responseBody)
            
            Log.d(TAG, "Successfully fetched ${categories.size} categories via REST API")
            categories
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching categories via REST API", e)
            emptyList()
        }
    }
    
    /**
     * Fetches videos by category ID using direct REST API call
     */
    suspend fun getVideosByCategory(categoryId: String): List<Video> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Fetching videos for category ID: $categoryId via REST API")
            
            val response: HttpResponse = httpClient.get("$restApiBaseUrl/video?category_id=eq.$categoryId&select=*") {
                header("apikey", supabaseKey)
                header("Authorization", "Bearer $supabaseKey")
                header("Content-Type", "application/json")
            }
            
            val responseBody = response.body<String>()
            val videos = json.decodeFromString<List<Video>>(responseBody)
            
            Log.d(TAG, "Successfully fetched ${videos.size} videos for category ID: $categoryId via REST API")
            videos
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching videos for category ID: $categoryId via REST API", e)
            emptyList()
        }
    }
    
    /**
     * Fetches videos with their category information using direct REST API call
     */
    suspend fun getVideosWithCategoryInfo(): List<VideoWithCategory> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Fetching videos with category info via REST API")
            
            // Using the foreign key relationship in the REST API
            val response: HttpResponse = httpClient.get("$restApiBaseUrl/video?select=*,categories(*)") {
                header("apikey", supabaseKey)
                header("Authorization", "Bearer $supabaseKey")
                header("Content-Type", "application/json")
            }
            
            val responseBody = response.body<String>()
            val videos = json.decodeFromString<List<VideoWithCategory>>(responseBody)
            
            Log.d(TAG, "Successfully fetched ${videos.size} videos with category info via REST API")
            videos
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching videos with category info via REST API", e)
            emptyList()
        }
    }
    
    /**
     * Gets paginated videos using direct REST API call
     */
    suspend fun getPaginatedVideos(pageSize: Int = 20, pageNumber: Int = 0): List<Video> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Fetching paginated videos: page $pageNumber, size $pageSize via REST API")
            
            val offset = pageNumber * pageSize
            
            val response: HttpResponse = httpClient.get("$restApiBaseUrl/video?select=*&limit=$pageSize&offset=$offset&order=title.asc") {
                header("apikey", supabaseKey)
                header("Authorization", "Bearer $supabaseKey")
                header("Content-Type", "application/json")
            }
            
            val responseBody = response.body<String>()
            val videos = json.decodeFromString<List<Video>>(responseBody)
            
            Log.d(TAG, "Successfully fetched ${videos.size} videos for page $pageNumber via REST API")
            videos
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching paginated videos via REST API", e)
            emptyList()
        }
    }
} 