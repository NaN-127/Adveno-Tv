package com.adveno.tv.feature.pairing

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adveno.tv.core.designsystem.TvTextPrimary
import com.adveno.tv.core.designsystem.TvTextSecondary
import com.adveno.tv.core.designsystem.components.AdvenoBackground
import com.adveno.tv.core.designsystem.components.AdvenoLogo
import kotlinx.coroutines.delay

/**
 * Screen 04: Connecting State (Figma Design 4)
 * Glowing animated circular ring around link icon indicating active device claiming.
 */
@Composable
fun ConnectingScreen(
    onConnected: () -> Unit,
    modifier: Modifier = Modifier,
    autoTransitionDelayMs: Long = 7000L
) {
    val infiniteTransition = rememberInfiniteTransition(label = "connecting_spin")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring_rotation"
    )

    LaunchedEffect(Unit) {
        if (autoTransitionDelayMs > 0) {
            delay(autoTransitionDelayMs)
            onConnected()
        }
    }

    AdvenoBackground(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AdvenoLogo(
                iconSize = 44.dp,
                fontSize = 32.sp
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Glowing Spinner Ring with Link Icon inside
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .drawBehind {
                        // Ambient radial purple halo behind ring
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0x55A855F7),
                                    Color(0x227C3AED),
                                    Color.Transparent
                                ),
                                center = Offset(size.width / 2f, size.height / 2f),
                                radius = size.width * 0.7f
                            )
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                // Background Track
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        color = Color(0x2E8B5CF6),
                        style = Stroke(width = 6.dp.toPx())
                    )
                }

                // Rotating Glowing Arc
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .rotate(rotation)
                ) {
                    drawArc(
                        brush = Brush.sweepGradient(
                            listOf(
                                Color(0x00A855F7),
                                Color(0x66A855F7),
                                Color(0xFFC084FC),
                                Color(0xFFFFFFFF)
                            )
                        ),
                        startAngle = 0f,
                        sweepAngle = 260f,
                        useCenter = false,
                        style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                // Center Icon with soft dark glass circle
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .background(Color(0x331E1438), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Link,
                        contentDescription = "Connecting",
                        tint = Color(0xFFE9D5FF),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Connecting...",
                color = TvTextPrimary,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Linking this screen to your Adveno account",
                color = TvTextSecondary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Normal
            )
        }
    }
}
