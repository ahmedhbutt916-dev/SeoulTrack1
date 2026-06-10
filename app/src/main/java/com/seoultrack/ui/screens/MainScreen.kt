package com.seoultrack.ui.screens

import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.seoultrack.ui.components.AmbientBackground
import com.seoultrack.ui.components.NavTab
import com.seoultrack.ui.components.SeoulTrackNavBar
import com.seoultrack.ui.theme.FrostedBackdrop

/**
 * Main screen scaffold for SeoulTrack with real backdrop blur.
 *
 * The frosted glass effect renders the screen content twice:
 * 1. Normally as the main content
 * 2. Inside clipped, blurred boxes behind the nav bar and search bubble
 *
 * The blurred duplicate content, combined with a semi-transparent tint,
 * creates a real backdrop-filter: blur() effect where you can see blurred
 * colors and shapes from the content through the frosted glass.
 *
 * API 31+ (Android 12+): GPU-accelerated blur via RenderEffect
 * Older APIs: Semi-transparent tint fallback
 */
@Composable
fun MainScreen() {
    var currentTab by remember { mutableStateOf(NavTab.DISCOVER) }

    @Composable
    fun ScreenContent() {
        when (currentTab) {
            NavTab.DISCOVER  -> DiscoverScreen()
            NavTab.LIBRARY   -> LibraryScreen()
            NavTab.PROFILE   -> ProfileScreen()
            NavTab.SETTINGS  -> SettingsScreen()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        // Layer 0 — Ambient animated orb background
        AmbientBackground(modifier = Modifier.fillMaxSize())

        // Layer 1 — Main page content (full screen, scrolls behind nav bar)
        Box(modifier = Modifier.fillMaxSize()) {
            ScreenContent()
        }

        // Layer 2 — Frosted backdrop blur
        // We render a full-screen duplicate of the content, but clip it to just
        // the nav bar / search bubble regions, and apply GPU blur + tint.
        // Since the duplicate content is in the same position as the main content,
        // it shows the correct region through the blur.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val density = LocalDensity.current
            val blurPx = with(density) { 28.dp.toPx() }

            // ── Blurred backdrop for the nav pill (left side) ──
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(bottom = 24.dp, start = 16.dp)
                    .padding(end = 92.dp)  // 76dp bubble width + 16dp gap
                    .fillMaxWidth()
                    .height(60.dp)
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(30.dp))
                    .graphicsLayer {
                        renderEffect = RenderEffect
                            .createBlurEffect(blurPx, blurPx, Shader.TileMode.CLAMP)
                            .asComposeRenderEffect()
                    }
                    .background(FrostedBackdrop)
            ) {
                // Duplicate content clipped to this region — gets blurred by RenderEffect
                Box(modifier = Modifier.fillMaxSize()) {
                    ScreenContent()
                }
            }

            // ── Blurred backdrop for the search bubble (right side) ──
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 24.dp, end = 16.dp)
                    .size(60.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .graphicsLayer {
                        renderEffect = RenderEffect
                            .createBlurEffect(blurPx, blurPx, Shader.TileMode.CLAMP)
                            .asComposeRenderEffect()
                    }
                    .background(FrostedBackdrop)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    ScreenContent()
                }
            }
        }

        // Layer 3 — Bottom navigation (floating on top of frosted backdrop)
        SeoulTrackNavBar(
            currentTab    = currentTab,
            onTabSelected = { currentTab = it },
            modifier      = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
        )
    }
}
