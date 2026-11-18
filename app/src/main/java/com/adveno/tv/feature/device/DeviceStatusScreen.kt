package com.adveno.tv.feature.device

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adveno.tv.core.designsystem.TvAccentCyan
import com.adveno.tv.core.designsystem.TvBackgroundDark
import com.adveno.tv.core.designsystem.TvDimens
import com.adveno.tv.core.designsystem.TvPrimary
import com.adveno.tv.core.designsystem.TvStatusError
import com.adveno.tv.core.designsystem.TvStatusOffline
import com.adveno.tv.core.designsystem.TvStatusSuccess
import com.adveno.tv.core.designsystem.TvTextMuted
import com.adveno.tv.core.designsystem.TvTextPrimary
import com.adveno.tv.core.designsystem.components.TvButtonStyle
import com.adveno.tv.core.designsystem.components.TvCard
import com.adveno.tv.core.designsystem.components.TvFocusableButton
import com.adveno.tv.core.designsystem.components.TvLoadingIndicator
import com.adveno.tv.core.designsystem.components.TvStatusBadge
import org.koin.androidx.compose.koinViewModel

@Composable
fun DeviceStatusScreen(
    onBack: () -> Unit,
    onDeviceUnpaired: () -> Unit,
    viewModel: DeviceStatusViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isUnpaired) {
        if (uiState.isUnpaired) {
            onDeviceUnpaired()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TvBackgroundDark)
            .padding(horizontal = TvDimens.OverscanHorizontal, vertical = TvDimens.OverscanVertical)
    ) {
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                TvLoadingIndicator(message = "Reading device diagnostics...")
            }
        } else {
            val identity = uiState.deviceIdentity

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Display Information & Diagnostics",
                            color = TvTextPrimary,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(TvDimens.SpacingSmall))
                        Text(
                            text = "Adveno TV Device Management Agent",
                            color = TvTextMuted,
                            fontSize = 16.sp
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(TvDimens.SpacingMedium)) {
                        TvStatusBadge(
                            text = if (uiState.isOnline) "ONLINE" else "OFFLINE",
                            indicatorColor = if (uiState.isOnline) TvStatusSuccess else TvStatusOffline
                        )

                        TvStatusBadge(
                            text = if (identity?.isPaired == true) "PAIRED / ACTIVE" else "UNREGISTERED",
                            indicatorColor = if (identity?.isPaired == true) TvStatusSuccess else TvStatusError
                        )
                    }
                }

                // Grid Details Cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(TvDimens.SpacingLarge)
                ) {
                    // Card 1: Identity & Cloud Account
                    TvCard(modifier = Modifier.weight(1f)) {
                        Column(verticalArrangement = Arrangement.spacedBy(TvDimens.SpacingSmall)) {
                            Text(
                                text = "Account & Display",
                                color = TvAccentCyan,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            InfoRow(label = "Screen ID", value = identity?.screenId ?: "--", isMonospace = true)
                            InfoRow(label = "Screen Name", value = identity?.registeredScreenName ?: "Unassigned")
                            InfoRow(label = "Company", value = identity?.companyName ?: "Unassigned")
                        }
                    }

                    // Card 2: Hardware & Runtime
                    TvCard(modifier = Modifier.weight(1f)) {
                        Column(verticalArrangement = Arrangement.spacedBy(TvDimens.SpacingSmall)) {
                            Text(
                                text = "Hardware & Runtime",
                                color = TvAccentCyan,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            InfoRow(label = "Model", value = identity?.deviceName ?: "--")
                            InfoRow(label = "OS", value = identity?.androidVersion ?: "--")
                            InfoRow(label = "App Version", value = "v${identity?.appVersion ?: "1.0.0"}")
                        }
                    }

                    // Card 3: Storage & Health
                    TvCard(modifier = Modifier.weight(1f)) {
                        Column(verticalArrangement = Arrangement.spacedBy(TvDimens.SpacingSmall)) {
                            Text(
                                text = "Storage & Cache",
                                color = TvAccentCyan,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            InfoRow(label = "Total Storage", value = uiState.totalStorageGb)
                            InfoRow(label = "Available Space", value = uiState.freeStorageGb)
                            InfoRow(label = "Sync Status", value = uiState.syncStatusText)
                            InfoRow(label = "Last Sync", value = uiState.lastSyncFormatted)
                        }
                    }
                }

                // Actions Footer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(TvDimens.SpacingMedium)) {
                        TvFocusableButton(
                            text = "Back",
                            icon = Icons.AutoMirrored.Filled.ArrowBack,
                            onClick = onBack,
                            style = TvButtonStyle.OUTLINE
                        )

                        TvFocusableButton(
                            text = if (uiState.isSyncing) "Checking..." else "Check for Updates",
                            icon = Icons.Default.Refresh,
                            onClick = { viewModel.triggerSync() },
                            enabled = !uiState.isSyncing,
                            style = TvButtonStyle.SECONDARY
                        )

                        TvFocusableButton(
                            text = "Simulate Mobile App Update",
                            icon = Icons.Default.CloudDownload,
                            onClick = { viewModel.simulateFCMUpdate() },
                            style = TvButtonStyle.PRIMARY
                        )
                    }

                    if (identity?.isPaired == true) {
                        TvFocusableButton(
                            text = "Unpair Screen",
                            icon = Icons.Default.Delete,
                            onClick = { viewModel.unpairDevice() },
                            style = TvButtonStyle.SECONDARY
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    isMonospace: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = TvTextMuted,
            fontSize = 15.sp
        )
        Text(
            text = value,
            color = TvTextPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = if (isMonospace) FontFamily.Monospace else FontFamily.Default
        )
    }
}
