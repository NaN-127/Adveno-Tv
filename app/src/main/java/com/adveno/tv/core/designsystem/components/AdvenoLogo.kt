package com.adveno.tv.core.designsystem.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adveno.tv.core.designsystem.TvTextPrimary

/**
 * Modern vector-rendered Adveno brand logo with rounded play triangle
 * and sleek white typographic branding.
 */
@Composable
fun AdvenoLogo(
    modifier: Modifier = Modifier,
    iconSize: Dp = 38.dp,
    fontSize: TextUnit = 28.sp,
    showText: Boolean = true
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Canvas(modifier = Modifier.size(iconSize)) {
            val w = size.width
            val h = size.height

            val gradient = Brush.linearGradient(
                colors = listOf(
                    Color(0xFFA855F7),
                    Color(0xFF7C3AED),
                    Color(0xFF6366F1)
                ),
                start = Offset(0f, 0f),
                end = Offset(w, h)
            )

            // Outer rounded play triangle path
            val path = Path().apply {
                val r = w * 0.16f // corner rounding radius
                // Start near top-left rounded apex
                moveTo(w * 0.22f + r, h * 0.12f)
                // Line toward right vertex
                lineTo(w * 0.88f - r * 1.5f, h * 0.44f)
                // Right rounded corner
                quadraticBezierTo(w * 0.95f, h * 0.50f, w * 0.88f - r * 1.5f, h * 0.56f)
                // Line toward bottom-left vertex
                lineTo(w * 0.22f + r, h * 0.88f)
                // Bottom-left rounded corner
                quadraticBezierTo(w * 0.14f, h * 0.92f, w * 0.14f, h * 0.82f)
                // Line toward top-left vertex
                lineTo(w * 0.14f, h * 0.18f)
                // Top-left rounded corner
                quadraticBezierTo(w * 0.14f, h * 0.08f, w * 0.22f + r, h * 0.12f)
                close()
            }

            drawPath(path = path, brush = gradient)

            // Inner subtle translucent cut
            val innerPath = Path().apply {
                moveTo(w * 0.32f, h * 0.30f)
                lineTo(w * 0.68f, h * 0.50f)
                lineTo(w * 0.32f, h * 0.70f)
                close()
            }
            drawPath(path = innerPath, color = Color(0x38000000))
        }

        if (showText) {
            Text(
                text = "Adveno",
                color = TvTextPrimary,
                fontSize = fontSize,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp
            )
        }
    }
}

