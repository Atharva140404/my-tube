package com.example.aman.data

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import kotlinx.coroutines.delay

/**
 * PagingSource implementation for loading videos in pages
 */
class VideosPagingSource(
    private val repository: VideoRepository
) : PagingSource<Int, Video>() {
    
    private val TAG = "VideosPagingSource"
    
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Video> {
        val page = params.key ?: 0
        
        return try {
            Log.d(TAG, "Loading page $page with size ${params.loadSize}")
            
            // Simulate instant load for first page
            if (page == 0) {
                delay(100) // Small delay to ensure UI paints first frame
            }
            
            val videos = repository.getPaginatedVideos(
                pageSize = params.loadSize,
                pageNumber = page
            )
            
            Log.d(TAG, "Loaded ${videos.size} videos for page $page")
            
            LoadResult.Page(
                data = videos,
                prevKey = if (page == 0) null else page - 1,
                nextKey = if (videos.isEmpty()) null else page + 1
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error loading page $page", e)
            LoadResult.Error(e)
        }
    }
    
    override fun getRefreshKey(state: PagingState<Int, Video>): Int? {
        // Try to find the page that contains the closest item to the last accessed position
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}
