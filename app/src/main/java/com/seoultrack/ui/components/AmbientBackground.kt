package com.seoultrack.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.seoultrack.ui.theme.*

/**
 * Animated ambient orb background — replicates the HTML's .ambient-bg with
 * 4 floating radial gradient orbs that drift via CSS @keyframes orbFloat.
 *
 * CSS original:
 *   .orb:nth-child(1) { background: radial-gradient(circle, #ff4d6d 0%, transparent 70%);
 *                        top:-10%; left:-10%; animation-duration:22s }
 *   .orb:nth-child(2) { background: radial-gradient(circle, #4d9fff 0%, transparent 70%);
 *                        top:30%; right:-15%; animation-duration:26s }
 *   .orb:nth-child(3) { background: radial-gradient(circle, #a855f7 0%, transparent 70%);
 *                        bottom:10%; left:20%; animation-duration:24s }
 *   .orb:nth-child(4) { background: radial-gradient(circle, #34d399 0%, transparent 70%);
 *                        top:60%; left:-5%; animation-duration:28s }
 */
@Composable
fun AmbientBackground(modifier: Modifier = Modifier) {
    // Each orb animates with orbFloat: translate(40px,-30px), translate(-20px,40px), translate(30px,20px)
    // We replicate this as InfiniteTransition with phase offsets per orb

    val infiniteTransition = rememberInfiniteTransition(label = "orb")

    // Orb 1 — red/pink, top-left, 22s
    val orb1X by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(22000, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "orb1x"
    )
    val orb1Y by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(22000, easing = EaseInOutSine, delayMillis = 5500), RepeatMode.Reverse),
        label = "orb1y"
    )

    // Orb 2 — blue, top-right, 26s
    val orb2X by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(26000, easing = EaseInOutSine, delayMillis = 3000), RepeatMode.Reverse),
        label = "orb2x"
    )
    val orb2Y by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(26000, easing = EaseInOutSine, delayMillis = 8000), RepeatMode.Reverse),
        label = "orb2y"
    )

    // Orb 3 — purple, bottom-center, 24s
    val orb3X by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(24000, easing = EaseInOutSine, delayMillis = 6000), RepeatMode.Reverse),
        label = "orb3x"
    )
    val orb3Y by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(24000, easing = EaseInOutSine, delayMillis = 2000), RepeatMode.Reverse),
        label = "orb3y"
    )

    // Orb 4 — green, mid-left, 28s
    val orb4X by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(28000, easing = EaseInOutSine, delayMillis = 10000), RepeatMode.Reverse),
        label = "orb4x"
    )
    val orb4Y by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(28000, easing = EaseInOutSine, delayMillis = 4000), RepeatMode.Reverse),
        label = "orb4y"
    )

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .background(BgBase)
    ) {
        val w = size.width
        val h = size.height
        val drift = 80f  // px drift range (matches CSS 40px translate range roughly)

        // Orb 1 — red, anchored top-left (-10%, -10%)
        val o1BaseX = -w * 0.10f + w * 0.25f  // center of the 500px orb
        val o1BaseY = -h * 0.10f + h * 0.25f
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(OrbRed.copy(alpha = 0.35f), Color.Transparent),
                center = Offset(o1BaseX + (orb1X - 0.5f) * drift, o1BaseY + (orb1Y - 0.5f) * drift),
                radius = w * 0.65f,
            ),
            center = Offset(o1BaseX + (orb1X - 0.5f) * drift, o1BaseY + (orb1Y - 0.5f) * drift),
            radius = w * 0.65f,
        )

        // Orb 2 — blue, anchored top-right (right:-15%, top:30%)
        val o2BaseX = w + w * 0.15f - w * 0.40f
        val o2BaseY = h * 0.30f
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(OrbBlue.copy(alpha = 0.35f), Color.Transparent),
                center = Offset(o2BaseX + (orb2X - 0.5f) * drift, o2BaseY + (orb2Y - 0.5f) * drift),
                radius = w * 0.75f,
            ),
            center = Offset(o2BaseX + (orb2X - 0.5f) * drift, o2BaseY + (orb2Y - 0.5f) * drift),
            radius = w * 0.75f,
        )

        // Orb 3 — purple, anchored bottom-center (left:20%, bottom:10%)
        val o3BaseX = w * 0.20f + w * 0.25f
        val o3BaseY = h * 0.90f - h * 0.20f
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(OrbPurple.copy(alpha = 0.35f), Color.Transparent),
                center = Offset(o3BaseX + (orb3X - 0.5f) * drift, o3BaseY + (orb3Y - 0.5f) * drift),
                radius = w * 0.55f,
            ),
            center = Offset(o3BaseX + (orb3X - 0.5f) * drift, o3BaseY + (orb3Y - 0.5f) * drift),
            radius = w * 0.55f,
        )

        // Orb 4 — green, anchored mid-left (left:-5%, top:60%)
        val o4BaseX = -w * 0.05f + w * 0.22f
        val o4BaseY = h * 0.60f
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(OrbGreen.copy(alpha = 0.35f), Color.Transparent),
                center = Offset(o4BaseX + (orb4X - 0.5f) * drift, o4BaseY + (orb4Y - 0.5f) * drift),
                radius = w * 0.48f,
            ),
            center = Offset(o4BaseX + (orb4X - 0.5f) * drift, o4BaseY + (orb4Y - 0.5f) * drift),
            radius = w * 0.48f,
        )
    }
}

private val EaseInOutSine = CubicBezierEasing(0.37f, 0f, 0.63f, 1f)
