package com.adveno.tv.core.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adveno.tv.core.designsystem.TvDimens
import com.adveno.tv.core.designsystem.TvPrimary
import com.adveno.tv.core.designsystem.TvTextSecondary

@Composable
fun TvLoadingIndicator(
    message: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(56.dp),
            color = TvPrimary,
            strokeWidth = 4.dp,
            strokeCap = StrokeCap.Round
        )
        Spacer(modifier = Modifier.height(TvDimens.SpacingLarge))
        Text(
            text = message,
            color = TvTextSecondary,
            fontSize = 18.sp
        )
    }
}

