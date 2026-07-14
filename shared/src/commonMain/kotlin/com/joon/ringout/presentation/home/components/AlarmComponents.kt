package com.joon.ringout.presentation.home.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.joon.ringout.RingoutTheme
import com.joon.ringout.presentation.home.HomeAlarm

@Composable
fun AlarmListHeader(
    onAddAlarm: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "알람",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontSize = 30.sp,
                lineHeight = 36.sp,
                fontWeight = FontWeight.ExtraBold,
            ),
            color = PrimaryText,
        )
        Box(
            modifier = Modifier
                .shadow(8.dp, RoundedCornerShape(16.dp), ambientColor = Color(0x14000000))
                .size(46.dp)
                .background(Orange, RoundedCornerShape(16.dp))
                .clickable(onClick = onAddAlarm),
            contentAlignment = Alignment.Center,
        ) {
            PlusIcon()
        }
    }
}

@Composable
fun AlarmRow(
    alarm: HomeAlarm,
    onClick: () -> Unit,
    onEnabledChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .shadow(2.dp, RoundedCornerShape(18.dp), ambientColor = Color(0x0F000000))
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.width(92.dp)) {
            Text(
                text = alarm.time,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = 28.sp,
                    lineHeight = 30.sp,
                    fontWeight = FontWeight.Bold,
                ),
                color = PrimaryText,
            )
            Spacer(Modifier.height(4.dp))
            AlarmCaption(alarm.days)
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = alarm.destination,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = PrimaryText,
            )
            Spacer(Modifier.height(6.dp))
            AlarmCaption("${alarm.timeLimitMinutes}분 안에 완료")
        }

        AlarmToggle(
            enabled = alarm.isEnabled,
            onEnabledChange = onEnabledChange,
        )
    }
}

@Composable
private fun AlarmCaption(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium.copy(
            fontSize = 12.sp,
            lineHeight = 14.sp,
            fontWeight = FontWeight.Bold,
        ),
        color = SecondaryText,
    )
}

@Composable
private fun AlarmToggle(
    enabled: Boolean,
    onEnabledChange: (Boolean) -> Unit,
) {
    Box(
        modifier = Modifier
            .size(width = 48.dp, height = 28.dp)
            .background(
                color = if (enabled) Color(0xFFE9F7F1) else Color(0xFFE8ECE6),
                shape = CircleShape,
            )
            .clickable { onEnabledChange(!enabled) }
            .padding(4.dp),
        contentAlignment = if (enabled) Alignment.CenterEnd else Alignment.CenterStart,
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .background(if (enabled) Green else SecondaryText, CircleShape),
        )
    }
}

@Composable
private fun PlusIcon() {
    Canvas(Modifier.size(24.dp)) {
        val stroke = 2.dp.toPx()
        drawLine(Color.White, Offset(size.width / 2, size.height * 0.25f), Offset(size.width / 2, size.height * 0.75f), stroke, StrokeCap.Round)
        drawLine(Color.White, Offset(size.width * 0.25f, size.height / 2), Offset(size.width * 0.75f, size.height / 2), stroke, StrokeCap.Round)
    }
}

internal val PrimaryText = Color(0xFF161A17)
internal val SecondaryText = Color(0xFF6E756F)
internal val Orange = Color(0xFFFF6B2C)
internal val Green = Color(0xFF21A67A)

@Preview
@Composable
private fun AlarmListHeaderPreview() {
    RingoutTheme {
        Box(
            modifier = Modifier
                .background(Color(0xFFF7F8F5))
                .padding(16.dp),
        ) {
            AlarmListHeader(onAddAlarm = {})
        }
    }
}

@Preview
@Composable
private fun AlarmRowPreview() {
    RingoutTheme {
        Box(
            modifier = Modifier
                .background(Color(0xFFF7F8F5))
                .padding(16.dp),
        ) {
            AlarmRow(
                alarm = HomeAlarm(
                    id = "preview-alarm",
                    time = "06:20",
                    days = "월-금",
                    destination = "회사",
                    timeLimitMinutes = 12,
                    isEnabled = true,
                ),
                onClick = {},
                onEnabledChange = {},
            )
        }
    }
}
