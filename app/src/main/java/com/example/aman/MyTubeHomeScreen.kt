package com.example.aman

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import com.example.aman.data.Category
import com.example.aman.data.SliderItem
import com.example.aman.data.Video
import com.example.aman.ui.shimmerEffect

/**
 * Main home screen for MyTube app with hybrid loading strategy
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTubeHomeScreen(
    viewModel: MyTubeViewModel = viewModel()
) {
    val videos = viewModel.videosPagingFlow.collectAsLazyPagingItems()
    val isRefreshing by viewModel.isLoading.collectAsState()

    // Collect UI state
    val sliderItems by viewModel.sliderItems.collectAsState(initial = emptyList())
    val categories by viewModel.categories.collectAsState(initial = emptyList())

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { viewModel.refresh() }
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            // 1. Slider (Immediate load)
            item(key = "slider") {
                if (sliderItems.isNotEmpty()) {
                    SliderSection(
                        items = sliderItems,
                        modifier = Modifier.height(200.dp)
                    )
                } else {
                    // Placeholder for slider
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }

            // 2. Categories (Immediate load)
            item(key = "categories") {
                if (categories.isNotEmpty()) {
                    CategorySection(
                        categories = categories,
                        selectedCategory = null,
                        onCategorySelected = { /* Handle category selection */ }
                    )
                } else {
                    // Placeholder for categories
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }

            // 3. First Videos Header
            item(key = "header") {
                Text(
                    text = "Trending Videos",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(16.dp)
                )
            }

            // 4. Videos list with pagination
            androidx.paging.compose.items(
                count = videos.itemCount,
                key = { index -> videos[index]?.id ?: index }
            ) { index ->
                val video = videos[index]
                if (video != null) {
                    HomeVideoCard(
                        video = video,
                        onClick = { /* Handle video click */ }
                    )
                } else {
                    VideoCardPlaceholder()
                }
            }

            // 5. Loading indicator for pagination
            when (videos.loadState.append) {
                is LoadState.Loading -> {
                    item { LoadingSpinner() }
                }
                is LoadState.Error -> {
                    item {
                        ErrorItem(
                            message = "Error loading more videos",
                            onRetryClick = { videos.retry() }
                        )
                    }
                }
                else -> { /* No-op */ }
            }

            // Initial load states
            when (videos.loadState.refresh) {
                is LoadState.Error -> {
                    item {
                        ErrorItem(
                            message = "Error loading videos",
                            onRetryClick = { videos.retry() }
                        )
                    }
                }
                is LoadState.Loading -> {
                    // We're already showing placeholders for the first page,
                    // so we don't need to show a loading spinner here
                }
                else -> { /* No-op */ }
            }
        }
    }
}

/**
 * Placeholder for video cards during loading
 */
@Composable
fun VideoCardPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .padding(8.dp)
            .shimmerEffect()
    ) {
        // Empty content since this is just a placeholder
    }
}

/**
 * Loading spinner for pagination
 */
@Composable
fun LoadingSpinner() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

/**
 * Error item with retry button
 */
@Composable
fun ErrorItem(
    message: String,
    onRetryClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error
        )
        Button(
            onClick = onRetryClick,
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text("Retry")
        }
    }
}

/**
 * Slider section for displaying featured content
 */
@Composable
fun SliderSection(
    items: List<SliderItem>,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Featured Content",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
        ) {
            // Display all slider items
            items.forEach { item ->
                Box(
                    modifier = Modifier
                        .width(300.dp)
                        .height(180.dp)
                        .padding(horizontal = 8.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Text(
                        text = item.title ?: "Featured Item",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                    )
                }
            }
        }
    }
}

/**
 * Category section for displaying content categories
 */
@Composable
fun CategorySection(
    categories: List<Category>,
    selectedCategory: Category?,
    onCategorySelected: (Category) -> Unit
) {
    val scrollState = rememberScrollState()
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = "Categories",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
                .padding(horizontal = 8.dp)
        ) {
            categories.forEach { category ->
                val isSelected = selectedCategory?.id == category.id
                
                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .clickable { onCategorySelected(category) }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = category.name,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                               else MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

/**
 * Video card component for displaying video items in home screen
 */
@Composable
internal fun HomeVideoCard(
    video: Video,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Thumbnail
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                // Placeholder for video thumbnail
                Text(
                    text = "Thumbnail",
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            
            // Video info
            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                Text(
                    text = video.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Text(
                    text = video.channelName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Text(
                    text = "${video.viewCount} views • ${video.uploadDate}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
