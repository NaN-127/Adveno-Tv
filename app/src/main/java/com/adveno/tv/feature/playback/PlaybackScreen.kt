package com.adveno.tv.feature.playback

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerView
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.adveno.tv.core.designsystem.TvBackgroundDark
import com.adveno.tv.core.designsystem.TvTextLavender
import com.adveno.tv.core.designsystem.TvTextMuted
import com.adveno.tv.core.designsystem.TvTextPrimary
import com.adveno.tv.core.designsystem.TvTextSecondary
import com.adveno.tv.core.designsystem.components.AdvenoLogo
import com.adveno.tv.core.designsystem.components.TvButtonStyle
import com.adveno.tv.core.designsystem.components.TvFocusableButton
import com.adveno.tv.domain.model.MediaType
import com.adveno.tv.playback.engine.SignageExoPlayerWrapper
import org.koin.androidx.compose.koinViewModel

/**
 * Screen 06: Fullscreen Playback (Figma Design 6)
 * Ultra-clean edge-to-edge ad display with minimal top watermark, logout button,
 * and bottom glowing progress indicator.
 */
@OptIn(UnstableApi::class)
@Composable
fun PlaybackScreen(
    onNavigateToDiagnostics: () -> Unit = {},
    onLogout: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: PlaybackViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val exoPlayerWrapper = remember {
        SignageExoPlayerWrapper(
            context = context,
            onVideoEnded = { viewModel.onVideoCompleted() },
            onError = { err ->
                uiState.currentItem?.let { item ->
                    viewModel.onMediaError(item.id, err)
                }
            }
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayerWrapper.release()
        }
    }

    LaunchedEffect(uiState.currentItem) {
        val item = uiState.currentItem
        if (item != null && item.type == MediaType.VIDEO) {
            exoPlayerWrapper.playVideo(item.playableUri)
        } else {
            exoPlayerWrapper.stop()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TvBackgroundDark)
    ) {
        // 1. ACTIVE AD CONTENT CANVAS
        val currentItem = uiState.currentItem
        if (currentItem != null && currentItem.localFilePath != null) {
            when (currentItem.type) {
                MediaType.VIDEO -> {
                    AndroidView(
                        factory = { ctx ->
                            PlayerView(ctx).apply {
                                player = exoPlayerWrapper.exoPlayer
                                useController = false
                                layoutParams = FrameLayout.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                MediaType.IMAGE -> {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(currentItem.playableUri)
                            .crossfade(300)
                            .build(),
                        contentDescription = currentItem.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        } else {
            // Default Luxury Premium Audio Ad Composition (Exact Match to Figma Image 6)
            DefaultHeadphonesAd()
        }

        // 2. TOP WATERMARK & LOGOUT BAR
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp, vertical = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AdvenoLogo(
                iconSize = 28.dp,
                fontSize = 20.sp
            )

            TvFocusableButton(
                text = "Logout",
                onClick = {
                    viewModel.logout(onLogout)
                },
                icon = Icons.Default.ExitToApp,
                style = TvButtonStyle.SECONDARY,
                height = 38.dp,
                cornerRadius = 10.dp
            )
        }

        // 3. BOTTOM SLEEK GLOWING PROGRESS BAR & REFRESH ICON
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 32.dp, vertical = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Progress Capsule Track
            val animatedProgress by animateFloatAsState(
                targetValue = uiState.progress.coerceIn(0.05f, 1.0f),
                animationSpec = tween(300),
                label = "ad_playback_progress"
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0x331E1538))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(3.dp))
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

            Spacer(modifier = Modifier.width(18.dp))

            // Refresh / Skip Action Pill
            var isRefreshFocused by remember { mutableStateOf(false) }
            val interactionSource = remember { MutableInteractionSource() }

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .scale(if (isRefreshFocused) 1.08f else 1.0f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isRefreshFocused) Color(0x6633215C) else Color(0x2E1E1438))
                    .border(
                        width = if (isRefreshFocused) 1.5.dp else 1.dp,
                        color = if (isRefreshFocused) Color(0xFFA855F7) else Color(0x33FFFFFF),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .onFocusChanged { isRefreshFocused = it.isFocused }
                    .focusable(interactionSource = interactionSource)
                    .clickable { viewModel.nextItem() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Next Ad",
                    tint = if (isRefreshFocused) Color.White else TvTextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

/**
 * Editorial Premium Audio Ad Layout matching Figma Screen 06
 */
@Composable
private fun DefaultHeadphonesAd() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF07040D))
            .drawBehind {
                val w = size.width
                val h = size.height

                // Ambient purple aurora ribbons
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0x557C3AED),
                            Color(0x1F4C1D95),
                            Color.Transparent
                        ),
                        center = Offset(w * 0.72f, h * 0.55f),
                        radius = w * 0.45f
                    )
                )

                // Fluid purple light wave
                val wavePath = Path().apply {
                    moveTo(0f, h * 0.85f)
                    cubicTo(
                        w * 0.35f, h * 0.95f,
                        w * 0.65f, h * 0.45f,
                        w, h * 0.35f
                    )
                    lineTo(w, h)
                    lineTo(0f, h)
                    close()
                }
                drawPath(
                    path = wavePath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0x288B5CF6),
                            Color(0x05000000)
                        )
                    )
                )
            }
            .padding(start = 72.dp, end = 72.dp, top = 60.dp, bottom = 60.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left Headline Column
            Column(
                modifier = Modifier.weight(1.1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "PREMIUM AUDIO",
                    color = Color(0xFFC4B5FD),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.5.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Feel the\nSound",
                    color = Color.White,
                    fontSize = 44.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontStyle = FontStyle.Italic,
                    lineHeight = 50.sp,
                    letterSpacing = (-1.0).sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Purple accent line
                Box(
                    modifier = Modifier
                        .size(width = 36.dp, height = 3.dp)
                        .background(Color(0xFFA855F7), RoundedCornerShape(1.5.dp))
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Wireless Headphones\nfor a Better Everyday",
                    color = Color(0xFFA19EB7),
                    fontSize = 16.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.Normal
                )
            }

            // Right Visual Canvas: High-End Headphone Graphic Composition
            Box(
                modifier = Modifier
                    .weight(1.2f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                // Outer glowing halo
                Box(
                    modifier = Modifier
                        .size(240.dp)
                        .background(
                            Brush.radialGradient(
                                listOf(Color(0x408B5CF6), Color.Transparent)
                            ),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // Stylized Headphone Ear Cup and Headband
                    Canvas(modifier = Modifier.size(190.dp)) {
                        val cx = size.width / 2f
                        val cy = size.height / 2f

                        // Headband arch
                        drawArc(
                            color = Color(0xFF261D42),
                            startAngle = 180f,
                            sweepAngle = 180f,
                            useCenter = false,
                            style = Stroke(width = 18.dp.toPx(), cap = StrokeCap.Round)
                        )

                        // Outer metallic headband rim
                        drawArc(
                            brush = Brush.sweepGradient(
                                listOf(Color(0xFF7C3AED), Color(0xFFA855F7), Color(0xFFC084FC))
                            ),
                            startAngle = 200f,
                            sweepAngle = 140f,
                            useCenter = false,
                            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                        )

                        // Left Ear Cup
                        drawOval(
                            brush = Brush.linearGradient(
                                listOf(Color(0xFF1E1738), Color(0xFF0F0B1D))
                            ),
                            topLeft = Offset(cx - 75.dp.toPx(), cy - 20.dp.toPx()),
                            size = androidx.compose.ui.geometry.Size(42.dp.toPx(), 65.dp.toPx())
                        )
                        drawOval(
                            color = Color(0x66A855F7),
                            topLeft = Offset(cx - 75.dp.toPx(), cy - 20.dp.toPx()),
                            size = androidx.compose.ui.geometry.Size(42.dp.toPx(), 65.dp.toPx()),
                            style = Stroke(width = 2.dp.toPx())
                        )

                        // Right Ear Cup (Facing Front)
                        drawOval(
                            brush = Brush.linearGradient(
                                listOf(Color(0xFF2D1F52), Color(0xFF120B24))
                            ),
                            topLeft = Offset(cx + 25.dp.toPx(), cy - 25.dp.toPx()),
                            size = androidx.compose.ui.geometry.Size(52.dp.toPx(), 75.dp.toPx())
                        )
                        drawOval(
                            brush = Brush.sweepGradient(
                                listOf(Color(0xFF7C3AED), Color(0xFFA855F7), Color(0xFFC084FC))
                            ),
                            topLeft = Offset(cx + 25.dp.toPx(), cy - 25.dp.toPx()),
                            size = androidx.compose.ui.geometry.Size(52.dp.toPx(), 75.dp.toPx()),
                            style = Stroke(width = 2.5.dp.toPx())
                        )

                        // Center Adveno Triangle Logo on Right Ear Cup
                        val logoPath = Path().apply {
                            val lx = cx + 51.dp.toPx()
                            val ly = cy + 12.dp.toPx()
                            moveTo(lx - 8.dp.toPx(), ly - 10.dp.toPx())
                            lineTo(lx + 8.dp.toPx(), ly)
                            lineTo(lx - 8.dp.toPx(), ly + 10.dp.toPx())
                            close()
                        }
                        drawPath(path = logoPath, color = Color(0xFFC084FC))
                    }
                }
            }
        }
    }
}
