package com.seoultrack.ui.components

import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Modifier that applies a real GPU-accelerated Gaussian blur via RenderEffect (API 31+).
 *
 * This blurs the composable's OWN rendered content (not what's behind it).
 * For backdrop blur, apply this to a duplicate of the background content.
 *
 * On pre-Android 12, this is a no-op.
 */
fun Modifier.backdropBlur(radius: Dp = 20.dp): Modifier {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val blurModifier: Modifier = Modifier.graphicsLayer {
            val density = this.density
            val blurRadiusPx = with(density) { radius.toPx() }
            renderEffect = RenderEffect
                .createBlurEffect(
                    blurRadiusPx,
                    blurRadiusPx,
                    Shader.TileMode.CLAMP
                )
                .asComposeRenderEffect()
        }
        this.then(blurModifier)
    } else {
        this
    }
}
