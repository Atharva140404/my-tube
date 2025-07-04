package com.example.aman

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.aman.data.Category
import com.example.aman.data.SliderItem
import com.example.aman.data.Video
import com.example.aman.data.VideoRepository
import com.example.aman.data.VideosPagingSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the MyTube app implementing hybrid loading strategy
 */
class MyTubeViewModel : ViewModel() {
    private val repository = VideoRepository()
    private val TAG = "MyTubeViewModel"
    
    // Immediate load data
    private val _sliderItems = MutableStateFlow<List<SliderItem>>(emptyList())
    val sliderItems: StateFlow<List<SliderItem>> = _sliderItems
    
    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories
    
    // Paginated videos
    val videosPagingFlow: Flow<PagingData<Video>> = Pager(
        config = PagingConfig(
            pageSize = 20,
            initialLoadSize = 20, // First batch
            prefetchDistance = 5, // Start loading when 5 items away from end
            enablePlaceholders = false
        ),
        pagingSourceFactory = { VideosPagingSource(repository) }
    ).flow.cachedIn(viewModelScope)
    
    // Loading state
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    init {
        loadInitialData()
    }
    
    /**
     * Load initial data that should appear immediately using parallel fetching
     */
    private fun loadInitialData() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                Log.d(TAG, "Loading initial data using parallel fetching")
                
                // Use optimized parallel loading for initial data
                val initialData = repository.getInitialData()
                
                // Update UI state with fetched data
                _sliderItems.value = initialData.slider
                _categories.value = initialData.categories
                
                Log.d(TAG, "Initial data loaded in parallel: ${_sliderItems.value.size} slider items, ${_categories.value.size} categories")
            } catch (e: Exception) {
                Log.e(TAG, "Error loading initial data", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Refresh all data
     */
    fun refresh() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                Log.d(TAG, "Refreshing data")
                
                val (categories, videos) = repository.refreshAllData()
                _categories.value = categories
                
                // Update slider items
                _sliderItems.value = videos.take(5).map { video ->
                    SliderItem(
                        imageUrl = video.thumbnailUrl ?: "",
                        title = video.title,
                        videoId = video.id
                    )
                }
                
                Log.d(TAG, "Data refreshed")
            } catch (e: Exception) {
                Log.e(TAG, "Error refreshing data", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}
