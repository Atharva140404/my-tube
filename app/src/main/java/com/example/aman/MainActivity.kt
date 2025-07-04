package com.example.aman

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.aman.data.SupabaseClient
import com.example.aman.ui.theme.AmanTheme
import androidx.core.view.WindowCompat
import android.view.WindowManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enable edge-to-edge content
        enableEdgeToEdge()
        
        // Configure window to draw edge-to-edge
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        // Keep screen on during video playback
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        
        // Initialize Supabase client
        val supabaseClient = SupabaseClient.client
        
        setContent {
            AmanTheme {
                // Use the new MyTubeHomeScreen with pagination support
                MyTubeHomeScreen()
            }
        }
    }
}

