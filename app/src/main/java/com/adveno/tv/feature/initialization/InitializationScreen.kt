package com.adveno.tv.feature.initialization

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adveno.tv.core.designsystem.TvBackgroundDark
import com.adveno.tv.core.designsystem.TvDimens
import com.adveno.tv.core.designsystem.TvPrimary
import com.adveno.tv.core.designsystem.TvStatusError
import com.adveno.tv.core.designsystem.TvTextMuted
import com.adveno.tv.core.designsystem.TvTextPrimary
import com.adveno.tv.core.designsystem.components.TvFocusableButton
import com.adveno.tv.core.designsystem.components.TvLoadingIndicator
import org.koin.androidx.compose.koinViewModel

@Composable
fun InitializationScreen(
    onNavigateToPairing: () -> Unit,
    onNavigateToPlayback: () -> Unit,
    viewModel: InitializationViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        when (uiState) {
            is InitializationUiState.NavigateToPairing -> onNavigateToPairing()
            is InitializationUiState.NavigateToPlayback -> onNavigateToPlayback()
            else -> Unit
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TvBackgroundDark)
            .padding(horizontal = TvDimens.OverscanHorizontal, vertical = TvDimens.OverscanVertical),
        contentAlignment = Alignment.Center
    ) {
        when (val state = uiState) {
            is InitializationUiState.Loading,
            is InitializationUiState.NavigateToPairing,
            is InitializationUiState.NavigateToPlayback -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Logo Box
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(TvDimens.CornerRadiusLarge))
                            .background(TvPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "A",
                            color = Color.White,
                            fontSize = 44.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    Spacer(modifier = Modifier.height(TvDimens.SpacingLarge))

                    Text(
                        text = "ADVENO TV",
                        color = TvTextPrimary,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 4.sp
                    )

                    Spacer(modifier = Modifier.height(TvDimens.SpacingSmall))

                    Text(
                        text = "Enterprise Digital Signage Player",
                        color = TvTextMuted,
                        fontSize = 16.sp
                    )

                    Spacer(modifier = Modifier.height(TvDimens.SpacingXXLarge))

                    TvLoadingIndicator(message = "Initializing secure display profile...")
                }
            }

            is InitializationUiState.Error -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Initialization Error",
                        color = TvStatusError,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(TvDimens.SpacingMedium))

                    Text(
                        text = state.message,
                        color = TvTextMuted,
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(TvDimens.SpacingXLarge))

                    TvFocusableButton(
                        text = "Retry Initialization",
                        icon = Icons.Default.Refresh,
                        onClick = { viewModel.initializeApplication() }
                    )
                }
            }
        }
    }
}

