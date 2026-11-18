package com.adveno.tv.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.adveno.tv.core.designsystem.TvBackgroundDark

/**
 * AdvenoBackground renders the cosmic midnight base with diffused violet/purple
 * aurora radial glow circles, matching the signature Figma dark aesthetic.
 */
@Composable
fun AdvenoBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TvBackgroundDark)
            .drawBehind {
                val canvasWidth = size.width
                val canvasHeight = size.height

                // Top-left soft violet aura
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0x386D28D9),
                            Color(0x1A4C1D95),
                            Color.Transparent
                        ),
                        center = Offset(canvasWidth * 0.12f, canvasHeight * 0.22f),
                        radius = canvasWidth * 0.42f
                    )
                )

                // Bottom-right deep purple aura
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0x407C3AED),
                            Color(0x1F581C87),
                            Color.Transparent
                        ),
                        center = Offset(canvasWidth * 0.88f, canvasHeight * 0.82f),
                        radius = canvasWidth * 0.46f
                    )
                )

                // Subtle center radial glow
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0x228B5CF6),
                            Color.Transparent
                        ),
                        center = Offset(canvasWidth * 0.5f, canvasHeight * 0.5f),
                        radius = canvasWidth * 0.35f
                    )
                )
            }
    ) {
        content()
    }
}

