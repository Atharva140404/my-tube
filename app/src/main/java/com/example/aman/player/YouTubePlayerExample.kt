package com.example.aman.player

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.aman.data.Video
import com.example.aman.extractYouTubeVideoId

/**
 * Example of how to use the YouTubePlayerWithThumbnail component
 */
@Composable
fun VideoPlayerExample(
    video: Video,
    modifier: Modifier = Modifier
) {
    val videoId = extractYouTubeVideoId(video.url)
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Video title
        Text(
            text = video.title,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        // Video player with 16:9 aspect ratio
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (videoId != null) {
                    // Use our thumbnail-first player for YouTube videos
                    YouTubePlayerWithThumbnail(
                        videoId = videoId,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // For non-YouTube videos, you could use a different player
                    // or show a message
                    Text(
                        text = "Non-YouTube video",
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
        
        // Video thumbnail URL if available
        Text(
            text = video.thumbnailUrl ?: "",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

/**
 * Example of how to use the YouTubePlayerWithThumbnail in a video card
 */
@Composable
fun VideoCard(
    video: Video,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val videoId = extractYouTubeVideoId(video.url)
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        onClick = onClick
    ) {
        Column {
            // Thumbnail with 16:9 aspect ratio
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
            ) {
                if (videoId != null) {
                    // Just show the thumbnail without auto-playing
                    YouTubePlayerWithThumbnail(
                        videoId = videoId,
                        // Set a very long delay so it doesn't auto-play in the card
                        autoPlayDelay = 1000000L
                    )
                }
            }
            
            // Video info
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = video.title,
                    style = MaterialTheme.typography.titleMedium
                )
                
                // Show thumbnail URL if available
                if (video.thumbnailUrl != null) {
                    Text(
                        text = video.thumbnailUrl,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
} 