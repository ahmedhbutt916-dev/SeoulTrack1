package com.seoultrack.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.seoultrack.ui.components.AmbientBackground
import com.seoultrack.ui.components.NavTab
import com.seoultrack.ui.components.SeoulTrackNavBar

/**
 * Main screen scaffold for SeoulTrack.
 *
 * Layout mirrors the HTML structure:
 *   - AmbientBackground (position: fixed, z-index: 0)
 *   - Content area (scrollable, padding-bottom for floating nav)
 *   - SeoulTrackNavBar (position: fixed, bottom: 24dp, z-index: 100) — floating
 */
@Composable
fun MainScreen() {
    var currentTab by remember { mutableStateOf(NavTab.DISCOVER) }

    Box(modifier = Modifier.fillMaxSize()) {

        // Layer 0 — Ambient animated orb background
        AmbientBackground(modifier = Modifier.fillMaxSize())

        // Layer 1 — Page content based on selected tab
        Box(
            modifier = Modifier
                .fillMaxSize()
                // 60dp nav + 24dp bottom padding + 16dp extra breathing room
                .padding(bottom = 100.dp)
        ) {
            when (currentTab) {
                NavTab.DISCOVER  -> DiscoverScreen()
                NavTab.LIBRARY   -> LibraryScreen()
                NavTab.PROFILE   -> ProfileScreen()
                NavTab.SETTINGS  -> SettingsScreen()
            }
        }

        // Layer 2 — Bottom navigation (always on top, floating)
        SeoulTrackNavBar(
            currentTab    = currentTab,
            onTabSelected = { currentTab = it },
            modifier      = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
        )
    }
}
