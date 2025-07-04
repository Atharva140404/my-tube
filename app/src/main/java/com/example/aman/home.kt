package com.example.aman
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource

import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aman.data.Category
import com.example.aman.data.CategoryRepository
import com.example.aman.data.SupabaseClient
import com.example.aman.data.Video
import com.example.aman.data.VideoRepository
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.ui.input.nestedscroll.nestedScroll
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import android.app.Activity
import android.content.pm.ActivityInfo
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebChromeClient
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.view.WindowCompat
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.activity.compose.BackHandler
import com.example.aman.player.CustomYouTubePlayer
import com.example.aman.player.YouTubePlayer
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.google.accompanist.pager.calculateCurrentOffsetForPage
import com.google.accompanist.pager.rememberPagerState
import com.example.aman.data.SliderItem
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.util.lerp
import kotlinx.coroutines.delay
import kotlin.math.absoluteValue

/**
 * Main home screen composable
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun homescreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    var isRefreshing by remember { mutableStateOf(false) }
    var videos by remember { mutableStateOf<List<Video>>(emptyList()) }
    var categories by remember { mutableStateOf<List<Category>>(emptyList()) }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var selectedVideo by remember { mutableStateOf<Video?>(null) }
    var showVideoPlayer by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) }
    
    // Load videos and categories
    LaunchedEffect(selectedCategory) {
        try {
            isRefreshing = true
            
            // Load categories if not already loaded
            if (categories.isEmpty()) {
                categories = CategoryRepository.getCategories()
                // Automatically select the first category if available
                if (categories.isNotEmpty() && selectedCategory == null) {
                    selectedCategory = categories.first()
                }
            }
            
            // Load videos based on selected category
            videos = selectedCategory?.let { category ->
                category.id?.let { categoryId ->
                    VideoRepository.getVideosByCategory(categoryId)
                }
            } ?: VideoRepository.getAllVideos()
            
        } catch (e: Exception) {
            Log.e("HomeScreen", "Error loading data", e)
            snackbarHostState.showSnackbar("Error loading data: ${e.message}")
        } finally {
            isRefreshing = false
        }
    }
    
    // Video player handler
    val videoPlayerHandler = remember {
        VideoPlayerHandler { video ->
            selectedVideo = video
            showVideoPlayer = true
        }
    }
    
    CompositionLocalProvider(LocalVideoPlayerHandler provides videoPlayerHandler) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Main content
            Scaffold(
                topBar = {
                    Column {
                        TopAppBar(
                            title = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.mx_player_logo),
                                        contentDescription = "MyTube Logo",
                                        modifier = Modifier
                                            .size(32.dp)
                                            .padding(end = 8.dp)
                                            .clip(CircleShape)
                                    )
                                    Row {
                                        Text(
                                            text = "My",
                                            style = MaterialTheme.typography.titleLarge.copy(
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = colorResource(id = R.color.mx_blue)
                                        )
                                        Text(
                                            text = "Tube",
                                            style = MaterialTheme.typography.titleLarge.copy(
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = colorResource(id = R.color.mx_red)
                                        )
                                    }
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = Color.Black
                            )
                        )
                        
                        // Categories section moved here, right below the app name
                        CategorySection(
                            categories = categories,
                            selectedCategory = selectedCategory,
                            onCategorySelected = { category ->
                                selectedCategory = if (selectedCategory == category) null else category
                            }
                        )
                    }
                },

                snackbarHost = { SnackbarHost(snackbarHostState) },
                containerColor = Color.Black
            ) { paddingValues ->
                Box(modifier = Modifier.fillMaxSize()) {
                    // Handle refresh state
                    if (isRefreshing) {
                        LaunchedEffect(true) {
                            try {
                                categories = CategoryRepository.getCategories()
                                videos = selectedCategory?.let { category ->
                                    category.id?.let { categoryId ->
                                        VideoRepository.getVideosByCategory(categoryId)
                                    }
                                } ?: VideoRepository.getAllVideos()
                            } catch (e: Exception) {
                                Log.e("HomeScreen", "Error refreshing data", e)
                                snackbarHostState.showSnackbar("Error refreshing data: ${e.message}")
                            } finally {
                                isRefreshing = false
                            }
                        }
                    }
                    
                    when (selectedTab) {
                        0 -> {
                            // Videos tab
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(paddingValues)
                                    .background(Color.Black)
                            ) {
                                // Featured slider
                                item {
                                    FeaturedVideosSection(videos)
                                }
                                
                                // Display videos by category in a grid layout
                                if (selectedCategory != null) {
                                    // If a category is selected, show only that category's videos
                                    val categoryVideos = videos.filter { it.categoryId == selectedCategory?.id }
                                    if (categoryVideos.isNotEmpty()) {
                                        item {
                                            CategoryVideosSection(
                                                categoryName = selectedCategory?.name ?: "Videos",
                                                videos = categoryVideos
                                            )
                                        }
                                    }
                                } else {
                                    // If no category is selected, show all categories
                                    categories.forEach { category ->
                                        val categoryVideos = videos.filter { it.categoryId == category.id }
                                        if (categoryVideos.isNotEmpty()) {
                                            item {
                                                CategoryVideosSection(
                                                    categoryName = category.name,
                                                    videos = categoryVideos
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        1 -> {
                            // Search tab (placeholder)
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(paddingValues)
                                    .background(Color.Black),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Search Coming Soon",
                                    color = Color.White,
                                    style = MaterialTheme.typography.headlineMedium
                                )
                            }
                        }
                    }
                    
                    // Simple refresh indicator
                    if (isRefreshing) {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .fillMaxWidth()
                        )
                    }
                }
            }
            
            // Full-screen video player
            if (showVideoPlayer && selectedVideo != null) {
                FullScreenVideoPlayer(
                    video = selectedVideo!!,
                    onDismiss = {
                        showVideoPlayer = false
                    }
                )
            }
        }
    }
}

/**
 * Featured videos section with ViewPager slider
 */
