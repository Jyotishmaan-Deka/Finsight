package com.example.finsight.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.finsight.presentation.theme.ChartColors
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

data class ChartEntry(val label: String, val value: Float, val color: Color)

@Composable
fun DonutChart(
    entries: List<ChartEntry>,
    modifier: Modifier = Modifier,
    centerLabel: String = "",
    centerSubLabel: String = ""
) {
    val total = entries.sumOf { it.value.toDouble() }.toFloat()
    if (total == 0f) return

    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(entries) {
        animProgress.snapTo(0f)
        animProgress.animateTo(1f, animationSpec = tween(900, easing = EaseOutCubic))
    }

    val prog by animProgress.asState()

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = size.minDimension * 0.13f
            val radius = (size.minDimension - strokeWidth) / 2f
            val center = Offset(size.width / 2f, size.height / 2f)
            val topLeft = Offset(center.x - radius, center.y - radius)
            val arcSize = Size(radius * 2, radius * 2)

            var startAngle = -90f

            entries.forEach { entry ->
                val sweep = (entry.value / total) * 360f * prog
                // Gap between segments
                val gap = if (entries.size > 1) 2f else 0f
                drawArc(
                    color = entry.color,
                    startAngle = startAngle + gap / 2,
                    sweepAngle = sweep - gap,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
                startAngle += sweep
            }

            // Inner shadow effect
            drawCircle(
                color = Color.Black.copy(alpha = 0.04f),
                radius = radius - strokeWidth / 2 - 4f,
                center = center
            )
        }

        if (centerLabel.isNotEmpty()) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = centerLabel,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (centerSubLabel.isNotEmpty()) {
                    Text(
                        text = centerSubLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun HorizontalBarChart(
    entries: List<ChartEntry>,
    modifier: Modifier = Modifier,
    maxValue: Float? = null
) {
    val max = maxValue ?: entries.maxOfOrNull { it.value } ?: 1f
    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(entries) {
        animProgress.snapTo(0f)
        animProgress.animateTo(1f, animationSpec = tween(700, easing = EaseOutCubic))
    }
    val prog by animProgress.asState()

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        entries.take(6).forEach { entry ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = entry.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(80.dp),
                    maxLines = 1
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(10.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val barWidth = size.width * (entry.value / max) * prog
                        drawRoundRect(
                            color = entry.color.copy(alpha = 0.18f),
                            size = size,
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(size.height / 2)
                        )
                        if (barWidth > 0f) {
                            drawRoundRect(
                                color = entry.color,
                                size = Size(barWidth, size.height),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(size.height / 2)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "₹${String.format("%.0f", entry.value)}",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = entry.color
                )
            }
        }
    }
}

@Composable
fun LineChart(
    dataPoints: List<Float>,
    labels: List<String>,
    modifier: Modifier = Modifier,
    lineColor: Color = Color(0xFF6C63FF),
    fillColor: Color = Color(0xFF6C63FF)
) {
    if (dataPoints.isEmpty()) return
    val max = dataPoints.maxOrNull() ?: 1f
    val min = 0f

    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(dataPoints) {
        animProgress.snapTo(0f)
        animProgress.animateTo(1f, animationSpec = tween(1000, easing = EaseOutCubic))
    }
    val prog by animProgress.asState()

    val onSurface = MaterialTheme.colorScheme.onSurfaceVariant

    Canvas(modifier = modifier.fillMaxWidth()) {
        val w = size.width
        val h = size.height
        val padTop = 16.dp.toPx()
        val padBottom = 0f
        val chartH = h - padTop - padBottom
        val n = dataPoints.size

        if (n < 2) return@Canvas

        val stepX = w / (n - 1).toFloat()
        fun xAt(i: Int) = i * stepX
        fun yAt(v: Float): Float {
            val range = if (max > min) max - min else 1f
            return padTop + chartH * (1f - ((v - min) / range))
        }

        // Build path up to animated progress
        val visibleCount = (n * prog).coerceIn(1f, n.toFloat()).toInt()
        val path = Path()
        val fillPath = Path()

        path.moveTo(xAt(0), yAt(dataPoints[0]))
        fillPath.moveTo(xAt(0), h)
        fillPath.lineTo(xAt(0), yAt(dataPoints[0]))

        for (i in 1 until visibleCount) {
            val cpx = (xAt(i - 1) + xAt(i)) / 2f
            path.cubicTo(cpx, yAt(dataPoints[i - 1]), cpx, yAt(dataPoints[i]), xAt(i), yAt(dataPoints[i]))
            fillPath.cubicTo(cpx, yAt(dataPoints[i - 1]), cpx, yAt(dataPoints[i]), xAt(i), yAt(dataPoints[i]))
        }
        fillPath.lineTo(xAt(visibleCount - 1), h)
        fillPath.close()

        // Gradient fill
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(fillColor.copy(alpha = 0.35f), fillColor.copy(alpha = 0f)),
                startY = padTop,
                endY = h
            )
        )

        // Line
        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // Dots at each visible point
        for (i in 0 until visibleCount) {
            drawCircle(
                color = lineColor,
                radius = 4.dp.toPx(),
                center = Offset(xAt(i), yAt(dataPoints[i]))
            )
            drawCircle(
                color = Color.White,
                radius = 2.dp.toPx(),
                center = Offset(xAt(i), yAt(dataPoints[i]))
            )
        }
    }
}

@Composable
fun WeekComparisonBar(
    thisWeek: Float,
    lastWeek: Float,
    modifier: Modifier = Modifier
) {
    val max = maxOf(thisWeek, lastWeek, 1f)
    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(thisWeek, lastWeek) {
        animProgress.snapTo(0f)
        animProgress.animateTo(1f, animationSpec = tween(700))
    }
    val prog by animProgress.asState()

    val primaryColor = Color(0xFF6C63FF)
    val secondaryColor = Color(0xFF9D94FF)

    Row(
        modifier = modifier.fillMaxWidth().height(80.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        // Last week bar
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f)
        ) {
            Canvas(modifier = Modifier.fillMaxWidth().weight(1f)) {
                val barH = size.height * (lastWeek / max) * prog
                drawRoundRect(
                    color = secondaryColor.copy(alpha = 0.3f),
                    size = size,
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx())
                )
                if (barH > 0f) {
                    drawRoundRect(
                        color = secondaryColor,
                        topLeft = Offset(0f, size.height - barH),
                        size = Size(size.width, barH),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx())
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text("Last", style = MaterialTheme.typography.labelSmall, color = secondaryColor)
        }

        // This week bar
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f)
        ) {
            Canvas(modifier = Modifier.fillMaxWidth().weight(1f)) {
                val barH = size.height * (thisWeek / max) * prog
                drawRoundRect(
                    color = primaryColor.copy(alpha = 0.2f),
                    size = size,
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx())
                )
                if (barH > 0f) {
                    drawRoundRect(
                        color = primaryColor,
                        topLeft = Offset(0f, size.height - barH),
                        size = Size(size.width, barH),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx())
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text("This", style = MaterialTheme.typography.labelSmall, color = primaryColor)
        }
    }
}
