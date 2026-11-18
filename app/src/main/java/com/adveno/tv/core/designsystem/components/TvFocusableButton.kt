package com.adveno.tv.core.designsystem.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adveno.tv.core.designsystem.TvGlassBackground
import com.adveno.tv.core.designsystem.TvGlassBorder
import com.adveno.tv.core.designsystem.TvGlassBorderFocused
import com.adveno.tv.core.designsystem.TvTextMuted
import com.adveno.tv.core.designsystem.TvTextPrimary
import com.adveno.tv.core.designsystem.TvTextSecondary

enum class TvButtonStyle {
    PRIMARY,
    SECONDARY,
    OUTLINE
}

@Composable
fun TvFocusableButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    keyHint: String? = null,
    style: TvButtonStyle = TvButtonStyle.PRIMARY,
    enabled: Boolean = true,
    height: Dp = 48.dp,
    cornerRadius: Dp = 14.dp
) {
    var isFocused by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }

    val scale by animateFloatAsState(
        targetValue = if (isFocused) 1.04f else 1.0f,
        animationSpec = tween(140),
        label = "tv_button_scale"
    )

    val shape = RoundedCornerShape(cornerRadius)

    val borderColor by animateColorAsState(
        targetValue = when {
            !enabled -> Color.Transparent
            isFocused -> when (style) {
                TvButtonStyle.PRIMARY -> Color(0xFFF3E8FF)
                TvButtonStyle.SECONDARY -> TvGlassBorderFocused
                TvButtonStyle.OUTLINE -> Color(0xFFA855F7)
            }
            else -> when (style) {
                TvButtonStyle.PRIMARY -> Color(0x40FFFFFF)
                TvButtonStyle.SECONDARY -> TvGlassBorder
                TvButtonStyle.OUTLINE -> Color(0x33FFFFFF)
            }
        },
        animationSpec = tween(140),
        label = "tv_button_border"
    )

    val contentColor by animateColorAsState(
        targetValue = when {
            !enabled -> TvTextMuted
            isFocused -> Color(0xFFFFFFFF)
            else -> when (style) {
                TvButtonStyle.PRIMARY -> Color(0xFFFFFFFF)
                TvButtonStyle.SECONDARY -> TvTextPrimary
                TvButtonStyle.OUTLINE -> TvTextSecondary
            }
        },
        animationSpec = tween(140),
        label = "tv_button_content"
    )

    val buttonModifier = if (style == TvButtonStyle.PRIMARY) {
        val gradient = if (isFocused) {
            Brush.horizontalGradient(
                listOf(Color(0xFF7C3AED), Color(0xFF8B5CF6), Color(0xFFA855F7))
            )
        } else {
            Brush.horizontalGradient(
                listOf(Color(0xFF5B21B6), Color(0xFF6D28D9), Color(0xFF7C3AED))
            )
        }
        Modifier.background(gradient, shape)
    } else {
        val glassBg = if (isFocused) Color(0x6633215C) else TvGlassBackground
        Modifier.background(glassBg, shape)
    }

    Box(
        modifier = modifier
            .scale(scale)
            .defaultMinSize(minHeight = height)
            .clip(shape)
            .then(buttonModifier)
            .border(
                width = if (isFocused) 2.dp else 1.dp,
                color = borderColor,
                shape = shape
            )
            .onFocusChanged { isFocused = it.isFocused }
            .focusable(enabled = enabled, interactionSource = interactionSource)
            .clickable(enabled = enabled, onClick = onClick)
            .pointerInput(enabled) {
                if (enabled) {
                    detectTapGestures(onTap = { onClick() })
                }
            }
            .padding(horizontal = 22.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
            }

            Text(
                text = text,
                color = contentColor,
                fontSize = 15.sp,
                fontWeight = if (isFocused) FontWeight.Bold else FontWeight.SemiBold,
                letterSpacing = 0.2.sp
            )

            if (keyHint != null && isFocused) {
                Spacer(modifier = Modifier.width(12.dp))
                Box(
                    modifier = Modifier
                        .background(Color(0x33FFFFFF), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = keyHint,
                        color = Color(0xFFFFFFFF),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
