package com.example.aman.data.network

import android.util.Log
import com.example.aman.data.SupabaseClient
import com.example.aman.data.Video
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

/**
 * Utility class to demonstrate how to use cURL-style commands with OkHttp
 * This class provides examples of how to convert cURL commands to OkHttp code
 * 
 * Note: This is for demonstration purposes. The app already uses ktor-client,
 * so the SupabaseRestClient is the preferred way to make REST API calls.
 */
object CurlStyleRequests {
    private val TAG = "CurlStyleRequests"
    
    // Supabase URL and key
    private val supabaseUrl = SupabaseClient.SUPABASE_URL
    private val supabaseKey = SupabaseClient.SUPABASE_ANON_KEY
    
    // Base URL for REST API
    private val restApiBaseUrl = "$supabaseUrl/rest/v1"
    
    // OkHttpClient instance
    private val client = OkHttpClient()
    
    // JSON parser with lenient mode
    private val json = Json { 
        ignoreUnknownKeys = true 
        isLenient = true
    }
    
    /**
     * Equivalent to the following cURL command:
     * 
     * curl -X GET 'https://[PROJECT_REF].supabase.co/rest/v1/video?select=*' \
     * -H "apikey: YOUR_SUPABASE_KEY" \
     * -H "Authorization: Bearer YOUR_SUPABASE_KEY"
     */
    suspend fun getAllVideos(): List<Video> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Fetching all videos with OkHttp (cURL style)")
            
            val request = Request.Builder()
                .url("$restApiBaseUrl/video?select=*")
                .addHeader("apikey", supabaseKey)
                .addHeader("Authorization", "Bearer $supabaseKey")
                .addHeader("Content-Type", "application/json")
                .build()
            
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e(TAG, "Error fetching videos: ${response.code}")
                    return@withContext emptyList<Video>()
                }
                
                val responseBody = response.body?.string() ?: ""
                val videos = json.decodeFromString<List<Video>>(responseBody)
                
                Log.d(TAG, "Successfully fetched ${videos.size} videos with OkHttp")
                videos
            }
        } catch (e: IOException) {
            Log.e(TAG, "Error fetching videos with OkHttp", e)
            emptyList()
        }
    }
    
    /**
     * Equivalent to the following cURL command:
     * 
     * curl -X GET 'https://[PROJECT_REF].supabase.co/rest/v1/video?category_id=eq.CATEGORY_ID' \
     * -H "apikey: YOUR_SUPABASE_KEY" \
     * -H "Authorization: Bearer YOUR_SUPABASE_KEY"
     */
    suspend fun getVideosByCategory(categoryId: String): List<Video> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Fetching videos for category ID: $categoryId with OkHttp (cURL style)")
            
            val request = Request.Builder()
                .url("$restApiBaseUrl/video?category_id=eq.$categoryId&select=*")
                .addHeader("apikey", supabaseKey)
                .addHeader("Authorization", "Bearer $supabaseKey")
                .addHeader("Content-Type", "application/json")
                .build()
            
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e(TAG, "Error fetching videos by category: ${response.code}")
                    return@withContext emptyList<Video>()
                }
                
                val responseBody = response.body?.string() ?: ""
                val videos = json.decodeFromString<List<Video>>(responseBody)
                
                Log.d(TAG, "Successfully fetched ${videos.size} videos for category ID: $categoryId with OkHttp")
                videos
            }
        } catch (e: IOException) {
            Log.e(TAG, "Error fetching videos by category with OkHttp", e)
            emptyList()
        }
    }
    
    /**
     * Equivalent to the following cURL command:
     * 
     * curl -X GET 'https://[PROJECT_REF].supabase.co/rest/v1/video?select=*&order=views.desc&limit=10' \
     * -H "apikey: YOUR_SUPABASE_KEY" \
     * -H "Authorization: Bearer YOUR_SUPABASE_KEY"
     */
    suspend fun getPopularVideos(limit: Int = 10): List<Video> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Fetching popular videos with OkHttp (cURL style)")
            
            val request = Request.Builder()
                .url("$restApiBaseUrl/video?select=*&order=views.desc&limit=$limit")
                .addHeader("apikey", supabaseKey)
                .addHeader("Authorization", "Bearer $supabaseKey")
                .addHeader("Content-Type", "application/json")
                .build()
            
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e(TAG, "Error fetching popular videos: ${response.code}")
                    return@withContext emptyList<Video>()
                }
                
                val responseBody = response.body?.string() ?: ""
                val videos = json.decodeFromString<List<Video>>(responseBody)
                
                Log.d(TAG, "Successfully fetched ${videos.size} popular videos with OkHttp")
                videos
            }
        } catch (e: IOException) {
            Log.e(TAG, "Error fetching popular videos with OkHttp", e)
            emptyList()
        }
    }
} 