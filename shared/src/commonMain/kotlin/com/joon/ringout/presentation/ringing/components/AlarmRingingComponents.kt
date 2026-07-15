package com.joon.ringout.presentation.ringing.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.joon.ringout.RingoutTheme

@Composable
fun AlarmPulseVisual(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "alarm-pulse")
    val scale by transition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1_250),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "alarm-pulse-scale",
    )

    Box(
        modifier = modifier
            .size(250.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(
                elevation = 12.dp,
                shape = CircleShape,
                ambientColor = Color(0x0A000000),
                spotColor = Color(0x0A000000),
            )
            .background(PulseOuter, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(190.dp)
                .background(PulseMiddle, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(132.dp)
                    .background(Orange, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                AlarmIcon()
            }
        }
    }
}

@Composable
fun MissionRulePanel(
    targetDistanceKm: Double,
    limitMinutes: Int,
    modifier: Modifier = Modifier,
) {
    val distanceText = formatDistance(targetDistanceKm)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(24.dp))
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(
            text = "${limitMinutes}분 안에 ${distanceText}를 움직이면 알람이 종료됩니다. " +
                "완료하지 못하면 다시 알람이 울려요.",
            color = SecondaryText,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 14.sp,
                lineHeight = 20.sp,
            ),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            RingingStat(
                value = distanceText,
                label = "거리",
                valueColor = Orange,
                modifier = Modifier.weight(1f),
            )
            RingingStat(
                value = "${limitMinutes.toString().padStart(2, '0')}:00",
                label = "제한",
                valueColor = Red,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
fun RingingActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Orange, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 17.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "알람 끄고 미션 시작",
            color = Color.White,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
            ),
        )
    }
}

@Composable
private fun RingingStat(
    value: String,
    label: String,
    valueColor: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .background(Pale, RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = value,
            color = valueColor,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
            ),
        )
        Text(
            text = label,
            color = SecondaryText,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
            ),
        )
    }
}

@Composable
private fun AlarmIcon() {
    Canvas(Modifier.size(58.dp)) {
        val stroke = 3.dp.toPx()
        val center = Offset(size.width / 2f, size.height * 0.55f)
        val radius = size.minDimension * 0.27f
        drawCircle(Color.White, radius, center, style = Stroke(stroke))
        drawLine(
            Color.White,
            center,
            Offset(center.x, center.y - radius * 0.58f),
            stroke,
            StrokeCap.Round,
        )
        drawLine(
            Color.White,
            center,
            Offset(center.x + radius * 0.5f, center.y),
            stroke,
            StrokeCap.Round,
        )
        drawArc(
            color = Color.White,
            startAngle = 205f,
            sweepAngle = 80f,
            useCenter = false,
            topLeft = Offset(size.width * 0.17f, size.height * 0.08f),
            size = androidx.compose.ui.geometry.Size(size.width * 0.3f, size.height * 0.24f),
            style = Stroke(stroke, cap = StrokeCap.Round),
        )
        drawArc(
            color = Color.White,
            startAngle = 255f,
            sweepAngle = 80f,
            useCenter = false,
            topLeft = Offset(size.width * 0.53f, size.height * 0.08f),
            size = androidx.compose.ui.geometry.Size(size.width * 0.3f, size.height * 0.24f),
            style = Stroke(stroke, cap = StrokeCap.Round),
        )
        drawLine(
            Color.White,
            Offset(center.x - radius * 0.62f, center.y + radius * 0.78f),
            Offset(center.x - radius * 0.9f, center.y + radius * 1.08f),
            stroke,
            StrokeCap.Round,
        )
        drawLine(
            Color.White,
            Offset(center.x + radius * 0.62f, center.y + radius * 0.78f),
            Offset(center.x + radius * 0.9f, center.y + radius * 1.08f),
            stroke,
            StrokeCap.Round,
        )
    }
}

private fun formatDistance(distanceKm: Double): String =
    if (distanceKm % 1.0 == 0.0) {
        "${distanceKm.toInt()} km"
    } else {
        "${(distanceKm * 10).toInt() / 10.0} km"
    }

private val Orange = Color(0xFFFF6B2C)
private val Red = Color(0xFFE5484D)
private val SecondaryText = Color(0xFF6E756F)
private val Pale = Color(0xFFF7F8F5)
private val PulseOuter = Color(0xFFFFEDE6)
private val PulseMiddle = Color(0xFFFFD9CA)

@Preview
@Composable
private fun AlarmPulseVisualPreview() {
    RingoutTheme { PreviewSurface { AlarmPulseVisual() } }
}

@Preview
@Composable
private fun MissionRulePanelPreview() {
    RingoutTheme { PreviewSurface { MissionRulePanel(1.2, 12) } }
}

@Preview
@Composable
private fun RingingActionButtonPreview() {
    RingoutTheme { PreviewSurface { RingingActionButton({}) } }
}

@Composable
private fun PreviewSurface(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .background(Pale)
            .padding(20.dp),
    ) {
        content()
    }
}
