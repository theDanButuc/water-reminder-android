package com.example.waterreminder.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HydrationProgressRing(consumed: Int, goal: Int) {
    val fraction = if (goal > 0) (consumed.toFloat() / goal.toFloat()).coerceIn(0f, 1f) else 0f
    val percentage = (fraction * 100).toInt()

    val animatedFraction by animateFloatAsState(
        targetValue = fraction,
        animationSpec = tween(durationMillis = 800),
        label = "progress"
    )

    val ringColor = when {
        fraction >= 1f -> MaterialTheme.colorScheme.tertiary
        fraction >= 0.5f -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.primary
    }

    Box(contentAlignment = Alignment.Center) {
        // Track (background ring)
        CircularProgressIndicator(
            progress = { 1f },
            modifier = Modifier.size(220.dp),
            strokeWidth = 16.dp,
            color = MaterialTheme.colorScheme.surfaceVariant,
            strokeCap = StrokeCap.Round
        )
        // Progress ring
        CircularProgressIndicator(
            progress = { animatedFraction },
            modifier = Modifier.size(220.dp),
            strokeWidth = 16.dp,
            color = ringColor,
            strokeCap = StrokeCap.Round
        )
        // Center text
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${consumed}ml",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "/ ${goal}ml",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "$percentage%",
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                color = ringColor
            )
        }
    }
}