@OptIn(ExperimentalPagerApi::class)
@Composable
fun FeaturedVideosSection(videos: List<Video>) {
    val videoPlayerHandler = LocalVideoPlayerHandler.current
    val featuredVideos = videos.take(5) // Take first 5 videos for featured section
    
    // Convert videos to SliderItems
    val sliderItems = featuredVideos.map { video ->
        SliderItem(
            imageUrl = video.thumbnailUrl ?: "",
            title = video.title,
            description = null, // Video class doesn't have description field
            tags = null, // Video class doesn't have tags field
            videoId = video.id
        )
    }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = "Trending on MyTube",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        
        if (featuredVideos.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No videos available",
                    color = Color.White
                )
            }
        } else {
            JioTvStyleViewPager(
                items = sliderItems,
                onItemClick = { sliderItem ->
                    // Find the corresponding video and play it
                    val video = featuredVideos.find { it.id == sliderItem.videoId }
                    video?.let { videoPlayerHandler.onVideoSelected(it) }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
        }
    }
}

/**
 * JioTV style ViewPager with auto-scroll and zoom animation
 */
@OptIn(ExperimentalPagerApi::class)
@Composable
fun JioTvStyleViewPager(
    items: List<SliderItem>,
    onItemClick: (SliderItem) -> Unit,
    modifier: Modifier = Modifier
) {
    if (items.isEmpty()) return
    
    val pagerState = rememberPagerState()
    
    // Auto-scroll logic
    LaunchedEffect(pagerState) {
        while (true) {
            delay(5000) // Change slide every 5 seconds
            val nextPage = (pagerState.currentPage + 1) % pagerState.pageCount
            pagerState.animateScrollToPage(nextPage)
        }
    }
    
    Column(modifier = modifier) {
        HorizontalPager(
            count = items.size,
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) { page ->
            val pageOffset = calculateCurrentOffsetForPage(page).absoluteValue
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp)
                    .graphicsLayer {
                        // Apply zoom effect
                        val scale = lerp(
                            start = 0.85f,
                            stop = 1f,
                            fraction = 1f - pageOffset.coerceIn(0f, 1f)
                        )
                        scaleX = scale
                        scaleY = scale
                        
                        // Apply alpha effect
                        alpha = lerp(
                            start = 0.5f,
                            stop = 1f,
                            fraction = 1f - pageOffset.coerceIn(0f, 1f)
                        )
                    }
                    .clickable { onItemClick(items[page]) }
            ) {
                SliderItemContent(item = items[page])
            }
        }
        
        HorizontalPagerIndicator(
            pagerState = pagerState,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(8.dp),
            activeColor = colorResource(id = R.color.mx_red),
            inactiveColor = Color.LightGray
        )
    }
}

/**
 * Content for a single slider item
 */
@Composable
fun SliderItemContent(item: SliderItem) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Thumbnail
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(item.imageUrl)
                .crossfade(true)
                .build(),
            contentDescription = item.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(8.dp))
        )
        
        // Gradient overlay for better text visibility
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.7f)
                        ),
                        startY = 0f,
                        endY = 500f
                    )
                )
        )
        
        // Hot tag
        Box(
            modifier = Modifier
                .padding(8.dp)
                .align(Alignment.TopStart)
                .background(colorResource(id = R.color.mx_red), RoundedCornerShape(4.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = "HOT",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        // Title and tags
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        ) {
            Text(
                text = item.title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            item.description?.let {
                Text(
                    text = it,
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * Featured video card for the slider
 */
@Composable
fun FeaturedVideoCard(
    video: Video,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(300.dp)
            .height(180.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Thumbnail
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(video.thumbnailUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "Video thumbnail",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            
            // Gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color(0x99000000)
                            ),
                            startY = 0f,
                            endY = 300f
                        )
                    )
            )
            
            // Play button
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(48.dp)
                    .background(Color(0x99000000), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
            
            // "HOT" label for trending videos
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
                    .background(colorResource(id = R.color.hot_label), RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "HOT",
                    color = Color.White,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
            }
            
            // Video title
            Text(
                text = video.title,
                color = Color.White,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
            )
        }
    }
}

/**
 * Category videos section with grid layout
 */
@Composable
fun CategoryVideosSection(
    categoryName: String,
    videos: List<Video>
) {
    val videoPlayerHandler = LocalVideoPlayerHandler.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$categoryName Videos",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

        }

        // Calculate the number of rows needed (2 videos per row)
        val rows = (videos.size + 1) / 2
        val gridHeight = (rows * 210).dp  // Each video card is 200dp + 10dp spacing

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(gridHeight)
                .padding(bottom = 16.dp)
        ) {
            items(videos) { video ->
                VideoCard(
                    video = video,
                    onClick = { videoPlayerHandler.onVideoSelected(video) }
                )
            }
        }
    }
}

