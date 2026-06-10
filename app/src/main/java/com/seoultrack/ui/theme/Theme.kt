package com.seoultrack.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

// Inter font family — matches the HTML's primary typeface
val InterFamily = FontFamily.Default  // Replace with actual Inter font files if bundled

private val SeoulTrackDarkColors = darkColorScheme(
    background  = BgBase,
    surface     = BgCard,
    primary     = Accent,
    onPrimary   = TextMain,
    onBackground= TextMain,
    onSurface   = TextMain,
    secondary   = StatusWatching,
    tertiary    = StatusWatched,
    outline     = GlassBorder,
)

@Composable
fun SeoulTrackTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = SeoulTrackDarkColors,
        typography  = SeoulTrackTypography,
        content     = content
    )
}
