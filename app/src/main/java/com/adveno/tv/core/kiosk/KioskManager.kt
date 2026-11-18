package com.adveno.tv.core.kiosk

import android.app.Activity
import android.app.ActivityManager
import android.app.admin.DevicePolicyManager
import android.content.Context
import android.os.Build

enum class KioskCapability {
    DEVICE_OWNER_LOCK_TASK, // Unattended commercial panel with Device Owner provisioned
    USER_PINNED_LOCK_TASK,  // Standard Android screen pinning supported
    UNSUPPORTED             // Consumer TV or restricted profile
}

class KioskManager(private val context: Context) {

    private val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
    private val devicePolicyManager = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager

    fun getKioskCapability(): KioskCapability {
        val dpm = devicePolicyManager
        if (dpm != null && dpm.isDeviceOwnerApp(context.packageName)) {
            return KioskCapability.DEVICE_OWNER_LOCK_TASK
        }
        return KioskCapability.USER_PINNED_LOCK_TASK
    }

    fun isLockTaskActive(): Boolean {
        val am = activityManager ?: return false
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            am.lockTaskModeState != ActivityManager.LOCK_TASK_MODE_NONE
        } else {
            @Suppress("DEPRECATION")
            am.isInLockTaskMode
        }
    }

    fun startLockTask(activity: Activity): Boolean {
        return runCatching {
            activity.startLockTask()
            true
        }.getOrDefault(false)
    }

    fun stopLockTask(activity: Activity): Boolean {
        return runCatching {
            activity.stopLockTask()
            true
        }.getOrDefault(false)
    }
}

