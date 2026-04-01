package com.example.waterreminder.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class ChartData(val label: String, val value: Int)

@OptIn(ExperimentalTextApi::class)
@Composable
fun BarChart(data: List<ChartData>, goalMl: Int = 0) {
    val maxValue = maxOf(data.maxOfOrNull { it.value } ?: 0, goalMl)
    val textMeasurer = rememberTextMeasurer()
    val textColor = MaterialTheme.colorScheme.onSurface
    val averageLineColor = MaterialTheme.colorScheme.outline
    val goalLineColor = MaterialTheme.colorScheme.primary

    // Colors for bar coding
    val colorRed = Color(0xFFEF5350)
    val colorYellow = Color(0xFFFFA726)
    val colorGreen = Color(0xFF66BB6A)
    val colorDefault = MaterialTheme.colorScheme.primary

    val average = if (data.isNotEmpty()) data.map { it.value }.average().toFloat() else 0f

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Box(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .height(260.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val chartHeight = size.height * 0.82f
                val barWidth = size.width / (data.size * 2)
                val spaceBetweenBars = barWidth

                data.forEachIndexed { index, chartData ->
                    val barHeight = if (maxValue > 0) (chartData.value / maxValue.toFloat()) * chartHeight else 0f
                    val startX = (index * (barWidth + spaceBetweenBars)) + spaceBetweenBars / 2

                    val barColor = when {
                        goalMl <= 0 -> colorDefault
                        chartData.value == 0 -> colorDefault.copy(alpha = 0.2f)
                        chartData.value.toFloat() / goalMl < 0.5f -> colorRed
                        chartData.value.toFloat() / goalMl < 0.8f -> colorYellow
                        else -> colorGreen
                    }

                    drawRect(
                        color = barColor,
                        topLeft = Offset(x = startX, y = size.height - barHeight - chartHeight * 0.18f),
                        size = Size(width = barWidth, height = barHeight)
                    )

                    val textLayoutResult = textMeasurer.measure(
                        text = AnnotatedString(chartData.label),
                        style = TextStyle(fontSize = 11.sp, color = textColor)
                    )
                    drawText(
                        textLayoutResult = textLayoutResult,
                        topLeft = Offset(
                            x = startX + barWidth / 2 - textLayoutResult.size.width / 2,
                            y = size.height - textLayoutResult.size.height
                        )
                    )
                }

                // Average line
                if (average > 0f && maxValue > 0) {
                    val avgY = size.height - (average / maxValue) * chartHeight - chartHeight * 0.18f
                    drawLine(
                        color = averageLineColor,
                        start = Offset(0f, avgY),
                        end = Offset(size.width, avgY),
                        strokeWidth = 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f))
                    )
                    val avgLabel = textMeasurer.measure(
                        text = AnnotatedString("avg ${average.toInt()}ml"),
                        style = TextStyle(fontSize = 10.sp, color = averageLineColor)
                    )
                    drawText(avgLabel, topLeft = Offset(4f, avgY - avgLabel.size.height - 2f))
                }

                // Goal line
                if (goalMl > 0 && maxValue > 0) {
                    val goalY = size.height - (goalMl.toFloat() / maxValue) * chartHeight - chartHeight * 0.18f
                    if (goalY > 0f) {
                        drawLine(
                            color = goalLineColor,
                            start = Offset(0f, goalY),
                            end = Offset(size.width, goalY),
                            strokeWidth = 2.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 8f))
                        )
                        val goalLabel = textMeasurer.measure(
                            text = AnnotatedString("goal ${goalMl}ml"),
                            style = TextStyle(fontSize = 10.sp, color = goalLineColor)
                        )
                        drawText(
                            goalLabel,
                            topLeft = Offset(
                                size.width - goalLabel.size.width - 4f,
                                goalY - goalLabel.size.height - 2f
                            )
                        )
                    }
                }
            }
        }
    }
}
