package com.seoultrack.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.seoultrack.ui.components.AmbientBackground
import com.seoultrack.ui.components.NavTab
import com.seoultrack.ui.components.SeoulTrackNavBar

/**
 * Main screen scaffold for SeoulTrack.
 *
 * Layout mirrors the HTML structure:
 *   - AmbientBackground (position: fixed, z-index: 0)
 *   - Content area (full-screen, content scrolls BEHIND the nav bar)
 *   - SeoulTrackNavBar (position: fixed, bottom: 24dp, z-index: 100) — truly floating over content
 *
 * Content fills the entire screen so it's visible through the translucent glass nav bar.
 * Each screen adds its own bottom padding so the last items can be scrolled above the nav bar.
 */
@Composable
fun MainScreen() {
    var currentTab by remember { mutableStateOf(NavTab.DISCOVER) }

    Box(modifier = Modifier.fillMaxSize()) {

        // Layer 0 — Ambient animated orb background
        AmbientBackground(modifier = Modifier.fillMaxSize())

        // Layer 1 — Page content based on selected tab
        // Content fills the ENTIRE screen so it scrolls behind the floating nav bar
        // Each screen handles its own bottom padding for scroll space
        Box(modifier = Modifier.fillMaxSize()) {
            when (currentTab) {
                NavTab.DISCOVER  -> DiscoverScreen()
                NavTab.LIBRARY   -> LibraryScreen()
                NavTab.PROFILE   -> ProfileScreen()
                NavTab.SETTINGS  -> SettingsScreen()
            }
        }

        // Layer 2 — Bottom navigation (always on top, truly floating over content)
        SeoulTrackNavBar(
            currentTab    = currentTab,
            onTabSelected = { currentTab = it },
            modifier      = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
        )
    }
}
