package com.adveno.tv.feature.initialization

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Link
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
import com.adveno.tv.core.designsystem.TvTextSecondary
import com.adveno.tv.core.designsystem.components.AdvenoBackground
import com.adveno.tv.core.designsystem.components.AdvenoLogo
import com.adveno.tv.core.designsystem.components.TvButtonStyle
import com.adveno.tv.core.designsystem.components.TvFocusableButton

/**
 * Screen 01: Welcome / Start Screen (Figma Design 1)
 * Minimal branded greeting with prominent "Start Connection" action.
 */
@Composable
fun WelcomeScreen(
    onStartConnection: () -> Unit,
    modifier: Modifier = Modifier
) {
    val buttonFocusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        buttonFocusRequester.requestFocus()
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
                iconSize = 52.dp,
                fontSize = 38.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Digital Signage & Screen Management",
                color = TvTextSecondary,
                fontSize = 17.sp,
                fontWeight = FontWeight.Normal,
                letterSpacing = 0.2.sp
            )

            Spacer(modifier = Modifier.height(36.dp))

            TvFocusableButton(
                text = "Start Connection",
                onClick = onStartConnection,
                icon = Icons.Default.Link,
                style = TvButtonStyle.PRIMARY,
                height = 50.dp,
                cornerRadius = 16.dp,
                modifier = Modifier.focusRequester(buttonFocusRequester)
            )
        }
    }
}
