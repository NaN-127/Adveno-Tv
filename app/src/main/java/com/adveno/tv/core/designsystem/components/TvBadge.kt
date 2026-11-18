package com.adveno.tv.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adveno.tv.core.designsystem.TvDimens
import com.adveno.tv.core.designsystem.TvTextPrimary

@Composable
fun TvStatusBadge(
    text: String,
    indicatorColor: Color,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(TvDimens.CornerRadiusSmall)
    Box(
        modifier = modifier
            .clip(shape)
            .background(indicatorColor.copy(alpha = 0.15f), shape)
            .border(1.dp, indicatorColor.copy(alpha = 0.4f), shape)
            .padding(horizontal = TvDimens.SpacingMedium, vertical = TvDimens.SpacingSmall),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(indicatorColor)
            )
            Spacer(modifier = Modifier.width(TvDimens.SpacingSmall))
            Text(
                text = text,
                color = TvTextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

