package com.adveno.tv.core.navigation

sealed class Screen(val route: String) {
    data object Initialization : Screen("initialization")
    data object Welcome : Screen("welcome")
    data object ConnectChoice : Screen("connect_choice")
    data object Pairing : Screen("pairing")
    data object Connecting : Screen("connecting")
    data object Sync : Screen("sync")
    data object Playback : Screen("playback")
    data object DeviceStatus : Screen("device_status")
    data object Diagnostics : Screen("diagnostics")
}
