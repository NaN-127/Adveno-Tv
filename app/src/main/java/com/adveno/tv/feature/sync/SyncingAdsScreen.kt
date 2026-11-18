package com.adveno.tv.feature.sync

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.adveno.tv.core.designsystem.TvTextLavender
import com.adveno.tv.core.designsystem.TvTextPrimary
import com.adveno.tv.core.designsystem.TvTextSecondary
import com.adveno.tv.core.designsystem.components.AdvenoBackground
import com.adveno.tv.core.designsystem.components.AdvenoLogo
import kotlinx.coroutines.delay

/**
 * Screen 05: Syncing Ads (Figma Design 5)
 * Rotating halo around floating media icons with sleek glowing progress bar.
 */
@Composable
fun SyncingAdsScreen(
    onSyncComplete: () -> Unit,
    modifier: Modifier = Modifier,
    initialProgress: Float = 0.70f,
    autoAdvance: Boolean = true
) {
    var progress by remember { mutableFloatStateOf(initialProgress) }

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(600),
        label = "sync_progress"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "sync_spin")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "halo_rotation"
    )

    LaunchedEffect(Unit) {
        if (autoAdvance) {
            delay(2000)
            progress = 0.70f
            delay(2500)
            progress = 0.92f
            delay(1500)
            progress = 1.0f
            delay(800)
            onSyncComplete()
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

            Spacer(modifier = Modifier.height(32.dp))

            // Rotating Halo around Floating Media Graphic
            Box(
                modifier = Modifier
                    .size(136.dp)
                    .drawBehind {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0x558B5CF6),
                                    Color(0x207C3AED),
                                    Color.Transparent
                                ),
                                center = Offset(size.width / 2f, size.height / 2f),
                                radius = size.width * 0.75f
                            )
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                // Background Track
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        color = Color(0x2E8B5CF6),
                        style = Stroke(width = 5.dp.toPx())
                    )
                }

                // Rotating Glowing Segments
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .rotate(rotation)
                ) {
                    drawArc(
                        brush = Brush.sweepGradient(
                            listOf(
                                Color(0x10A855F7),
                                Color(0x66A855F7),
                                Color(0xFFC084FC),
                                Color(0xFFFFFFFF)
                            )
                        ),
                        startAngle = 20f,
                        sweepAngle = 180f,
                        useCenter = false,
                        style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                // Floating Media Card Composition (Center Image + Flanking Video Badges)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    // Left subtle card
                    Box(
                        modifier = Modifier
                            .size(36.dp, 44.dp)
                            .background(Color(0x2B4C1D95), RoundedCornerShape(6.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color(0x55DDD6FE),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Main Center Active Media Card
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 6.dp)
                            .size(54.dp, 54.dp)
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF5B21B6), Color(0xFF7C3AED))
                                ),
                                RoundedCornerShape(10.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = null,
                            tint = Color(0xFFFFFFFF),
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    // Right subtle card
                    Box(
                        modifier = Modifier
                            .size(36.dp, 44.dp)
                            .background(Color(0x2B4C1D95), RoundedCornerShape(6.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color(0x55DDD6FE),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "Syncing Ads...",
                color = TvTextPrimary,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Downloading the latest content to this screen.",
                color = TvTextSecondary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Normal
            )

            Spacer(modifier = Modifier.height(30.dp))

            // Sleek Glowing Progress Bar with Percentage Tag
            Row(
                modifier = Modifier.width(420.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Progress Track
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF1E1538))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animatedProgress)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color(0xFF7C3AED),
                                        Color(0xFFA855F7),
                                        Color(0xFFC084FC)
                                    )
                                )
                            )
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = "${(animatedProgress * 100).toInt()}%",
                    color = TvTextLavender,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
