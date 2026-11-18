package com.adveno.tv.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adveno.tv.core.designsystem.TvDimens
import com.adveno.tv.core.designsystem.TvPrimary
import com.adveno.tv.core.designsystem.TvSurfaceBorder
import com.adveno.tv.core.designsystem.TvSurfaceContainerHigh
import com.adveno.tv.core.designsystem.TvSurfaceContainerLowest
import com.adveno.tv.core.designsystem.TvTextMuted
import com.adveno.tv.core.designsystem.TvTextPrimary
import com.adveno.tv.core.designsystem.TvTextSecondary

@Composable
fun TvFooter(
    modifier: Modifier = Modifier,
    firmwareVersion: String = "v4.2.1-LTS",
    resolution: String = "1920x1080@60Hz"
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(42.dp)
            .background(TvSurfaceContainerLowest.copy(alpha = 0.95f))
            .border(width = 1.dp, color = TvSurfaceBorder, shape = RoundedCornerShape(0.dp))
            .padding(horizontal = 36.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // LEFT: D-Pad Navigation Legend Pills
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                RemoteKeyPill(key = "D-PAD", label = "NAVIGATE")
                RemoteKeyPill(key = "OK", label = "SELECT", isPrimary = true)
                RemoteKeyPill(key = "BACK", label = "(HOLD 3S) DIAGNOSTICS")
                RemoteKeyPill(key = "MENU", label = "SETTINGS")
            }

            // RIGHT: Hardware / Firmware Specification
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "FIRMWARE ",
                        color = TvTextMuted,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = firmwareVersion,
                        color = TvTextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Text(
                    text = "•",
                    color = TvSurfaceBorder,
                    fontSize = 11.sp
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "RES ",
                        color = TvTextMuted,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = resolution,
                        color = TvTextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
private fun RemoteKeyPill(
    key: String,
    label: String,
    isPrimary: Boolean = false
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(TvSurfaceContainerHigh)
                .padding(horizontal = 7.dp, vertical = 3.dp)
        ) {
            Text(
                text = key,
                color = if (isPrimary) TvPrimary else TvTextPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
        Text(
            text = label,
            color = TvTextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.5.sp
        )
    }
}

