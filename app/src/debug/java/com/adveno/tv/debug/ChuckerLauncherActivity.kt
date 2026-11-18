package com.adveno.tv.debug

import android.app.Activity
import android.os.Bundle
import com.chuckerteam.chucker.api.Chucker

class ChuckerLauncherActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = Chucker.getLaunchIntent(this)
        if (intent != null) {
            startActivity(intent)
        }
        finish()
    }
}
