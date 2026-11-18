package com.adveno.tv.domain.model

data class DeviceIdentity(
    val screenId: String,
    val deviceName: String,
    val manufacturer: String,
    val model: String,
    val androidVersion: String,
    val apiLevel: Int,
    val appVersion: String,
    val isPaired: Boolean,
    val registeredScreenName: String? = null,
    val companyName: String? = null
)

