package com.adveno.tv.feature.sync

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adveno.tv.core.designsystem.TvBackgroundDark
import com.adveno.tv.core.designsystem.TvDimens
import com.adveno.tv.core.designsystem.TvOutline
import com.adveno.tv.core.designsystem.TvOutlineVariant
import com.adveno.tv.core.designsystem.TvPrimary
import com.adveno.tv.core.designsystem.TvPrimaryContainer
import com.adveno.tv.core.designsystem.TvSurfaceBorder
import com.adveno.tv.core.designsystem.TvSurfaceContainer
import com.adveno.tv.core.designsystem.TvSurfaceContainerHigh
import com.adveno.tv.core.designsystem.TvSurfaceContainerHighest
import com.adveno.tv.core.designsystem.TvSurfaceContainerLow
import com.adveno.tv.core.designsystem.TvSurfaceContainerLowest
import com.adveno.tv.core.designsystem.TvTertiary
import com.adveno.tv.core.designsystem.TvTextMuted
import com.adveno.tv.core.designsystem.TvTextPrimary
import com.adveno.tv.core.designsystem.TvTextSecondary
import com.adveno.tv.core.designsystem.components.TvButtonStyle
import com.adveno.tv.core.designsystem.components.TvFocusableButton
import com.adveno.tv.core.designsystem.components.TvFooter
import com.adveno.tv.core.designsystem.components.TvHeader
import kotlinx.coroutines.delay

@Composable
fun SyncScreen(
    onNavigate: (String) -> Unit,
    screenId: String = "ADV-8842-X"
) {
    var progressPercent by remember { mutableFloatStateOf(0.68f) }
    var currentItemIndex by remember { mutableIntStateOf(7) }

    LaunchedEffect(Unit) {
        while (progressPercent < 1.0f) {
            delay(1200)
            progressPercent = (progressPercent + 0.05f).coerceAtMost(1.0f)
            if (progressPercent > 0.85f && currentItemIndex < 10) {
                currentItemIndex++
            }
        }
    }

    val animatedProgress by animateFloatAsState(
        targetValue = progressPercent,
        animationSpec = tween(500),
        label = "sync_progress"
    )

    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        delay(200)
        focusRequester.requestFocus()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TvBackgroundDark)
    ) {
        // TOP REUSABLE TV HEADER
        TvHeader(
            currentRoute = "sync",
            onNavigate = onNavigate,
            screenId = screenId
        )

        // MAIN CONTENT CANVAS
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 36.dp, vertical = 10.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header Meta Strip
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(TvSurfaceContainerHigh)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(TvPrimary)
                                )
                                Text(
                                    text = "SYNC IN PROGRESS",
                                    color = TvPrimary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.8.sp
                                )
                            }
                        }

                        Text(text = "|", color = TvOutlineVariant, fontSize = 11.sp)

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = null,
                                tint = TvPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "CHANNEL 01 • PROD SIGNAGE",
                                color = TvTextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Text(
                        text = "CDN NODE: US-EAST-EDGE-09",
                        color = TvTextMuted,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Title & Subtitle
                Column {
                    Text(
                        text = "Preparing your advertisements",
                        color = TvTextPrimary,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.3).sp
                    )
                    Text(
                        text = "Downloading high-resolution media package from cloud CDN. Playback will begin automatically.",
                        color = TvTextSecondary,
                        fontSize = 13.sp
                    )
                }

                // Central Elevated Hardware Synchronization Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(TvSurfaceContainerLow)
                        .border(1.dp, TvSurfaceBorder, RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        // Metric Header Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CloudDownload,
                                        contentDescription = null,
                                        tint = TvPrimary,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Text(
                                        text = "ASSET PIPELINE",
                                        color = TvPrimary,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.8.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "Downloading $currentItemIndex of 10 items",
                                        color = TvTextPrimary,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "(${((currentItemIndex * 21)).coerceAtMost(210)} MB / 210 MB)",
                                        color = TvTextMuted,
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${(animatedProgress * 100).toInt()}%",
                                    color = TvTextPrimary,
                                    fontSize = 30.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "18.4 MB/s • Approx 3s remaining",
                                    color = TvTextMuted,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        // Precision Progress Rail
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(CircleShape)
                                .background(TvSurfaceContainerHighest)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(fraction = animatedProgress)
                                    .height(6.dp)
                                    .clip(CircleShape)
                                    .background(TvPrimary)
                            )
                        }

                        // Active File Status Row
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(TvSurfaceContainer)
                                .padding(horizontal = 12.dp, vertical = 7.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(TvSurfaceContainerHigh),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.VideoFile,
                                            contentDescription = null,
                                            tint = TvPrimary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    Column {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = "royal_biryani_promo_4k.jpg",
                                                color = TvTextPrimary,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(TvSurfaceContainerHighest)
                                                    .padding(horizontal = 5.dp, vertical = 1.5.dp)
                                            ) {
                                                Text(
                                                    text = "HEVC • 60fps",
                                                    color = TvPrimary,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    fontFamily = FontFamily.Monospace
                                                )
                                            }
                                        }
                                        Text(
                                            text = "Buffer allocation verified • SHA-256 chunk validated",
                                            color = TvTextMuted,
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Verified,
                                        contentDescription = null,
                                        tint = TvTertiary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "98% CHUNK OK",
                                        color = TvTertiary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }

                        // Tri-Column Metadata Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            SyncMetaSubCard(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Default.PlaylistPlay,
                                title = "ASSIGNED PLAYLIST",
                                primary = "Downtown Bistro Promo",
                                secondary = "SCHEDULE: 17:00 - 23:30 DAILY"
                            )
                            SyncMetaSubCard(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Default.Tv,
                                title = "DISPLAY TARGET",
                                primary = "$screenId (Main Dining)",
                                secondary = "ZONE: EAST WING FLOOR 1"
                            )
                            SyncMetaSubCard(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Default.Storage,
                                title = "STORAGE ALLOCATION",
                                primary = "1.4 GB / 32 GB Used",
                                secondary = "95.7% AVAILABLE • eMMC HEALTH OK"
                            )
                        }
                    }
                }

                // Bottom Notice Strip & Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = TvTextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Existing cached loop continues running in memory until synchronization completes.",
                            color = TvTextSecondary,
                            fontSize = 12.sp
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        TvFocusableButton(
                            text = "Return to Playback",
                            onClick = { onNavigate("playback") },
                            style = TvButtonStyle.PRIMARY,
                            keyHint = "OK",
                            modifier = Modifier.focusRequester(focusRequester)
                        )

                        TvFocusableButton(
                            text = "Diagnostics",
                            onClick = { onNavigate("diagnostics") },
                            style = TvButtonStyle.SECONDARY
                        )
                    }
                }
            }
        }

        // BOTTOM REUSABLE TV FOOTER
        TvFooter()
    }
}

@Composable
private fun SyncMetaSubCard(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    primary: String,
    secondary: String
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(TvSurfaceContainer)
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = TvOutline,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = title,
                    color = TvOutline,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
            Text(
                text = primary,
                color = TvTextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = secondary,
                color = TvTextMuted,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
