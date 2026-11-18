package com.adveno.tv.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.adveno.tv.feature.device.DeviceStatusScreen
import com.adveno.tv.feature.diagnostics.DiagnosticsScreen
import com.adveno.tv.feature.initialization.InitializationScreen
import com.adveno.tv.feature.initialization.WelcomeScreen
import com.adveno.tv.feature.pairing.ConnectChoiceScreen
import com.adveno.tv.feature.pairing.ConnectingScreen
import com.adveno.tv.feature.pairing.PairingScreen
import com.adveno.tv.feature.playback.PlaybackScreen
import com.adveno.tv.feature.sync.SyncingAdsScreen

@Composable
fun AdvenoNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Initialization.route
) {
    androidx.compose.runtime.LaunchedEffect(startDestination) {
        if (startDestination.isNotEmpty() && startDestination != Screen.Initialization.route) {
            navController.navigate(startDestination) {
                launchSingleTop = true
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Screen.Initialization.route) {
            InitializationScreen(
                onNavigateToPairing = {
                    navController.navigate(Screen.Welcome.route) {
                        popUpTo(Screen.Initialization.route) { inclusive = true }
                    }
                },
                onNavigateToPlayback = {
                    navController.navigate(Screen.Playback.route) {
                        popUpTo(Screen.Initialization.route) { inclusive = true }
                    }
                }
            )
        }

        // Screen 01: Welcome / Start Connection
        composable(Screen.Welcome.route) {
            WelcomeScreen(
                onStartConnection = {
                    navController.navigate(Screen.ConnectChoice.route)
                }
            )
        }

        // Screen 02: Connect Your Screen (New Screen vs Reconnect)
        composable(Screen.ConnectChoice.route) {
            ConnectChoiceScreen(
                onNewScreen = {
                    navController.navigate(Screen.Pairing.route)
                },
                onReconnect = {
                    navController.navigate(Screen.Connecting.route)
                }
            )
        }

        // Screen 03: Scan to Connect (QR Code)
        composable(Screen.Pairing.route) {
            PairingScreen(
                onPairingSuccess = {
                    navController.navigate(Screen.Connecting.route) {
                        popUpTo(Screen.Pairing.route) { inclusive = true }
                    }
                }
            )
        }

        // Screen 04: Connecting... (Linking state)
        composable(Screen.Connecting.route) {
            ConnectingScreen(
                autoTransitionDelayMs = 25000L,
                onConnected = {
                    navController.navigate(Screen.Sync.route) {
                        popUpTo(Screen.Connecting.route) { inclusive = true }
                    }
                }
            )
        }

        // Screen 05: Syncing Ads... (Progress bar & asset downloading)
        composable(Screen.Sync.route) {
            SyncingAdsScreen(
                initialProgress = 0.70f,
                autoAdvance = false,
                onSyncComplete = {
                    navController.navigate(Screen.Playback.route) {
                        popUpTo(Screen.Sync.route) { inclusive = true }
                    }
                }
            )
        }

        // Screen 06: Fullscreen Playback (Ad canvas with watermark & Logout action)
        composable(Screen.Playback.route) {
            PlaybackScreen(
                onLogout = {
                    navController.navigate(Screen.Welcome.route) {
                        popUpTo(Screen.Playback.route) { inclusive = true }
                    }
                },
                onNavigateToDiagnostics = {
                    navController.navigate(Screen.Diagnostics.route)
                }
            )
        }

        composable(Screen.DeviceStatus.route) {
            DeviceStatusScreen(
                onBack = {
                    navController.popBackStack()
                },
                onDeviceUnpaired = {
                    navController.navigate(Screen.Welcome.route) {
                        popUpTo(Screen.Playback.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Diagnostics.route) {
            DiagnosticsScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
