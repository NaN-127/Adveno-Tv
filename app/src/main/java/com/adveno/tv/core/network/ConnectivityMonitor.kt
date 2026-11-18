package com.adveno.tv.core.network

import kotlinx.coroutines.flow.Flow

interface ConnectivityMonitor {
    val isOnline: Flow<Boolean>
    fun checkCurrentConnectivity(): Boolean
}

