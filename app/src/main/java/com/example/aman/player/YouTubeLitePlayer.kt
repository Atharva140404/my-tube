package com.example.aman.player

import android.webkit.WebView
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceResponse
import android.webkit.WebViewClient
import android.widget.FrameLayout
import java.io.ByteArrayInputStream

/**
 * A lightweight YouTube player with minimal UI and controls
 * Provides a simple play/pause button overlay on the YouTube iframe
 */
@Composable
fun YouTubeLitePlayer(videoId: String) {
    val context = LocalContext.current
    val activity = context as? android.app.Activity
    var webView: WebView? = null

    // Handle fullscreen
    var customView by remember { mutableStateOf<View?>(null) }
    var customViewCallback by remember { mutableStateOf<WebChromeClient.CustomViewCallback?>(null) }
    
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

    val htmlData = """
        <!DOCTYPE html><html>
        <head>
            <meta name="viewport" content="width=device-width,initial-scale=1"/>
            <style>
                html,body{margin:0;height:100%;background:#000;display:flex;justify-content:center;align-items:center}
                #wrapper{position:relative;width:100%;max-width:640px;}
                iframe{width:100%;aspect-ratio:16/9;border:0}
                /* Our single play/pause button */
                #pp{
                    position:absolute;inset:0;margin:auto;
                    width:64px;height:64px;border:none;background:transparent;
                    font-size:54px;line-height:54px;color:#fff;cursor:pointer;
                    z-index:9999;
                }
                #pp.paused::before{content:"►";}     /* play symbol  */
                #pp.playing::before{content:"❚❚";}  /* pause symbol */
                
                /* Aggressive YouTube elements hiding */
                iframe {
                    position: relative;
                }
                
                /* Create a style that will be injected into the iframe */
                iframe::after {
                    content: "";
                    position: absolute;
                    top: 0;
                    left: 0;
                    width: 100%;
                    height: 100%;
                    z-index: 1;
                }
            </style>
        </head>
        <body>
          <div id="wrapper">
            <iframe id="yt"
              src="https://www.youtube-nocookie.com/embed/${videoId}?enablejsapi=1&controls=0&rel=0&modestbranding=1&showinfo=0&fs=0&iv_load_policy=3&disablekb=1&loop=0&color=white&theme=dark&origin=${context.packageName}&playsinline=1&mute=0&autoplay=0"
              allow="autoplay; encrypted-media" allowfullscreen>
            </iframe>
            <button id="pp" class="paused"></button>
          </div>

          <script src="https://www.youtube.com/iframe_api"></script>
          <script>
            let player, btn;

            /* called automatically by the API script */
            function onYouTubeIframeAPIReady(){
              player = new YT.Player('yt',{
                 events:{ onReady:onReady, onStateChange:onState }
              });
            }
            
            function injectCSS() {
              try {
                const iframe = document.getElementById('yt');
                if (iframe && iframe.contentWindow) {
                  const iframeDoc = iframe.contentWindow.document;
                  if (iframeDoc) {
                    const style = document.createElement('style');
                    style.textContent = `
                      /* Hide all YouTube elements */
                      .ytp-chrome-top, .ytp-chrome-bottom, .ytp-watermark, 
                      .ytp-pause-overlay, .ytp-youtube-button, .ytp-embed,
                      .ytp-title, .ytp-share-button, .ytp-watch-later-button,
                      .ytp-chapter-container, .ytp-gradient-top, .ytp-gradient-bottom,
                      .ytp-caption-window-container, .ytp-iv-player-content,
                      .ytp-ce-element, .ytp-ce-covering-overlay, .ytp-ce-element-shadow,
                      .ytp-ce-covering-image, .ytp-ce-expanding-image, .ytp-ce-element.ytp-ce-channel,
                      .ytp-ce-element.ytp-ce-video, .ytp-ce-element.ytp-ce-playlist,
                      .ytp-youtube-logo, .ytp-logo, .yt-uix-sessionlink, .ytp-impression-link,
                      .ytp-chrome-top-buttons, .ytp-button, .ytp-cards-button,
                      .ytp-paid-content-overlay, .ytp-spinner, .ytp-bezel,
                      .ytp-overflow-button, .ytp-settings-button, .ytp-fullscreen-button,
                      .ytp-miniplayer-button, .ytp-remote-button, .ytp-subtitles-button,
                      .ytp-mute-button, .ytp-volume-panel, .ytp-time-display,
                      .ytp-player-content, .ytp-endscreen-content, .ytp-endscreen-previous,
                      .ytp-endscreen-next, .ytp-progress-bar-container, .ytp-scrubber-container,
                      .ytp-scrubber-button, .ytp-autonav-endscreen-countdown-container,
                      .ytp-playlist-menu-button, .ytp-playlist-container,
                      .ytp-related-videos-container, .ytp-pause-overlay-container,
                      .ytp-contextmenu, .ytp-popup, .ytp-tooltip,
                      .ytp-ad-overlay-container, .ytp-ad-text-overlay,
                      .ytp-ad-image-overlay, .ytp-ad-progress,
                      .ytp-ad-progress-list, .ytp-ad-overlay-close-button,
                      .ytp-ad-overlay-close-container, .ytp-ad-text,
                      .ytp-ad-skip-button, .ytp-ad-skip-button-container,
                      .ytp-ad-skip-button-text, .ytp-ad-skip-button-icon,
                      .ytp-ad-skip-button-modern, .ytp-cued-thumbnail-overlay,
                      .ytp-spinner-container, .ytp-player-content,
                      .html5-video-player .ytp-chrome-bottom,
                      [class*="youtube"], [class*="yt-"], [id*="youtube"], [id*="yt-"],
                      .annotation, .video-annotations, .ytp-scroll-min, .ytp-pause-overlay,
                      .ytp-suggested-action, .ytp-autohide .ytp-chrome-bottom,
                      .ytp-show-cards-title, .ytp-related-on-show-info,
                      .ytp-show-tiles, .ytp-videowall-still, .ytp-videowall-still-info,
                      .ytp-videowall-still-image, .ytp-videowall-still-info-content,
                      .ytp-videowall-still-info-title, .ytp-videowall-still-info-author,
                      .ytp-videowall-still-info-duration, .ytp-videowall-still-info-view-count,
                      .ytp-videowall-still-listlabel, .ytp-videowall-still-listlabel-icon,
                      .ytp-videowall-still-listlabel-text, .ytp-videowall-still-listlabel-count,
                      .ytp-videowall-still-listlabel-mix, .ytp-videowall-still-listlabel-mix-icon,
                      .ytp-videowall-still-listlabel-mix-text, .ytp-videowall-still-listlabel-mix-count,
                      .ytp-ce-video-wall, .ytp-ce-element-show, .ytp-ce-element-shadow-show,
                      .ytp-ce-channel-this, .ytp-ce-channel-container, .ytp-ce-channel-metadata,
                      .ytp-ce-channel-title, .ytp-ce-channel-description, .ytp-ce-channel-subscribe,
                      .ytp-ce-channel-subscribe-button, .ytp-ce-channel-subscribe-button-text,
                      .ytp-ce-channel-subscribe-button-icon, .ytp-ce-channel-subscribe-button-count,
                      .ytp-ce-channel-subscribe-button-count-text, .ytp-ce-channel-subscribe-button-count-icon,
                      .ytp-endscreen-content, .ytp-endscreen-previous, .ytp-endscreen-next,
                      .ytp-upnext, .ytp-upnext-autoplay-icon, .ytp-upnext-autoplay-paused,
                      .ytp-upnext-container, .ytp-upnext-cancel-button, .ytp-upnext-cancel-button-countdown,
                      .ytp-upnext-description, .ytp-upnext-heading, .ytp-upnext-thumbnail,
                      .ytp-upnext-thumbnail-container, .ytp-upnext-title, .ytp-upnext-toggle-button,
                      .ytp-upnext-toggle-button-icon, .ytp-upnext-toggle-button-text,
                      .ytp-ce-expanding-overlay, .ytp-ce-expanding-overlay-background,
                      .ytp-ce-expanding-overlay-content, .ytp-ce-expanding-overlay-content-container {
                        display: none !important;
                        opacity: 0 !important;
                        pointer-events: none !important;
                        visibility: hidden !important;
                        width: 0 !important;
                        height: 0 !important;
                        position: absolute !important;
                        left: -9999px !important;
                      }
                      
                      /* Hide end screen specifically */
                      .html5-endscreen {
                        display: none !important;
                        opacity: 0 !important;
                        visibility: hidden !important;
                        pointer-events: none !important;
                      }
                      
                      /* Make sure the video player itself is still visible */
                      .html5-video-container, video {
                        width: 100% !important;
                        height: 100% !important;
                        left: 0 !important;
                        top: 0 !important;
                        position: absolute !important;
                        z-index: 1 !important;
                      }
                    `;
                    iframeDoc.head.appendChild(style);
                  }
                }
              } catch(e) {
                console.error("Error injecting CSS:", e);
                if (window.AndroidInterface) {
                  window.AndroidInterface.onError("Error injecting CSS: " + e.message);
                }
              }
            }

            function onReady(){
              btn = document.getElementById('pp');
              btn.onclick = toggle;
              
              // Try to hide YouTube elements
              injectCSS();
              
              // Set an interval to keep hiding elements
              setInterval(injectCSS, 500);
              
              // Notify Android
              if (window.AndroidInterface) {
                window.AndroidInterface.onPlayerReady();
              }
              
              // Try to mute the player to avoid autoplay restrictions
              if (player && player.mute) {
                player.mute();
                // Then unmute after a short delay
                setTimeout(function() {
                  if (player && player.unMute) {
                    player.unMute();
                  }
                }, 1000);
              }
              
              // Handle video end events
              player.addEventListener('onStateChange', function(event) {
                // YT.PlayerState.ENDED = 0
                if (event.data === 0) {
                  // Hide elements when video ends
                  injectCSS();
                  
                  // Prevent end screen by restarting the video
                  setTimeout(function() {
                    // Seek to beginning
                    player.seekTo(0);
                    // Pause the video
                    player.pauseVideo();
                    // Show play button
                    btn.classList.remove('playing');
                    btn.classList.add('paused');
                  }, 100);
                }
              });
            }

            function toggle(){
              const state = player.getPlayerState();
              if(state === YT.PlayerState.PLAYING){ 
                player.pauseVideo();
                if (window.AndroidInterface) {
                  window.AndroidInterface.onVideoStateChanged(false);
                }
              } else { 
                player.playVideo();
                if (window.AndroidInterface) {
                  window.AndroidInterface.onVideoStateChanged(true);
                }
              }
              
              // Hide elements after toggle
              setTimeout(injectCSS, 100);
            }

            function onState(e){
              if(e.data === YT.PlayerState.PLAYING){
                  btn.classList.remove('paused'); btn.classList.add('playing');
                  injectCSS(); // Try to hide elements again when state changes
                  
                  if (window.AndroidInterface) {
                    window.AndroidInterface.onVideoStateChanged(true);
                  }
              }else{
                  btn.classList.remove('playing'); btn.classList.add('paused');
                  
                  if (window.AndroidInterface) {
                    window.AndroidInterface.onVideoStateChanged(false);
                  }
              }
            }
            
            // Try to hide elements more aggressively
            document.addEventListener('DOMContentLoaded', injectCSS);
            window.addEventListener('load', injectCSS);
            window.addEventListener('resize', injectCSS);
            
            // Detect user interaction to hide elements again
            document.addEventListener('click', function() {
              injectCSS();
              if (window.AndroidInterface) {
                window.AndroidInterface.onUserInteraction();
              }
            });
            
            document.addEventListener('touchstart', function() {
              injectCSS();
              if (window.AndroidInterface) {
                window.AndroidInterface.onUserInteraction();
              }
            });
            
            // Also try to hide elements periodically
            setTimeout(function() {
              setInterval(injectCSS, 2000);
            }, 5000);
          </script>
        </body>
        </html>
    """.trimIndent()

    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
                webView = this
                
                settings.javaScriptEnabled = true
                settings.mediaPlaybackRequiresUserGesture = false
                settings.domStorageEnabled = true
                settings.allowContentAccess = true
                settings.allowFileAccess = true
                settings.databaseEnabled = true
                settings.javaScriptCanOpenWindowsAutomatically = true
                settings.mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                settings.loadWithOverviewMode = true
                settings.useWideViewPort = true
                settings.setSupportMultipleWindows(true)
                
                // Set layer type for better performance
                setLayerType(View.LAYER_TYPE_HARDWARE, null)
                
                // Add JavaScript interface for communication
                addJavascriptInterface(object : Any() {
                    @JavascriptInterface
                    fun onPlayerReady() {
                        // Player is ready
                        android.util.Log.d("YouTubeLitePlayer", "Player ready")
                        
                        // Run on UI thread
                        (context as? android.app.Activity)?.runOnUiThread {
                            // Try to evaluate JavaScript to hide elements
                            evaluateJavascript("injectCSS();", null)
                        }
                    }
                    
                    @JavascriptInterface
                    fun onError(message: String) {
                        android.util.Log.e("YouTubeLitePlayer", "Error: $message")
                    }
                    
                    @JavascriptInterface
                    fun onVideoStateChanged(playing: Boolean) {
                        android.util.Log.d("YouTubeLitePlayer", "Video state changed: $playing")
                        
                        // Run on UI thread
                        (context as? android.app.Activity)?.runOnUiThread {
                            // Try to evaluate JavaScript to hide elements
                            evaluateJavascript("injectCSS();", null)
                        }
                    }
                    
                    @JavascriptInterface
                    fun onUserInteraction() {
                        android.util.Log.d("YouTubeLitePlayer", "User interaction detected")
                        
                        // Run on UI thread
                        (context as? android.app.Activity)?.runOnUiThread {
                            // Try to evaluate JavaScript to hide elements
                            evaluateJavascript("injectCSS();", null)
                        }
                    }
                }, "AndroidInterface")
                
                // Set WebViewClient to intercept and block YouTube UI elements
                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        
                        // Inject CSS immediately when page is loaded
                        view?.evaluateJavascript("injectCSS();", null)
                        
                        // And then try again after a short delay to ensure it's applied
                        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                            view?.evaluateJavascript("injectCSS();", null)
                        }, 500)
                        
                        // And again after a longer delay
                        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                            view?.evaluateJavascript("injectCSS();", null)
                        }, 1500)
                    }
                    
                    // Block YouTube assets that might contain branding
                    override fun shouldInterceptRequest(view: WebView, url: String): WebResourceResponse? {
                        // Block YouTube branding resources and UI elements
                        if (url.contains("youtube.com/yts/img") || 
                            url.contains("youtube.com/img") || 
                            url.contains("youtube.com/s/player") ||
                            url.contains("youtube.com/s/desktop") ||
                            url.contains("youtube.com/about") ||
                            url.contains("youtube.com/t/terms") ||
                            url.contains("youtube.com/branding") ||
                            url.contains("youtube.com/howyoutubeworks") ||
                            url.contains("youtube.com/logo") ||
                            url.contains("youtube.com/yt/about") ||
                            url.contains("youtube.com/yt/brand") ||
                            url.contains("youtube.com/yt/copyright") ||
                            url.contains("youtube.com/yt/press") ||
                            url.contains("youtube-nocookie.com/yts/img") ||
                            url.contains("youtube-nocookie.com/img") ||
                            url.contains("youtube-nocookie.com/s/player") ||
                            url.contains("youtube-nocookie.com/s/desktop") ||
                            url.contains("i.ytimg.com")) {
                            
                            return WebResourceResponse(
                                "text/plain",
                                "UTF-8",
                                ByteArrayInputStream("".toByteArray())
                            )
                        }
                        return super.shouldInterceptRequest(view, url)
                    }
                    
                    override fun onLoadResource(view: WebView?, url: String?) {
                        super.onLoadResource(view, url)
                        // Try to hide elements when resources load
                        view?.evaluateJavascript("injectCSS();", null)
                    }
                }
                
                // Add WebChromeClient to handle fullscreen requests
                webChromeClient = object : WebChromeClient() {
                    override fun onShowCustomView(view: View, callback: CustomViewCallback) {
                        android.util.Log.d("YouTubeLitePlayer", "onShowCustomView called")
                        
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
                        android.util.Log.d("YouTubeLitePlayer", "onHideCustomView called")
                        
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
                
                loadDataWithBaseURL("https://www.youtube.com", htmlData, "text/html", "UTF-8", null)
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
    
    // Clean up when the composable is disposed
    DisposableEffect(Unit) {
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
        }
    }
}

/**
 * A version of the YouTubeLitePlayer that can be embedded in a card or list item
 * with a fixed aspect ratio
 */
@Composable
fun EmbeddedYouTubeLitePlayer(
    videoId: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(16f/9f) // 16:9 aspect ratio
            .padding(4.dp)
    ) {
        YouTubeLitePlayer(videoId = videoId)
    }
}

/**
 * Example of how to use the YouTubeLitePlayer in different contexts:
 * 
 * 1. In a full screen player:
 *    LiteVideoPlayer(videoId = "dQw4w9WgXcQ", onClose = { /* handle close */ })
 *
 * 2. In a dialog:
 *    VideoPlayerDialog(video = myVideo, onDismiss = { /* handle dismiss */ }, useLitePlayer = true)
 *
 * 3. Embedded in a list or card:
 *    EmbeddedYouTubeLitePlayer(videoId = "dQw4w9WgXcQ")
 *
 * 4. In the FullScreenVideoPlayer:
 *    FullScreenVideoPlayer(video = myVideo, onClose = { /* handle close */ }, useLitePlayer = true)
 */ 