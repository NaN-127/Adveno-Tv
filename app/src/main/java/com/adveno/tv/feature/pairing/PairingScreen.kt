package com.adveno.tv.feature.pairing

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adveno.tv.core.designsystem.TvTextPrimary
import com.adveno.tv.core.designsystem.TvTextSecondary
import com.adveno.tv.core.designsystem.components.AdvenoBackground
import com.adveno.tv.core.designsystem.components.AdvenoLogo
import com.adveno.tv.core.designsystem.components.TvLoadingIndicator
import org.koin.androidx.compose.koinViewModel

/**
 * Screen 03: Scan to Connect (Figma Design 3)
 * Pure, distraction-free QR screen designed for instant optical mobile scanning.
 */
@Composable
fun PairingScreen(
    onPairingSuccess: () -> Unit,
    modifier: Modifier = Modifier,
    onNavigate: (String) -> Unit = {},
    viewModel: PairingViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val qrFocusRequester = remember { FocusRequester() }
    var isQrFocused by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }

    LaunchedEffect(uiState) {
        if (uiState is PairingUiState.Ready) {
            qrFocusRequester.requestFocus()
        }
        if (uiState is PairingUiState.Success) {
            onPairingSuccess()
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

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Scan to Connect",
                color = TvTextPrimary,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Scan this QR code from your Adveno dashboard to link this screen.",
                color = TvTextSecondary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Normal
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Center QR Code Card with ambient purple halo
            when (val state = uiState) {
                is PairingUiState.Loading -> {
                    Box(
                        modifier = Modifier.size(190.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        TvLoadingIndicator(message = "Generating session...")
                    }
                }

                is PairingUiState.Ready -> {
                    Box(
                        modifier = Modifier
                            .scale(if (isQrFocused) 1.03f else 1.0f)
                            .size(190.dp)
                            .drawBehind {
                                // Ambient radial purple glow behind QR card
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            Color(0x66A855F7),
                                            Color(0x247C3AED),
                                            Color.Transparent
                                        ),
                                        center = Offset(size.width / 2f, size.height / 2f),
                                        radius = size.width * 0.72f
                                    )
                                )
                            }
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color.White)
                            .border(
                                width = if (isQrFocused) 2.5.dp else 1.dp,
                                color = if (isQrFocused) Color(0xFFC084FC) else Color(0x33FFFFFF),
                                shape = RoundedCornerShape(18.dp)
                            )
                            .onFocusChanged { isQrFocused = it.isFocused }
                            .focusRequester(qrFocusRequester)
                            .focusable(interactionSource = interactionSource)
                            .onKeyEvent { keyEvent ->
                                if (keyEvent.type == KeyEventType.KeyUp && 
                                    (keyEvent.key == Key.DirectionCenter || keyEvent.key == Key.Enter || keyEvent.key == Key.NumPadEnter)) {
                                    viewModel.simulateCustomerPairing()
                                    true
                                } else {
                                    false
                                }
                            }
                            .clickable {
                                viewModel.simulateCustomerPairing()
                            }
                            .pointerInput(Unit) {
                                detectTapGestures(onTap = { viewModel.simulateCustomerPairing() })
                            }
                            .padding(14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        state.qrBitmap?.let { bmp ->
                            Image(
                                bitmap = bmp.asImageBitmap(),
                                contentDescription = "Scan to Connect QR Code",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }

                is PairingUiState.Connecting -> {
                    ConnectingScreen(
                        onConnected = onPairingSuccess,
                        autoTransitionDelayMs = 0L
                    )
                }

                is PairingUiState.Success -> {
                    ConnectingScreen(
                        onConnected = onPairingSuccess,
                        autoTransitionDelayMs = 800L
                    )
                }

                is PairingUiState.Expired -> {
                    Box(
                        modifier = Modifier
                            .size(190.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0x331E1438))
                            .clickable { viewModel.refreshCode() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Code Expired\nClick to refresh",
                            color = TvTextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            fontSize = 14.sp
                        )
                    }
                }

                is PairingUiState.Error -> {
                    Box(
                        modifier = Modifier
                            .size(190.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0x337F1D1D))
                            .clickable { viewModel.refreshCode() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Connection Failed\nClick to retry",
                            color = Color(0xFFFCA5A5),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}
