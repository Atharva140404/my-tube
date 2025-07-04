package com.example.aman.player

import android.app.Activity
import android.content.pm.ActivityInfo
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.aman.R
import com.example.aman.data.Video
import com.example.aman.extractYouTubeVideoId
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView

/**
 * Sealed class representing the different states of the YouTube player
 */
sealed class YouTubePlayerState {
    object Loading : YouTubePlayerState()
    object Ready : YouTubePlayerState()
    data class Error(val message: String) : YouTubePlayerState()
}

/**
 * A YouTube player implementation using the official YouTube Android Player SDK
 * from com.pierfrancescosoffritti.androidyoutubeplayer
 */
@Composable
fun YouTubePlayer(
    videoId: String,
    autoPlay: Boolean = true,
    showControls: Boolean = true,
    enableFullscreen: Boolean = true,
    onFullscreenChange: ((Boolean) -> Unit)? = null,
    onReady: ((YouTubePlayer) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var isLoading by remember { mutableStateOf(true) }
    
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // YouTube Player
        AndroidView(
            factory = { ctx ->
                YouTubePlayerView(ctx).apply {
                    // Disable automatic initialization
                    enableAutomaticInitialization = false
                    
                    // Configure player options
                    val iFramePlayerOptions = IFramePlayerOptions.Builder()
                        .controls(if (showControls) 1 else 0)
                        .fullscreen(if (enableFullscreen) 1 else 0)
                        .build()
                    
                    // Initialize the player
                    initialize(object : AbstractYouTubePlayerListener() {
                        override fun onReady(youTubePlayer: YouTubePlayer) {
                            isLoading = false
                            
                            if (autoPlay) {
                                youTubePlayer.loadVideo(videoId, 0f)
                            } else {
                                youTubePlayer.cueVideo(videoId, 0f)
                            }
                            
                            onReady?.invoke(youTubePlayer)
                        }
                    }, iFramePlayerOptions)
                }
            },
            modifier = Modifier.fillMaxSize(),
            update = { youTubePlayerView ->
                // Add as lifecycle observer
                lifecycleOwner.lifecycle.addObserver(youTubePlayerView)
            }
        )
        
        // Loading indicator
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = Color.Red
            )
        }
    }
    
    // Clean up when leaving the composition
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_DESTROY) {
                // No need to manually release as the lifecycle observer handles this
            }
        }
        
        lifecycleOwner.lifecycle.addObserver(observer)
        
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}

/**
 * A YouTube player implementation that supports fullscreen mode
 */
@Composable
fun FullscreenYouTubePlayer(
    videoId: String,
    autoPlay: Boolean = true,
    showControls: Boolean = true,
    isFullscreen: Boolean = false,
    onFullscreenChange: (Boolean) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var isLoading by remember { mutableStateOf(true) }
    
    // Set screen orientation based on fullscreen state
    DisposableEffect(isFullscreen) {
        val activity = context as? Activity
        if (isFullscreen) {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        } else {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        
        onDispose {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }
    
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // YouTube Player
        AndroidView(
            factory = { ctx ->
                YouTubePlayerView(ctx).apply {
                    enableAutomaticInitialization = false
                    
                    val iFramePlayerOptions = IFramePlayerOptions.Builder()
                        .controls(if (showControls) 1 else 0)
                        .fullscreen(1) // Enable fullscreen button
                        .build()
                    
                    initialize(object : AbstractYouTubePlayerListener() {
                        override fun onReady(youTubePlayer: YouTubePlayer) {
                            isLoading = false
                            
                            if (autoPlay) {
                                youTubePlayer.loadVideo(videoId, 0f)
                            } else {
                                youTubePlayer.cueVideo(videoId, 0f)
                            }
                        }
                    }, iFramePlayerOptions)
                }
            },
            modifier = Modifier.fillMaxSize(),
            update = { youTubePlayerView ->
                lifecycleOwner.lifecycle.addObserver(youTubePlayerView)
            }
        )
        
        // Loading indicator
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = Color.Red
            )
        }
        
        // Close button (only visible in fullscreen mode)
        if (isFullscreen) {
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(36.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.White
                )
            }
        }
    }
}

/**
 * Fullscreen YouTube player composable
 */
@Composable
fun FullScreenYouTubePlayer(
    video: Video,
    onClose: () -> Unit,
    showControls: Boolean = true
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val videoId = extractYouTubeVideoId(video.url)
    
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
        onClose()
    }
    
    // Apply immersive fullscreen mode
    DisposableEffect(Unit) {
        activity?.let {
            // Store original orientation
            val originalOrientation = it.requestedOrientation
            
            // Force landscape orientation
            it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            
            // Set window to edge-to-edge
            WindowCompat.setDecorFitsSystemWindows(it.window, false)
            
            // Hide system UI for immersive experience
            val decorView = it.window.decorView
            val originalVisibility = decorView.systemUiVisibility
            
            decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN)
            
            onDispose {
                // Explicitly set back to portrait first
                it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                
                // Small delay to ensure orientation change completes
                try {
                    Thread.sleep(100)
                } catch (e: Exception) {
                    Log.e("FullScreenYouTubePlayer", "Error during orientation change delay", e)
                }
                
                // Then restore window insets
                WindowCompat.setDecorFitsSystemWindows(it.window, true)
                
                // Restore system UI visibility
                decorView.systemUiVisibility = originalVisibility
            }
        }
        
        onDispose {}
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (videoId != null) {
            // Use our compliant YouTube player
            YouTubePlayer(
                videoId = videoId,
                autoPlay = true,
                showControls = showControls
            )
        } else {
            // Handle invalid video ID
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = colorResource(id = R.color.jio_blue)
                )
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
                onClose()
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