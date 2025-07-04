package com.example.aman.player

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.Toast
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import com.example.aman.R
import com.example.aman.data.Video
import com.example.aman.extractYouTubeVideoId
import com.example.aman.player.LiteVideoPlayer
import android.webkit.JavascriptInterface
import android.webkit.WebResourceResponse
import java.io.ByteArrayInputStream
import android.webkit.WebResourceRequest
import android.content.Context
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/**
 * A fullscreen video player for YouTube videos
 */
@Composable
fun FullScreenVideoPlayer(
    video: Video,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    var isFullscreen by remember { mutableStateOf(false) }
    
    // Handle back button press
    BackHandler {
        if (isFullscreen) {
            isFullscreen = false
        } else {
            onDismiss()
        }
    }
    
    // Set screen orientation when in fullscreen mode
    DisposableEffect(isFullscreen) {
        if (isFullscreen) {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        } else {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        
        onDispose {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }
    
    // Extract YouTube video ID
    val videoId = extractYouTubeVideoId(video.url)
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            if (videoId != null) {
                // YouTube video
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
                    // Use our new thumbnail-first player
                    YouTubePlayerWithThumbnail(
                        videoId = videoId,
                        modifier = Modifier.fillMaxSize(),
                        autoPlayDelay = 2000L // 2 second delay
                    )
                }
            } else {
                // Non-YouTube video - open in external player
                DisposableEffect(Unit) {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(video.url))
                        context.startActivity(intent)
                        onDismiss()
                    } catch (e: Exception) {
                        Log.e("FullScreenVideoPlayer", "Error opening video: ${e.message}")
                        Toast.makeText(context, "Cannot play this video", Toast.LENGTH_SHORT).show()
                        onDismiss()
                    }
                    onDispose {}
                }
            }
            
            // Close button
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .size(40.dp)
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
 * YouTube player screen using WebView with proper fullscreen handling
 */
