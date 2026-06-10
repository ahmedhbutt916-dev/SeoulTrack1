package com.seoultrack.ui.components

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.seoultrack.ui.theme.*

/**
 * Draws the iridescent rainbow border that replicates the CSS:
 *
 *   background: linear-gradient(145deg,
 *     rgba(255,120,120,0.55) 0%,
 *     rgba(120,190,255,0.50) 20%,
 *     rgba(180,120,255,0.55) 40%,
 *     rgba(120,255,190,0.50) 60%,
 *     rgba(255,220,120,0.50) 80%,
 *     rgba(255,120,180,0.55) 100%);
 *   mask: content-box XOR border-box  →  border-only technique
 */
fun DrawScope.drawIridescentBorder(
    cornerRadius: Float,
    strokeWidth: Float = 3f,
) {
    val brush = Brush.linearGradient(
        colorStops = arrayOf(
            0.00f to IridescentRed,
            0.20f to IridescentBlue,
            0.40f to IridescentPurple,
            0.60f to IridescentGreen,
            0.80f to IridescentYellow,
            1.00f to IridescentPink,
        ),
        start = Offset(0f, 0f),
        end   = Offset(size.width, size.height),
    )
    drawRoundRect(
        brush        = brush,
        cornerRadius = CornerRadius(cornerRadius, cornerRadius),
        style        = Stroke(width = strokeWidth),
    )
}

/**
 * Draws the top specular highlight line — the thin white line near the top
 * of the pill/nav that sells the "glass" look:
 *
 *   background: linear-gradient(90deg, transparent, rgba(255,255,255,0.55), transparent)
 *   top: 4px; left: 12%; right: 12%; height: 1px
 */
fun DrawScope.drawTopReflection(cornerRadius: Float) {
    val left   = size.width * 0.12f
    val right  = size.width * 0.88f
    val y      = 5f
    drawLine(
        brush       = Brush.horizontalGradient(
            colors  = listOf(Color.Transparent, Color(0x8CFFFFFF), Color.Transparent),
            startX  = left,
            endX    = right,
        ),
        start       = Offset(left, y),
        end         = Offset(right, y),
        strokeWidth = 1.5f,
    )
    // Secondary dimmer line (top: 7px)
    val left2  = size.width * 0.25f
    val right2 = size.width * 0.70f
    val y2     = 9f
    drawLine(
        brush       = Brush.horizontalGradient(
            colors  = listOf(Color.Transparent, Color(0x33FFFFFF), Color.Transparent),
            startX  = left2,
            endX    = right2,
        ),
        start       = Offset(left2, y2),
        end         = Offset(right2, y2),
        strokeWidth = 1f,
    )
}

/**
 * Modifier that applies the full liquid glass look to any composable:
 *  - Frosted glass background (translucent gradient)
 *  - Iridescent border
 *  - Top specular reflection lines
 *  - Soft drop shadow
 *
 * On API 31+ this can be combined with a BlurMaskFilter for true blur;
 * the Compose BlurredEdgeTreatment doesn't exist, so we rely on the
 * translucent background to approximate backdrop-filter visually.
 */
fun Modifier.liquidGlass(
    shape: Shape,
    cornerRadiusDp: Float = 60f,
    borderWidthPx: Float = 3f,
): Modifier = this
    .drawBehind {
        // Shadow underneath (replicates box-shadow: 0 8px 40px rgba(0,0,0,0.3))
        drawRoundRect(
            color        = Color(0x4D000000),
            topLeft      = Offset(0f, 8.dp.toPx()),
            size         = Size(size.width, size.height + 8.dp.toPx()),
            cornerRadius = CornerRadius(cornerRadiusDp.dp.toPx()),
            blurRadius   = 40.dp.toPx(),  // Note: requires custom draw for real blur; approximated
        )
    }
    .background(
        brush = Brush.linearGradient(
            colorStops = arrayOf(
                0.00f to Color(0x38FFFFFF),  // rgba(255,255,255,0.22)
                0.25f to Color(0x1AFFFFFF),  // rgba(255,255,255,0.10)
                0.50f to Color(0x29FFFFFF),  // rgba(255,255,255,0.16)
                0.75f to Color(0x0FFFFFFF),  // rgba(255,255,255,0.06)
                1.00f to Color(0x21FFFFFF),  // rgba(255,255,255,0.13)
            ),
            start = Offset(0f, 0f),
            end   = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
        ),
        shape = shape,
    )
    .drawWithContent {
        drawContent()
        // Iridescent border on top of content
        drawIridescentBorder(cornerRadiusDp.dp.toPx(), borderWidthPx)
        // Specular reflection lines
        drawTopReflection(cornerRadiusDp.dp.toPx())
    }

// Extension for DrawScope that accepts Dp blur (approximate)
fun DrawScope.drawRoundRect(
    color: Color,
    topLeft: Offset = Offset.Zero,
    size: Size = this.size,
    cornerRadius: CornerRadius = CornerRadius.Zero,
    blurRadius: Float,
) {
    // Compose doesn't support blur in DrawScope directly.
    // For real blur, use RenderEffect on the View level (see AmbientBackground.kt)
    // Here we approximate with a semi-transparent fill.
    drawRoundRect(color = color, topLeft = topLeft, size = size, cornerRadius = cornerRadius)
}
