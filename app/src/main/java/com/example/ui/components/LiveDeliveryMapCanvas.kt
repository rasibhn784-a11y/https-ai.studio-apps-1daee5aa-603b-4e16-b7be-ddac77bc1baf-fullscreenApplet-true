package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.UrgentEmergencyRed
import com.example.ui.theme.UrgentTimerNeon

@Composable
fun LiveDeliveryMapCanvas(
    progressPercent: Float, // 0.0 to 1.0 (0.0 = just placed, 1.0 = delivered)
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 10f,
        targetValue = 28f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseRadius"
    )

    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseAlpha"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF0F1B1A))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Background grid lines (subtle map streets)
            val gridColor = Color(0xFF1E3230)
            val streetStroke = 2f

            drawLine(gridColor, Offset(0f, height * 0.3f), Offset(width, height * 0.3f), streetStroke)
            drawLine(gridColor, Offset(0f, height * 0.7f), Offset(width, height * 0.7f), streetStroke)
            drawLine(gridColor, Offset(width * 0.25f, 0f), Offset(width * 0.25f, height), streetStroke)
            drawLine(gridColor, Offset(width * 0.75f, 0f), Offset(width * 0.75f, height), streetStroke)

            // Start Point: TS Express Pharmacy Hub
            val startX = width * 0.15f
            val startY = height * 0.75f

            // Mid Waypoint
            val midX = width * 0.5f
            val midY = height * 0.35f

            // Destination Point: Customer Doorstep
            val endX = width * 0.85f
            val endY = height * 0.25f

            // Draw route path
            val routePath = Path().apply {
                moveTo(startX, startY)
                cubicTo(
                    width * 0.3f, height * 0.75f,
                    width * 0.4f, height * 0.35f,
                    midX, midY
                )
                cubicTo(
                    width * 0.65f, height * 0.35f,
                    width * 0.7f, height * 0.25f,
                    endX, endY
                )
            }

            // Inactive route line
            drawPath(
                path = routePath,
                color = Color(0xFF264642),
                style = Stroke(
                    width = 6.dp.toPx(),
                    cap = StrokeCap.Round,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 12f), 0f)
                )
            )

            // Dynamic Rider Position calculation based on progress
            val clamped = progressPercent.coerceIn(0f, 1f)
            val riderX = if (clamped < 0.5f) {
                val t = clamped / 0.5f
                startX + (midX - startX) * t
            } else {
                val t = (clamped - 0.5f) / 0.5f
                midX + (endX - midX) * t
            }

            val riderY = if (clamped < 0.5f) {
                val t = clamped / 0.5f
                startY + (midY - startY) * t
            } else {
                val t = (clamped - 0.5f) / 0.5f
                midY + (endY - midY) * t
            }

            // Completed route line (glowing cyan)
            val completedPath = Path().apply {
                moveTo(startX, startY)
                if (clamped <= 0.5f) {
                    val t = clamped / 0.5f
                    lineTo(startX + (midX - startX) * t, startY + (midY - startY) * t)
                } else {
                    lineTo(midX, midY)
                    val t = (clamped - 0.5f) / 0.5f
                    lineTo(midX + (endX - midX) * t, midY + (endY - midY) * t)
                }
            }
            drawPath(
                path = completedPath,
                color = UrgentTimerNeon,
                style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
            )

            // Pharmacy Hub Circle (Teal)
            drawCircle(
                color = Color(0xFF00796B),
                radius = 14.dp.toPx(),
                center = Offset(startX, startY)
            )
            drawCircle(
                color = Color.White,
                radius = 5.dp.toPx(),
                center = Offset(startX, startY)
            )

            // Destination Marker (Emergency Coral)
            drawCircle(
                color = UrgentEmergencyRed.copy(alpha = 0.3f),
                radius = 18.dp.toPx(),
                center = Offset(endX, endY)
            )
            drawCircle(
                color = UrgentEmergencyRed,
                radius = 12.dp.toPx(),
                center = Offset(endX, endY)
            )
            drawCircle(
                color = Color.White,
                radius = 4.dp.toPx(),
                center = Offset(endX, endY)
            )

            // Rider Live Marker with pulsing ripple
            drawCircle(
                color = UrgentTimerNeon.copy(alpha = pulseAlpha),
                radius = pulseRadius.dp.toPx(),
                center = Offset(riderX, riderY)
            )
            drawCircle(
                color = Color(0xFF004D40),
                radius = 14.dp.toPx(),
                center = Offset(riderX, riderY)
            )
            drawCircle(
                color = UrgentTimerNeon,
                radius = 9.dp.toPx(),
                center = Offset(riderX, riderY)
            )
            drawCircle(
                color = Color.White,
                radius = 4.dp.toPx(),
                center = Offset(riderX, riderY)
            )
        }

        // Overlay status badges
        Surface(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp),
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFF004D40).copy(alpha = 0.85f)
        ) {
            Text(
                text = "TS Pharmacy Hub (Dispatched)",
                color = Color(0xFFE0F2F1),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }

        Surface(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(12.dp),
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFF1B2D2B).copy(alpha = 0.9f)
        ) {
            Text(
                text = "Fast Delivery Priority - Doorstep",
                color = UrgentTimerNeon,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}
