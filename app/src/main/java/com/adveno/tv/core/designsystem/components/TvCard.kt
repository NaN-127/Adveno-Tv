package com.adveno.tv.core.designsystem.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.adveno.tv.core.designsystem.TvDimens
import com.adveno.tv.core.designsystem.TvFocusContainer
import com.adveno.tv.core.designsystem.TvFocusRing
import com.adveno.tv.core.designsystem.TvSurfaceBorder
import com.adveno.tv.core.designsystem.TvSurfaceDark
import com.adveno.tv.core.designsystem.TvSurfaceElevated

@Composable
fun TvCard(
    modifier: Modifier = Modifier,
    isFocusable: Boolean = false,
    onClick: (() -> Unit)? = null,
    backgroundColor: Color = TvSurfaceDark,
    content: @Composable BoxScope.() -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }

    val scale by animateFloatAsState(
        targetValue = if (isFocused && isFocusable) TvDimens.FocusScaleFactor else 1.0f,
        animationSpec = tween(150),
        label = "tv_card_scale"
    )

    val currentBorderColor by animateColorAsState(
        targetValue = when {
            isFocused && isFocusable -> TvFocusRing
            else -> TvSurfaceBorder
        },
        animationSpec = tween(150),
        label = "tv_card_border"
    )

    val currentBgColor by animateColorAsState(
        targetValue = when {
            isFocused && isFocusable -> TvFocusContainer
            else -> backgroundColor
        },
        animationSpec = tween(150),
        label = "tv_card_bg"
    )

    val shape = RoundedCornerShape(12.dp)

    Box(
        modifier = modifier
            .scale(scale)
            .clip(shape)
            .border(
                width = if (isFocused && isFocusable) 1.5.dp else 1.dp,
                color = currentBorderColor,
                shape = shape
            )
            .background(currentBgColor, shape)
            .then(
                if (isFocusable) {
                    Modifier
                        .onFocusChanged { state -> isFocused = state.isFocused }
                        .focusable(interactionSource = interactionSource)
                        .then(
                            if (onClick != null) {
                                Modifier.clickable(
                                    interactionSource = interactionSource,
                                    indication = null,
                                    onClick = onClick
                                )
                            } else Modifier
                        )
                } else Modifier
            )
            .padding(16.dp),
        content = content
    )
}

