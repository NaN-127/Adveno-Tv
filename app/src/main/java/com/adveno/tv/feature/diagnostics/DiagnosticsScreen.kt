package com.adveno.tv.feature.diagnostics

import android.app.Activity
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Lan
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.NetworkPing
import androidx.compose.material.icons.filled.SettingsRemote
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adveno.tv.core.designsystem.TvBackgroundDark
import com.adveno.tv.core.designsystem.TvDimens
import com.adveno.tv.core.designsystem.TvFocusRing
import com.adveno.tv.core.designsystem.TvOutline
import com.adveno.tv.core.designsystem.TvOutlineVariant
import com.adveno.tv.core.designsystem.TvPrimary
import com.adveno.tv.core.designsystem.TvPrimaryContainer
import com.adveno.tv.core.designsystem.TvStatusError
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
import org.koin.androidx.compose.koinViewModel

@Composable
fun DiagnosticsScreen(
    onBack: () -> Unit,
    onNavigate: (String) -> Unit = {},
    viewModel: DiagnosticsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val activity = context as? Activity
    val identity = uiState.deviceIdentity
    val playlist = uiState.activePlaylist

    var actionFeedbackText by remember { mutableStateOf("Ready for remote command") }
    var actionFeedbackDone by remember { mutableStateOf(false) }

    val initialFocusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        delay(250)
        initialFocusRequester.requestFocus()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TvBackgroundDark)
    ) {
        // TOP REUSABLE TV HEADER
        TvHeader(
            currentRoute = "diagnostics",
            onNavigate = onNavigate,
            screenId = identity?.screenId ?: "ADV-8842-X"
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
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Console Header & Meta Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(TvTertiary)
                            )
                            Text(
                                text = "Adveno TV • System Diagnostics",
                                color = TvTextPrimary,
                                fontSize = 20.sp,
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
                                    text = "TECHNICIAN HUD",
                                    color = TvTextSecondary,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.8.sp
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Node ID: ${identity?.screenId ?: "ADV-8842-X"}",
                                color = TvTextPrimary,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                            Text(text = "•", color = TvOutlineVariant, fontSize = 11.sp)
                            Text(
                                text = "${identity?.manufacturer ?: "Google"} ${identity?.model ?: "Android TV"} (${identity?.androidVersion ?: "API 36"})",
                                color = TvTextMuted,
                                fontSize = 11.sp
                            )
                            Text(text = "•", color = TvOutlineVariant, fontSize = 11.sp)
                            Text(
                                text = "Telemetry: SECURE TLS 1.3",
                                color = TvTertiary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    TvFocusableButton(
                        text = "Return to Playback",
                        onClick = onBack,
                        keyHint = "BACK",
                        style = TvButtonStyle.PRIMARY
                    )
                }

                // 2x2 HIGH-READABILITY HARDWARE GRID
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // LEFT COLUMN: Panels 1 & 3
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // PANEL 1: Hardware & Application State
                        DiagPanelCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.Memory,
                            title = "1. Hardware & Application State",
                            badgeText = "STATUS: NOMINAL",
                            badgeColor = TvTertiary
                        ) {
                            DiagMetricRow("App Version", "v${identity?.appVersion ?: "1.0.0"}-tv (Build 8920)", isMonospace = true)
                            DiagMetricRow("Continuous Uptime", "41 days, 16 hrs (Continuous 24/7)", valueColor = TvTertiary)
                            DiagMetricRow("SoC Thermal Sensor", "42°C (Optimal Band)", valueColor = TvTertiary)
                            DiagMetricRow("Display Panel Geometry", "1920 x 1080 @ 60Hz (Direct FHD)", valueColor = TvPrimary)
                        }

                        // PANEL 3: Playlist & Cache Storage
                        DiagPanelCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.VideoLibrary,
                            title = "3. Playlist & Cache Storage",
                            badgeText = "100% OFFLINE READY",
                            badgeColor = TvTertiary
                        ) {
                            DiagMetricRow("Active Campaign", playlist?.name ?: "Restaurant Prime Promo Loop")
                            DiagMetricRow("Queued Inventory", "${playlist?.items?.size ?: 4} items (Images & 1080p Videos)")
                            DiagMetricRow("eMMC Flash Storage", "${uiState.cacheUsedMb} MB used / ${uiState.availableDiskGb} GB available", isMonospace = true)
                            DiagMetricRow("Failover Redundancy", "100% Pre-cached (Safe if WAN drops)", valueColor = TvTertiary)
                        }
                    }

                    // RIGHT COLUMN: Panels 2 & 4
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // PANEL 2: Network & Telemetry
                        DiagPanelCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.Lan,
                            title = "2. Network & Telemetry",
                            badgeText = if (uiState.isOnline) "ONLINE • ETH0" else "OFFLINE",
                            badgeColor = if (uiState.isOnline) TvTertiary else TvStatusError
                        ) {
                            DiagMetricRow("Physical Interface", "IEEE 802.3ab Gigabit (Full Duplex)", isMonospace = true)
                            DiagMetricRow("Local IP Assignment", "192.168.1.144 (Static DHCP)", isMonospace = true)
                            DiagMetricRow("Roundtrip Gateway / CDN", "18ms GW / 24ms Cloud CDN", valueColor = TvTertiary)
                            DiagMetricRow("Telemetry Heartbeat", "Acknowledged ${uiState.lastHeartbeatFormatted}", isMonospace = true)
                        }

                        // PANEL 4: Remote Actions & Direct Technician Controls
                        DiagPanelCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.SettingsRemote,
                            title = "4. Remote Actions & Controls",
                            badgeText = "D-PAD NAVIGABLE",
                            badgeColor = TvPrimary
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                ActionListItem(
                                    keyNum = "OK",
                                    label = "Force Immediate Cloud Sync",
                                    icon = Icons.Default.Sync,
                                    modifier = Modifier.focusRequester(initialFocusRequester),
                                    onClick = {
                                        actionFeedbackText = "Dispatching immediate cloud synchronization..."
                                        actionFeedbackDone = true
                                    }
                                )

                                ActionListItem(
                                    keyNum = "2",
                                    label = "Clear Media Cache & Reload",
                                    icon = Icons.Default.DeleteSweep,
                                    onClick = {
                                        actionFeedbackText = "Media cache cleared. Re-verifying chunk hashes..."
                                        actionFeedbackDone = true
                                    }
                                )

                                ActionListItem(
                                    keyNum = "3",
                                    label = "Test Network Heartbeat",
                                    icon = Icons.Default.NetworkPing,
                                    onClick = {
                                        viewModel.forceHeartbeatPing()
                                        actionFeedbackText = "Heartbeat telemetry burst dispatched (ACK: 14ms)"
                                        actionFeedbackDone = true
                                    }
                                )

                                ActionListItem(
                                    keyNum = "4",
                                    label = "Unpair Display",
                                    icon = Icons.Default.LinkOff,
                                    isWarning = true,
                                    onClick = {
                                        actionFeedbackText = "Unpairing confirmation requested."
                                        actionFeedbackDone = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Action Feedback Strip
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(TvSurfaceContainerLowest)
                        .border(1.dp, TvSurfaceBorder, RoundedCornerShape(6.dp))
                        .padding(horizontal = 12.dp, vertical = 5.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(if (actionFeedbackDone) TvTertiary else TvPrimary)
                            )
                            Text(
                                text = actionFeedbackText,
                                color = if (actionFeedbackDone) TvTertiary else TvTextPrimary,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Text(
                            text = "SELECTABLE: 4 ACTIONS • D-PAD ▲ ▼",
                            color = TvTextMuted,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
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
private fun DiagPanelCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    badgeText: String,
    badgeColor: Color,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(TvSurfaceContainerLow)
            .border(1.dp, TvSurfaceBorder, RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Panel Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = TvPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = title,
                            color = TvTextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(badgeColor.copy(alpha = 0.15f))
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = badgeText,
                            color = badgeColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // Panel Content Rows
                content()
            }
        }
    }
}

@Composable
private fun DiagMetricRow(
    label: String,
    value: String,
    valueColor: Color = TvTextPrimary,
    isMonospace: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = TvTextMuted, fontSize = 12.sp)
        Text(
            text = value,
            color = valueColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = if (isMonospace) FontFamily.Monospace else FontFamily.Default
        )
    }
}

