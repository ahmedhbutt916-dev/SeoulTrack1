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
 *   - Header (position: fixed, z-index: 90)
 *   - Content area (scrollable, padding-bottom: 96px)
 *   - SeoulTrackNavBar (position: fixed, bottom: 0, z-index: 100)
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
                .padding(bottom = 92.dp)  // matches HTML's padding-bottom: 96px
        ) {
            when (currentTab) {
                NavTab.DISCOVER  -> DiscoverScreen()
                NavTab.LIBRARY   -> LibraryScreen()
                NavTab.PROFILE   -> ProfileScreen()
                NavTab.SETTINGS  -> SettingsScreen()
            }
        }

        // Layer 2 — Bottom navigation (always on top)
        SeoulTrackNavBar(
            currentTab    = currentTab,
            onTabSelected = { currentTab = it },
            modifier      = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
        )
    }
}