/**
 * Full screen video player composable
 */
@Composable
fun FullScreenVideoPlayer(
    video: Video,
    onDismiss: () -> Unit
) {
    val activity = LocalContext.current as? Activity

    // Handle back button press
    BackHandler {
        activity?.let {
            // Ensure we set orientation back to portrait first
            it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

            // Restore window insets
            WindowCompat.setDecorFitsSystemWindows(it.window, true)

            // Restore system UI visibility
            val decorView = it.window.decorView
            decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
        }

        // Then call the close callback
        onDismiss()
    }

    // Check if it's a YouTube video
    val videoId = extractYouTubeVideoId(video.url)

    // Apply immersive fullscreen mode
    DisposableEffect(Unit) {
        var originalVisibility = View.SYSTEM_UI_FLAG_VISIBLE
        
        activity?.let {
            // Store original orientation
            val originalOrientation = it.requestedOrientation

            // Force landscape orientation
            it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

            // Set window to edge-to-edge
            WindowCompat.setDecorFitsSystemWindows(it.window, false)

            // Hide system UI for immersive experience
            val decorView = it.window.decorView
            originalVisibility = decorView.systemUiVisibility

            decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN)
        }

        onDispose {
            activity?.let {
                // Explicitly set back to portrait first
                it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

                // Small delay to ensure orientation change completes
                try {
                    Thread.sleep(100)
                } catch (e: Exception) {
                    Log.e("FullScreenVideoPlayer", "Error during orientation change delay", e)
                }

                // Then restore window insets
                WindowCompat.setDecorFitsSystemWindows(it.window, true)

                // Restore system UI visibility
                it.window.decorView.systemUiVisibility = originalVisibility
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (videoId != null) {
            // For YouTube videos, use our custom player
            CustomYouTubePlayer(videoId = videoId)
        } else {
            // For non-YouTube videos, use external app
            val context = LocalContext.current

            // Create an intent to view the video
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(video.url)
                // Add flags to start as a new task
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }

            // Launch the intent
            LaunchedEffect(Unit) {
                try {
                    context.startActivity(intent)
                    // Close our player since we're using an external app
                    onDismiss()
                } catch (e: Exception) {
                    // Show a toast if no app can handle the intent
                    Toast.makeText(
                        context,
                        "No app found to play this video",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e("VideoPlayer", "Error opening video: ${e.message}")
                    onDismiss()
                }
            }
        }

        // Close button
        IconButton(
            onClick = {
                activity?.let {
                    // Ensure we set orientation back to portrait first
                    it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

                    // Restore window insets
                    WindowCompat.setDecorFitsSystemWindows(it.window, true)

                    // Restore system UI visibility
                    val decorView = it.window.decorView
                    decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
                }

                // Then call the close callback
                onDismiss()
            },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .size(48.dp)
                .background(Color(0x66000000), CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

/**
 * Video card composable
 */
@Composable
fun VideoCard(
    video: Video,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            // Thumbnail
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            ) {
                // If thumbnail URL is available, load it with Coil
                if (!video.thumbnailUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(video.thumbnailUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Video thumbnail",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // Placeholder for missing thumbnail
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.DarkGray),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }
                
                // Play button overlay
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(36.dp)
                        .background(Color(0x99000000), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                // "RECOMMENDED" label for some videos (every other video)
                if (video.id?.hashCode()?.rem(2) == 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(4.dp)
                            .background(colorResource(id = R.color.recommended_label), RoundedCornerShape(2.dp))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "RECOMMENDED",
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            // Video title
            Text(
                text = video.title,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

// Create a CompositionLocal to pass the video player handler
data class VideoPlayerHandler(
    val onVideoSelected: (Video) -> Unit
)

val LocalVideoPlayerHandler = compositionLocalOf<VideoPlayerHandler> { 
    error("No VideoPlayerHandler provided") 
}

/**
 * Extracts YouTube video ID from various YouTube URL formats
 */
fun extractYouTubeVideoId(url: String): String? {
    val patterns = listOf(
        "(?<=youtu.be/)[a-zA-Z0-9_-]+".toRegex(),
        "(?<=v=)[a-zA-Z0-9_-]+".toRegex(),
        "(?<=embed/)[a-zA-Z0-9_-]+".toRegex(),
        "(?<=/v/)[a-zA-Z0-9_-]+".toRegex()
    )
    
    for (pattern in patterns) {
        val matcher = pattern.find(url)
        if (matcher != null) {
            return matcher.value
        }
    }
    return null
}