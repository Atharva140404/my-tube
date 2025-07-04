package com.example.aman.player

import android.app.Activity
import android.view.View
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.unit.dp
import com.example.aman.data.Video

/**
 * A lightweight YouTube player for use in smaller UI components
 */
@Composable
fun LiteVideoPlayer(
    videoId: String,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isFullscreen by remember { mutableStateOf(false) }
    
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Use our YouTube player with minimal UI
        YouTubePlayer(
            videoId = videoId,
            autoPlay = true,
            showControls = true,
            enableFullscreen = false,
            modifier = Modifier.fillMaxSize()
        )
        
        // Close button
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

/**
 * Convenience function to play a video using the lite player
 */
@Composable
fun LiteVideoPlayerForVideo(
    video: Video,
    onClose: () -> Unit
) {
    val videoId = com.example.aman.extractYouTubeVideoId(video.url)
    
    if (videoId != null) {
        LiteVideoPlayer(
            videoId = videoId,
            onClose = onClose
        )
    }
} 