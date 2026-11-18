package com.adveno.tv.app

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.adveno.tv.core.designsystem.AdvenoTvTheme
import com.adveno.tv.core.navigation.AdvenoNavHost
import com.adveno.tv.core.navigation.Screen
import com.chuckerteam.chucker.api.Chucker

class MainActivity : ComponentActivity() {

    private val targetRoute = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Keep TV display continuously awake for signage
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Enable immersive full-screen mode (hide status and navigation bars)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        // Prevent accidental exit to TV launcher on Back press in commercial signage
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Commercial signage retains continuous display
            }
        })

        val initialRoute = intent?.getStringExtra("route") ?: Screen.Initialization.route
        targetRoute.value = initialRoute

        setContent {
            AdvenoTvTheme {
                Box(modifier = Modifier.fillMaxSize()) {
                    AdvenoNavHost(
                        startDestination = targetRoute.value ?: Screen.Initialization.route
                    )

                    // Debug inspector badge to view live network calls when clicked (only in debug builds)
                    if (com.adveno.tv.BuildConfig.DEBUG) {
                        TvChuckerDebugBadge(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(start = 28.dp, top = 24.dp),
                            onClick = { openChucker() }
                        )
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        intent.getStringExtra("route")?.let { route ->
            targetRoute.value = route
        }
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        if (com.adveno.tv.BuildConfig.DEBUG && (keyCode == KeyEvent.KEYCODE_C || keyCode == KeyEvent.KEYCODE_MENU || keyCode == KeyEvent.KEYCODE_INFO)) {
            if (openChucker()) return true
        }
        return super.onKeyUp(keyCode, event)
    }

    private fun openChucker(): Boolean {
        val intent = Chucker.getLaunchIntent(this)
        return if (intent != null) {
            startActivity(intent)
            true
        } else {
            false
        }
    }
}

@Composable
private fun TvChuckerDebugBadge(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.05f else 1.0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "chuckerScale"
    )

    Surface(
        onClick = onClick,
        modifier = modifier
            .scale(scale)
            .onFocusChanged { isFocused = it.isFocused }
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = if (isFocused) 2.dp else 1.dp,
                color = if (isFocused) Color(0xFF9D4EDD) else Color(0x558A5CF5),
                shape = RoundedCornerShape(12.dp)
            ),
        color = if (isFocused) Color(0xDD28104D) else Color(0x9917082E)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF00E676))
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "⚡ Chucker (Press 'C')",
                color = Color.White.copy(alpha = 0.92f),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
