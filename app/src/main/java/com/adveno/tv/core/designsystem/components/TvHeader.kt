package com.adveno.tv.core.designsystem.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adveno.tv.core.designsystem.TvDimens
import com.adveno.tv.core.designsystem.TvOnPrimaryContainer
import com.adveno.tv.core.designsystem.TvOutlineVariant
import com.adveno.tv.core.designsystem.TvPrimary
import com.adveno.tv.core.designsystem.TvPrimaryContainer
import com.adveno.tv.core.designsystem.TvSurfaceBorder
import com.adveno.tv.core.designsystem.TvSurfaceContainer
import com.adveno.tv.core.designsystem.TvSurfaceContainerHigh
import com.adveno.tv.core.designsystem.TvSurfaceContainerLowest
import com.adveno.tv.core.designsystem.TvTertiary
import com.adveno.tv.core.designsystem.TvTextMuted
import com.adveno.tv.core.designsystem.TvTextPrimary
import com.adveno.tv.core.designsystem.TvTextSecondary

@Composable
fun TvHeader(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    screenId: String = "ADV-8842-X",
    ipAddress: String = "192.168.1.144",
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(TvSurfaceContainerLowest.copy(alpha = 0.95f))
            .border(width = 1.dp, color = TvSurfaceBorder, shape = RoundedCornerShape(0.dp))
            .padding(horizontal = 36.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // LEFT: Brand + Enterprise Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(TvPrimaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Tv,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Text(
                    text = "Adveno TV",
                    color = TvTextPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.3).sp
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(TvSurfaceContainerHigh)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "ENTERPRISE SIGNAGE",
                        color = TvTextSecondary,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp
                    )
                }
            }

            // CENTER: Navigation Tabs
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                TvHeaderTab(
                    title = "Playback",
                    route = "playback",
                    isSelected = currentRoute == "playback",
                    onClick = { onNavigate("playback") }
                )
                TvHeaderTab(
                    title = "Pairing",
                    route = "pairing",
                    isSelected = currentRoute == "pairing",
                    onClick = { onNavigate("pairing") }
                )
                TvHeaderTab(
                    title = "Sync",
                    route = "sync",
                    isSelected = currentRoute == "sync",
                    onClick = { onNavigate("sync") }
                )
                TvHeaderTab(
                    title = "Diagnostics",
                    route = "diagnostics",
                    isSelected = currentRoute == "diagnostics",
                    onClick = { onNavigate("diagnostics") }
                )
            }

            // RIGHT: Live Node Telemetry Capsule (Fixed-size, Single-line, No Wrapping)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(TvSurfaceContainer)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(TvTertiary)
                    )
                    Text(
                        text = screenId,
                        color = TvTextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1,
                        softWrap = false
                    )
                    Text(
                        text = "•",
                        color = TvOutlineVariant,
                        fontSize = 11.sp
                    )
                    Text(
                        text = ipAddress,
                        color = TvTextMuted,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
    }
}

@Composable
private fun TvHeaderTab(
    title: String,
    route: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }

    val bgColor by animateColorAsState(
        targetValue = when {
            isSelected -> TvPrimaryContainer
            isFocused -> TvSurfaceContainerHigh
            else -> Color.Transparent
        },
        label = "tab_bg"
    )

    val textColor by animateColorAsState(
        targetValue = when {
            isSelected -> TvOnPrimaryContainer
            isFocused -> TvTextPrimary
            else -> TvTextSecondary
        },
        label = "tab_text"
    )

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .onFocusChanged { isFocused = it.isFocused }
            .focusable(interactionSource = interactionSource)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 12.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            color = textColor,
            fontSize = 13.sp,
            fontWeight = if (isSelected || isFocused) FontWeight.Bold else FontWeight.Medium
        )
    }
}

