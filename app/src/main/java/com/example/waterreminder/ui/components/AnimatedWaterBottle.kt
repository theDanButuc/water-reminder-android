package com.example.waterreminder.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.sin

@Composable
fun AnimatedWaterBottle(consumed: Int, goal: Int) {
    val progress = if (goal > 0) (consumed.toFloat() / goal.toFloat()).coerceIn(0f, 1f) else 0f

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 1000, easing = LinearEasing),
        label = ""
    )

    Box(contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(200.dp, 300.dp)) {
            val bottleWidth = size.width * 0.6f
            val bottleHeight = size.height * 0.9f
            val cornerRadius = CornerRadius(x = 30f, y = 30f)

            val bottlePath = Path().apply {
                moveTo(center.x - bottleWidth / 2, center.y + bottleHeight / 2)
                lineTo(center.x - bottleWidth / 2, center.y - bottleHeight / 2 + cornerRadius.y)
                arcTo(
                    rect = Rect(
                        Offset(center.x - bottleWidth / 2, center.y - bottleHeight / 2),
                        Size(cornerRadius.x, cornerRadius.y)
                    ),
                    startAngleDegrees = 180f,
                    sweepAngleDegrees = -90f,
                    forceMoveTo = false
                )
                lineTo(center.x + bottleWidth / 2 - cornerRadius.x, center.y - bottleHeight / 2)
                arcTo(
                    rect = Rect(
                        Offset(center.x + bottleWidth / 2 - cornerRadius.x, center.y - bottleHeight / 2),
                        Size(cornerRadius.x, cornerRadius.y)
                    ),
                    startAngleDegrees = 90f,
                    sweepAngleDegrees = -90f,
                    forceMoveTo = false
                )
                lineTo(center.x + bottleWidth / 2, center.y + bottleHeight / 2)
                close()
            }

            clipPath(bottlePath) {
                val waterHeight = bottleHeight * animatedProgress
                val waveHeight = 10f
                val waveLength = 150f

                val waterPath = Path().apply {
                    moveTo(center.x - bottleWidth, center.y + bottleHeight / 2)
                    lineTo(center.x - bottleWidth, center.y + bottleHeight / 2 - waterHeight)

                    if (waterHeight > 0) {
                        for (i in 0..size.width.toInt() step 2) {
                            val x = i.toFloat()
                            val y = (sin((x + animatedProgress * size.width) * 2 * Math.PI / waveLength) * waveHeight).toFloat() +
                                    (center.y + bottleHeight / 2 - waterHeight)
                            lineTo(x, y)
                        }
                    }
                    lineTo(center.x + bottleWidth, center.y + bottleHeight / 2)
                    close()
                }

                drawPath(waterPath, color = Color(0xFF89CFF0))
            }

            drawPath(bottlePath, color = Color.Gray, style = Stroke(width = 5f))
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$consumed ml / $goal ml",
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${(progress * 100).toInt()}%",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}
