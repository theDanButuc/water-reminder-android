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
fun BarChart(data: List<ChartData>) {
    val maxIntake = data.maxOfOrNull { it.value } ?: 0
    val barColor = MaterialTheme.colorScheme.primary
    val textMeasurer = rememberTextMeasurer()
    val textColor = MaterialTheme.colorScheme.onSurface

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Box(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .height(250.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val barWidth = size.width / (data.size * 2)
                val spaceBetweenBars = barWidth

                data.forEachIndexed { index, chartData ->
                    val barHeight = if (maxIntake > 0) (chartData.value / maxIntake.toFloat()) * size.height * 0.8f else 0f
                    val startX = (index * (barWidth + spaceBetweenBars)) + spaceBetweenBars / 2

                    drawRect(
                        color = barColor,
                        topLeft = Offset(x = startX, y = size.height - barHeight),
                        size = Size(width = barWidth, height = barHeight)
                    )

                    val textLayoutResult = textMeasurer.measure(
                        text = AnnotatedString(chartData.label),
                        style = TextStyle(fontSize = 12.sp, color = textColor)
                    )

                    drawText(
                        textLayoutResult = textLayoutResult,
                        topLeft = Offset(
                            x = startX + barWidth / 2 - textLayoutResult.size.width / 2,
                            y = size.height + 4.dp.toPx() - textLayoutResult.size.height
                        )
                    )
                }
            }
        }
    }
}
