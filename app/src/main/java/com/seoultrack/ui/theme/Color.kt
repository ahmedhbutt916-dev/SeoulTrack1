package com.seoultrack.ui.theme

import androidx.compose.ui.graphics.Color

// === SeoulTrack Color System — mirrors HTML CSS variables exactly ===

// Base
val BgBase = Color(0xFF0A0E1A)
val BgCard = Color(0x0AFFFFFF)         // rgba(255,255,255,0.04)
val BgCardHover = Color(0x14FFFFFF)    // rgba(255,255,255,0.08)

// Glass surface
val GlassBg = Color(0x0FFFFFFF)        // rgba(255,255,255,0.06)
val GlassBorder = Color(0x1FFFFFFF)    // rgba(255,255,255,0.12)
val GlassHighlight = Color(0x2EFFFFFF) // rgba(255,255,255,0.18)
val GlassSpecular = Color(0x40FFFFFF)  // rgba(255,255,255,0.25)

// Text
val TextMain = Color(0xFFF0F2F8)
val TextMuted = Color(0xFF8892A8)

// Accent — the pink-red
val Accent = Color(0xFFFF4D6D)
val AccentGlow = Color(0x66FF4D6D)     // rgba(255,77,109,0.4)

// Status colors
val StatusWatching = Color(0xFF4D9FFF)
val StatusWatched = Color(0xFF34D399)
val StatusPlan = Color(0xFFFBBF24)

// Nav glass gradient stops
val NavGlassLight = Color(0x38FFFFFF)  // rgba(255,255,255,0.22)
val NavGlassMid = Color(0x1AFFFFFF)   // rgba(255,255,255,0.10)
val NavGlassDark = Color(0x0FFFFFFF)  // rgba(255,255,255,0.06)

// Iridescent border colors (the rainbow shimmer)
val IridescentRed   = Color(0x8CFF7878)  // rgba(255,120,120,0.55)
val IridescentBlue  = Color(0x8078BEFF)  // rgba(120,190,255,0.50)
val IridescentPurple= Color(0x8CB478FF)  // rgba(180,120,255,0.55)
val IridescentGreen = Color(0x8078FFBE)  // rgba(120,255,190,0.50)
val IridescentYellow= Color(0x80FFDC78)  // rgba(255,220,120,0.50)
val IridescentPink  = Color(0x8CFF78B4)  // rgba(255,120,180,0.55)

// Pill indicator
val PillBg          = Color(0x26FF4D6D)  // rgba(255,77,109,0.15)
val PillBorder      = Color(0x40FF4D6D)  // rgba(255,77,109,0.25)

// OLED theme overrides
val OledBgBase = Color(0xFF000000)
val OledGlassBg = Color(0x0AFFFFFF)
val OledGlassBorder = Color(0x14FFFFFF)
val OledTextMain = Color(0xFFE8EAF0)
val OledTextMuted = Color(0xFF6B7280)

// Frosted glass backdrop tint (same as BgBase at ~70% opacity)
val FrostedBackdrop = Color(0xB30A0E1A)  // rgba(10,14,26,0.70)

// Ambient orb colors
val OrbRed    = Color(0xFFFF4D6D)
val OrbBlue   = Color(0xFF4D9FFF)
val OrbPurple = Color(0xFFA855F7)
val OrbGreen  = Color(0xFF34D399)
