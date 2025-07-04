package com.example.aman.player

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import com.example.aman.R
import kotlinx.coroutines.delay

/**
 * A YouTube player that shows a thumbnail first and then loads the player after a delay
 * This helps hide the YouTube logo and provides a better user experience
 *
 * @param videoId The YouTube video ID
 * @param modifier The modifier to apply to this layout
 * @param placeholderResId The resource ID of the placeholder image to show if thumbnail loading fails
 * @param autoPlayDelay The delay in milliseconds before the WebView loads and auto-plays
 */
@Composable
fun YouTubePlayerWithThumbnail(
    videoId: String,
    modifier: Modifier = Modifier,
    placeholderResId: Int = R.drawable.ic_video_placeholder,
    autoPlayDelay: Long = 2000L, // 2 sec delay before WebView loads
) {
    var showWebView by remember { mutableStateOf(false) }
    var thumbnailLoadState by remember { mutableStateOf<AsyncImagePainter.State>(AsyncImagePainter.State.Empty) }
    var isLoading by remember { mutableStateOf(true) }

    // YouTube Thumbnail URL (fallback to lower quality if maxres fails)
    val thumbnailUrls = remember(videoId) {
        listOf(
            "https://img.youtube.com/vi/$videoId/maxresdefault.jpg", // Highest quality
            "https://img.youtube.com/vi/$videoId/hqdefault.jpg",    // Medium quality
            "https://img.youtube.com/vi/$videoId/mqdefault.jpg",    // Low quality
        )
    }

    // Try loading thumbnail first, then WebView after delay
    LaunchedEffect(Unit) {
        delay(autoPlayDelay)
        showWebView = true
    }

    Box(modifier = modifier.fillMaxSize()) {
        // (1) Show Thumbnail (while WebView is loading)
        AnimatedVisibility(
            visible = !showWebView || isLoading,
            exit = fadeOut(),
        ) {
            // Using SubcomposeAsyncImage instead of AsyncImage for better control
            SubcomposeAsyncImage(
                model = thumbnailUrls.first(),
                contentDescription = "YouTube Thumbnail",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                loading = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = colorResource(id = R.color.jio_blue)
                        )
                    }
                },
                error = {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        // Show placeholder on error
                        androidx.compose.foundation.Image(
                            painter = painterResource(id = placeholderResId),
                            contentDescription = "Video placeholder",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }
                },
                success = {
                    SubcomposeAsyncImageContent()
                }
            )
        }

        // (2) Show WebView (after delay)
        AnimatedVisibility(
            visible = showWebView,
            enter = fadeIn(),
        ) {
            YouTubeWebView(
                videoId = videoId,
                modifier = Modifier.fillMaxSize(),
                onLoadingChanged = { loading -> isLoading = loading }
            )
        }
        
        // Show loading indicator while WebView is loading
        if (showWebView && isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = colorResource(id = R.color.jio_blue)
            )
        }
    }
}

/**
 * A simple WebView wrapper for YouTube videos
 */
@Composable
private fun YouTubeWebView(
    videoId: String,
    modifier: Modifier = Modifier,
    onLoadingChanged: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current

    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
                settings.javaScriptEnabled = true
                settings.mediaPlaybackRequiresUserGesture = false
                settings.domStorageEnabled = true
                settings.loadsImagesAutomatically = true
                
                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        onLoadingChanged(false)
                    }
                    
                    // Handle page errors (e.g., no internet)
                    override fun onReceivedError(
                        view: WebView?,
                        errorCode: Int,
                        description: String?,
                        failingUrl: String?
                    ) {
                        super.onReceivedError(view, errorCode, description, failingUrl)
                        onLoadingChanged(false)
                        // You can implement retry logic here if needed
                    }
                }
                
                // Background color to match the app theme
                setBackgroundColor(android.graphics.Color.BLACK)
                
                // Load the YouTube video with parameters
                loadUrl(
                    "https://www.youtube-nocookie.com/embed/$videoId?" +
                            "autoplay=1&" +         // Auto-play video
                            "modestbranding=1&" +   // Reduce YouTube logo
                            "rel=0"                 // Hide related videos
                )
            }
        },
        modifier = modifier.background(Color.Black),
        onRelease = { webView ->
            // Clean up WebView to prevent memory leaks
            webView.destroy()
        }
    )
} 