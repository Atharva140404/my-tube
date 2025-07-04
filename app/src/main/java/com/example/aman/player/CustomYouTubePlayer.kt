package com.example.aman.player

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.res.colorResource
import com.example.aman.R

/**
 * A custom YouTube player that wraps the compliant YouTube player implementation
 */
@Composable
fun CustomYouTubePlayer(
    videoId: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isFullscreen by remember { mutableStateOf(false) }
    
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (isFullscreen) {
            FullscreenYouTubePlayer(
                videoId = videoId,
                autoPlay = true,
                showControls = true,
                isFullscreen = true,
                onFullscreenChange = { isFullscreen = it },
                onClose = { isFullscreen = false }
            )
        } else {
            YouTubePlayer(
                videoId = videoId,
                autoPlay = true,
                showControls = true,
                enableFullscreen = true,
                onFullscreenChange = { isFullscreen = it }
            )
        }
    }
}