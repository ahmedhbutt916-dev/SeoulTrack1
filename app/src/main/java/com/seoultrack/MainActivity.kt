package com.seoultrack

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.seoultrack.ui.screens.MainScreen
import com.seoultrack.ui.theme.SeoulTrackTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Edge-to-edge: draw behind system bars for the full-bleed look
        enableEdgeToEdge()

        // Keep screen on during playback (remove if not needed globally)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setContent {
            SeoulTrackTheme {
                MainScreen()
            }
        }
    }
}