@Composable
fun YouTubePlayerScreen(videoId: String) {
    val context = LocalContext.current
    val activity = (context as? Activity)
    var isLoading by remember { mutableStateOf(true) }
    var customView by remember { mutableStateOf<View?>(null) }
    var customViewCallback by remember { mutableStateOf<WebChromeClient.CustomViewCallback?>(null) }
    var isPlaying by remember { mutableStateOf(true) }
    
    // Container for the custom view (fullscreen video)
    val fullscreenContainer = remember {
        FrameLayout(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(android.graphics.Color.BLACK)
            visibility = View.GONE
            fitsSystemWindows = false
        }
    }
    
    // Attach fullscreen container directly to the Activity's window decor view
    DisposableEffect(Unit) {
        activity?.let {
            val decorView = it.window.decorView as ViewGroup
            if (fullscreenContainer.parent == null) {
                decorView.addView(fullscreenContainer)
            }
        }
        
        onDispose {
            activity?.let {
                val decorView = it.window.decorView as ViewGroup
                decorView.removeView(fullscreenContainer)
            }
        }
    }
    
    // Create a WebView to display the YouTube video
    val webView = rememberWebView(context)
    
    // CSS to hide YouTube UI elements
    val css = """
        .ytp-chrome-top, 
        .ytp-chrome-bottom,
        .ytp-watermark,
        .ytp-gradient-top,
        .ytp-gradient-bottom,
        .ytp-show-cards-title,
        .ytp-title,
        .ytp-title-text,
        .ytp-title-link,
        .ytp-title-channel,
        .ytp-title-channel-logo,
        .ytp-ce-element,
        .ytp-ce-video,
        .ytp-ce-playlist,
        .html5-endscreen,
        .ytp-endscreen,
        .ytp-player-content,
        .ytp-pause-overlay,
        .ytp-related-videos-container,
        .ytp-spinner,
        .annotation,
        .video-annotations,
        .ytp-iv-player-content,
        .ytp-cards-teaser,
        .ytp-cards-button,
        .ytp-cards-button-icon,
        .ytp-button,
        .ytp-youtube-button,
        .ytp-overflow-button,
        .ytp-share-button,
        .ytp-watch-later-button,
        .ytp-subtitles-button,
        .ytp-settings-button,
        .ytp-fullscreen-button,
        .ytp-miniplayer-button,
        .ytp-volume-panel,
        .ytp-time-display,
        .ytp-chapter-container,
        .ytp-ad-overlay-container,
        .ytp-ad-text-overlay,
        .ytp-ad-skip-button-container { 
            display: none !important;
            opacity: 0 !important;
            visibility: hidden !important;
            width: 0 !important;
            height: 0 !important;
            position: absolute !important;
            left: -9999px !important;
            top: -9999px !important;
            pointer-events: none !important;
            z-index: -9999 !important;
        }
        
        /* Ensure video takes full size */
        .html5-video-container, video {
            width: 100% !important;
            height: 100% !important;
            left: 0 !important;
            top: 0 !important;
            object-fit: contain !important;
        }
    """
    
    // JavaScript to inject CSS
    val js = """
        var style = document.createElement('style');
        style.type = 'text/css';
        style.innerHTML = `$css`;
        document.head.appendChild(style);
        
        // Set up a mutation observer to catch dynamically added UI elements
        const observer = new MutationObserver(mutations => {
            document.head.appendChild(style.cloneNode(true));
            
            // Target specific elements visible in the screenshot
            const elementsToRemove = [
                '.ytp-title',
                '.ytp-title-text',
                '.ytp-title-link',
                '.ytp-title-channel',
                '.ytp-title-channel-logo',
                '.ytp-chrome-top',
                '.ytp-chrome-bottom',
                '.ytp-gradient-top',
                '.ytp-gradient-bottom',
                '.ytp-watermark'
            ];
            
            elementsToRemove.forEach(selector => {
                const elements = document.querySelectorAll(selector);
                elements.forEach(element => {
                    if (element && element.parentNode) {
                        element.parentNode.removeChild(element);
                    }
                });
            });
        });
        
        // Start observing
        observer.observe(document.documentElement, {
            childList: true,
            subtree: true
        });
    """
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Show regular WebView if not in fullscreen mode
        if (customView == null || customViewCallback == null) {
            AndroidView(
                factory = { webView },
                modifier = Modifier.fillMaxSize()
            )
        }
        
        // Loading indicator
        if (isLoading && customView == null) {
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
        }
    }
    
    // Configure WebView
    DisposableEffect(webView) {
        webView.apply {
            settings.javaScriptEnabled = true
            settings.mediaPlaybackRequiresUserGesture = false
            settings.domStorageEnabled = true
            settings.loadsImagesAutomatically = true
            settings.allowContentAccess = true
            settings.allowFileAccess = true
            settings.databaseEnabled = true
            settings.javaScriptCanOpenWindowsAutomatically = true
            settings.loadWithOverviewMode = true
            settings.useWideViewPort = true
            settings.builtInZoomControls = false
            settings.displayZoomControls = false
            settings.setSupportMultipleWindows(true)
            settings.setSupportZoom(false)
            
            // Force hardware acceleration for better video rendering
            setLayerType(View.LAYER_TYPE_HARDWARE, null)
            
            // Add JavaScript interface for communication with native code
            addJavascriptInterface(object : Any() {
                @JavascriptInterface
                fun onPlayerReady() {
                    activity?.runOnUiThread {
                        isLoading = false
                    }
                }
                
                @JavascriptInterface
                fun onPlayerStateChange(state: Int) {
                    // YouTube player states: -1 (unstarted), 0 (ended), 1 (playing), 2 (paused), 3 (buffering), 5 (cued)
                    activity?.runOnUiThread {
                        isPlaying = state == 1
                    }
                }
            }, "AndroidInterface")
            
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    isLoading = false
                    
                    // Inject CSS when page is loaded
                    view?.evaluateJavascript(js, null)
                }
                
                // Block YouTube branding resources
                override fun shouldInterceptRequest(view: WebView?, url: String?): WebResourceResponse? {
                    url?.let {
                        // Block YouTube logo and branding resources
                        if (it.contains("youtube.com/img") || 
                            it.contains("ytimg.com") || 
                            it.contains("logo") || 
                            it.contains("branding") || 
                            it.contains("watermark") ||
                            it.contains("avatar") ||
                            it.contains("profile") ||
                            it.contains("channel") ||
                            it.contains("thumbnail")) {
                            
                            // Return empty response to block the resource
                            return WebResourceResponse(
                                "text/plain",
                                "UTF-8",
                                ByteArrayInputStream("".toByteArray())
                            )
                        }
                    }
                    return super.shouldInterceptRequest(view, url)
                }
                
                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                    return false
                }
            }
            
            // Add WebChromeClient to handle fullscreen requests
            webChromeClient = object : WebChromeClient() {
                override fun onShowCustomView(view: View, callback: CustomViewCallback) {
                    Log.d("YouTubePlayer", "onShowCustomView called")
                    
                    // Save the view and callback
                    customView = view
                    customViewCallback = callback
                    
                    // Configure the view for fullscreen
                    view.layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    
                    // Clear any existing views
                    fullscreenContainer.removeAllViews()
                    
                    // Add the view to our container
                    fullscreenContainer.addView(view, 
                        ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    )
                    
                    // Make sure container is visible and in front
                    fullscreenContainer.visibility = View.VISIBLE
                    fullscreenContainer.bringToFront()
                    
                    // Hide system UI for truly immersive experience
                    activity?.let {
                        val decorView = it.window.decorView
                        decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                or View.SYSTEM_UI_FLAG_FULLSCREEN)
                    }
                }
                
                override fun onHideCustomView() {
                    Log.d("YouTubePlayer", "onHideCustomView called")
                    
                    // Hide the container
                    fullscreenContainer.visibility = View.GONE
                    
                    // Remove the view from our container
                    fullscreenContainer.removeAllViews()
                    
                    // Notify the callback
                    customViewCallback?.onCustomViewHidden()
                    
                    // Reset our state
                    customView = null
                    customViewCallback = null
                    
                    // Restore system UI
                    activity?.let {
                        val decorView = it.window.decorView
                        decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
                    }
                }
            }
            
            // Load the YouTube video using the nocookie domain
            val modifiedUrl = "https://www.youtube-nocookie.com/embed/$videoId?autoplay=1&modestbranding=1&rel=0&controls=0&showinfo=0&fs=0&disablekb=1&iv_load_policy=3&cc_load_policy=0"
            loadUrl(modifiedUrl)
        }
        
        onDispose {
            // Make sure to call the callback before cleaning up
            customViewCallback?.onCustomViewHidden()
            
            // Clean up views
            fullscreenContainer.removeAllViews()
            fullscreenContainer.visibility = View.GONE
            
            // Reset state
            customView = null
            customViewCallback = null
            
            // Ensure system UI is restored
            activity?.let {
                val decorView = it.window.decorView
                decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
            }
            
            // Destroy WebView to prevent memory leaks
            webView.destroy()
        }
    }
}

@Composable
fun rememberWebView(context: Context): WebView {
    return remember {
        WebView(context).apply {
            setBackgroundColor(android.graphics.Color.BLACK)
        }
    }
} 