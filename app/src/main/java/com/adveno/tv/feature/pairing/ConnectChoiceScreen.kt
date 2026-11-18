package com.adveno.tv.feature.pairing

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adveno.tv.core.designsystem.TvTextPrimary
import com.adveno.tv.core.designsystem.TvTextSecondary
import com.adveno.tv.core.designsystem.components.AdvenoBackground
import com.adveno.tv.core.designsystem.components.AdvenoLogo
import com.adveno.tv.core.designsystem.components.TvButtonStyle
import com.adveno.tv.core.designsystem.components.TvFocusableButton

/**
 * Screen 02: Connection Choice (Figma Design 2)
 * Allows the user to select between setting up a new screen or reconnecting an existing one.
 */
@Composable
fun ConnectChoiceScreen(
    onNewScreen: () -> Unit,
    onReconnect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val newScreenFocusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        newScreenFocusRequester.requestFocus()
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
                text = "Connect Your Screen",
                color = TvTextPrimary,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Set up a new screen or reconnect an existing one.",
                color = TvTextSecondary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Normal
            )

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TvFocusableButton(
                    text = "New Screen",
                    onClick = onNewScreen,
                    icon = Icons.Default.Add,
                    style = TvButtonStyle.PRIMARY,
                    height = 50.dp,
                    cornerRadius = 14.dp,
                    modifier = Modifier.focusRequester(newScreenFocusRequester)
                )

                TvFocusableButton(
                    text = "Reconnect",
                    onClick = onReconnect,
                    icon = Icons.Default.Refresh,
                    style = TvButtonStyle.SECONDARY,
                    height = 50.dp,
                    cornerRadius = 14.dp
                )
            }
        }
    }
}