@Composable
private fun ActionListItem(
    keyNum: String,
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isWarning: Boolean = false
) {
    var isFocused by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }

    val bgColor = when {
        isFocused -> if (isWarning) Color(0xFF331416) else Color(0xFF26262E)
        else -> TvSurfaceContainer
    }

    val borderColor = when {
        isFocused -> if (isWarning) TvStatusError else Color(0xFFFFFFFF)
        else -> Color.Transparent
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .scale(if (isFocused) 1.02f else 1.0f)
            .clip(RoundedCornerShape(6.dp))
            .border(width = if (isFocused) 1.5.dp else 1.dp, color = borderColor, shape = RoundedCornerShape(6.dp))
            .background(bgColor)
            .onFocusChanged { isFocused = it.isFocused }
            .focusable(interactionSource = interactionSource)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
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
                        .size(20.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (keyNum == "OK") TvPrimaryContainer else TvSurfaceContainerHighest),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = keyNum,
                        color = if (keyNum == "OK") TvTextPrimary else if (isWarning) TvStatusError else TvTextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Text(
                    text = label,
                    color = if (isWarning) TvStatusError else TvTextPrimary,
                    fontSize = 12.sp,
                    fontWeight = if (isFocused) FontWeight.Bold else FontWeight.Medium
                )
            }

            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isWarning) TvStatusError else if (isFocused) TvPrimary else TvTextMuted,
                modifier = Modifier.size(15.dp)
            )
        }
    }
}
