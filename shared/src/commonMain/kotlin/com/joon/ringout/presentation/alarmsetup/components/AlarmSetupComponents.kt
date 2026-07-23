package com.joon.ringout.presentation.alarmsetup.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.joon.ringout.RingoutTheme
import com.joon.ringout.presentation.toTwelveHourDisplay
import kotlin.math.roundToInt

private val PrimaryText = Color(0xFF161A17)
private val SecondaryText = Color(0xFF6E756F)
private val Orange = Color(0xFFFF6B2C)
private val Blue = Color(0xFF2F6FED)
private val Pale = Color(0xFFF7F8F5)
private val Off = Color(0xFFE8ECE6)
private val WeekdayOrder = listOf("월", "화", "수", "목", "금", "토", "일")

@Composable
fun TimePickerCard(
    time: String,
    days: List<String>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val displayTime = time.toTwelveHourDisplay()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(24.dp))
            .clickable(onClick = onClick)
            .padding(22.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("알람 시각", style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = displayTime.period,
                color = SecondaryText,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                ),
            )
            Text(
                text = displayTime.time,
                color = PrimaryText,
                style = MaterialTheme.typography.displayMedium.copy(
                    fontSize = 56.sp,
                    lineHeight = 64.sp,
                    fontWeight = FontWeight.Bold,
                ),
            )
        }
        Text(
            weekdaySummary(days),
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
        )
    }
}

@Composable
fun DestinationCard(
    destination: String,
    address: String,
    distance: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            FeatureIcon(Orange, IconType.Pin)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Label("목표 지점")
                Text(destination, color = PrimaryText, style = MaterialTheme.typography.titleLarge.copy(fontSize = 21.sp, fontWeight = FontWeight.Bold))
            }
            ChevronRight()
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Pale, RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FeatureGlyph(SecondaryText, IconType.Pin, Modifier.size(16.dp))
            Text(address, modifier = Modifier.weight(1f), color = PrimaryText, style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp, fontWeight = FontWeight.Medium))
        }
        Text(distance, color = SecondaryText, style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold))
    }
}

@Composable
fun LimitTimeCard(minutes: Int, onMinutesChange: (Int) -> Unit, modifier: Modifier = Modifier) {
    val sliderMinutes = minutes.coerceIn(1, 30)
    val sliderColors = SliderDefaults.colors(
        thumbColor = Blue,
        activeTrackColor = Blue,
        inactiveTrackColor = Off,
        activeTickColor = Color.Transparent,
        inactiveTickColor = Color.Transparent,
    )
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(18.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            FeatureIcon(Blue, IconType.Timer)
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Label("제한 시간")
                Text("${sliderMinutes}분", color = PrimaryText, style = MaterialTheme.typography.titleLarge.copy(fontSize = 22.sp, fontWeight = FontWeight.Bold))
            }
        }
        Slider(
            value = sliderMinutes.toFloat(),
            onValueChange = { value -> onMinutesChange(value.roundToInt().coerceIn(1, 30)) },
            modifier = Modifier.fillMaxWidth(),
            valueRange = 1f..30f,
            steps = 28,
            colors = sliderColors,
            track = { sliderState ->
                SliderDefaults.Track(
                    sliderState = sliderState,
                    colors = sliderColors,
                    drawStopIndicator = null,
                )
            },
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Caption("1분")
            Caption("30분")
        }
    }
}

@Composable
fun AlarmSoundCard(
    soundName: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        FeatureIcon(Orange, IconType.Sound)
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Label("알람음")
            Text(
                text = soundName,
                color = PrimaryText,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                ),
            )
            Text(
                text = "눌러서 알람음을 변경하세요",
                color = SecondaryText,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
            )
        }
        ChevronRight()
    }
}

@Composable
fun RepeatScheduleCard(
    repeatEnabled: Boolean,
    selectedDays: List<String>,
    onRepeatEnabledChange: (Boolean) -> Unit,
    onDayClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(18.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Label("반복")
                Text("선택한 요일마다 반복", color = PrimaryText, style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp, fontWeight = FontWeight.ExtraBold))
            }
            Toggle(repeatEnabled) { onRepeatEnabledChange(!repeatEnabled) }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Label("요일")
            Text(weekdaySummary(selectedDays), color = Orange, style = MaterialTheme.typography.labelMedium.copy(fontSize = 13.sp, fontWeight = FontWeight.ExtraBold))
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            WeekdayOrder.forEach { day ->
                val selected = day in selectedDays
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .background(if (selected) Orange else Off, RoundedCornerShape(14.dp))
                        .clickable { onDayClick(day) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(day, color = if (selected) Color.White else SecondaryText, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.ExtraBold))
                }
            }
        }
    }
}

@Composable
fun SaveAlarmButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(Orange, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CheckIcon()
        Text("알람 저장하기", color = Color.White, style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold))
    }
}

@Composable
private fun FeatureIcon(color: Color, type: IconType) {
    Box(Modifier.size(38.dp).background(Pale, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
        FeatureGlyph(color, type, Modifier.size(20.dp))
    }
}

@Composable
private fun FeatureGlyph(color: Color, type: IconType, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val stroke = 1.6.dp.toPx()
        when (type) {
            IconType.Pin -> {
                drawCircle(color, size.minDimension * .2f, Offset(size.width / 2, size.height * .4f), style = androidx.compose.ui.graphics.drawscope.Stroke(stroke))
                drawLine(color, Offset(size.width / 2, size.height * .6f), Offset(size.width / 2, size.height * .86f), stroke, StrokeCap.Round)
            }
            IconType.Timer -> {
                drawCircle(color, size.minDimension * .34f, Offset(size.width / 2, size.height * .55f), style = androidx.compose.ui.graphics.drawscope.Stroke(stroke))
                drawLine(color, Offset(size.width / 2, size.height * .1f), Offset(size.width / 2, size.height * .25f), stroke, StrokeCap.Round)
                drawLine(color, Offset(size.width * .38f, size.height * .1f), Offset(size.width * .62f, size.height * .1f), stroke, StrokeCap.Round)
            }
            IconType.Sound -> {
                drawLine(color, Offset(size.width * .18f, size.height * .42f), Offset(size.width * .38f, size.height * .42f), stroke, StrokeCap.Round)
                drawLine(color, Offset(size.width * .18f, size.height * .42f), Offset(size.width * .18f, size.height * .68f), stroke, StrokeCap.Round)
                drawLine(color, Offset(size.width * .18f, size.height * .68f), Offset(size.width * .38f, size.height * .68f), stroke, StrokeCap.Round)
                drawLine(color, Offset(size.width * .38f, size.height * .42f), Offset(size.width * .58f, size.height * .24f), stroke, StrokeCap.Round)
                drawLine(color, Offset(size.width * .58f, size.height * .24f), Offset(size.width * .58f, size.height * .86f), stroke, StrokeCap.Round)
                drawLine(color, Offset(size.width * .58f, size.height * .86f), Offset(size.width * .38f, size.height * .68f), stroke, StrokeCap.Round)
                drawArc(color, -55f, 110f, false, Offset(size.width * .55f, size.height * .35f), androidx.compose.ui.geometry.Size(size.width * .32f, size.height * .4f), style = androidx.compose.ui.graphics.drawscope.Stroke(stroke, cap = StrokeCap.Round))
            }
        }
    }
}

@Composable
private fun ChevronRight() = Canvas(Modifier.size(22.dp)) {
    val stroke = 1.5.dp.toPx()
    drawLine(SecondaryText, Offset(size.width * .4f, size.height * .25f), Offset(size.width * .65f, size.height * .5f), stroke, StrokeCap.Round)
    drawLine(SecondaryText, Offset(size.width * .65f, size.height * .5f), Offset(size.width * .4f, size.height * .75f), stroke, StrokeCap.Round)
}

@Composable
private fun Toggle(enabled: Boolean, onClick: () -> Unit) {
    Box(
        Modifier.size(52.dp, 30.dp).background(if (enabled) Color(0xFFE9F7F1) else Off, CircleShape).clickable(onClick = onClick).padding(4.dp),
        contentAlignment = if (enabled) Alignment.CenterEnd else Alignment.CenterStart,
    ) { Box(Modifier.size(22.dp).background(if (enabled) Color(0xFF21A67A) else SecondaryText, CircleShape)) }
}

@Composable
private fun CheckIcon() = Canvas(Modifier.padding(end = 10.dp).size(22.dp)) {
    val stroke = 1.8.dp.toPx()
    drawLine(Color.White, Offset(size.width * .2f, size.height * .52f), Offset(size.width * .43f, size.height * .72f), stroke, StrokeCap.Round)
    drawLine(Color.White, Offset(size.width * .43f, size.height * .72f), Offset(size.width * .82f, size.height * .28f), stroke, StrokeCap.Round)
}

@Composable private fun Label(text: String) = Text(text, color = SecondaryText, style = MaterialTheme.typography.labelMedium.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold))
@Composable private fun Caption(text: String) = Text(text, color = SecondaryText, style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold))

internal fun weekdaySummary(days: List<String>): String = when (days.toSet()) {
    setOf("월", "화", "수", "목", "금") -> "평일"
    setOf("토", "일") -> "주말"
    WeekdayOrder.toSet() -> "매일"
    emptySet<String>() -> "선택 안 함"
    else -> WeekdayOrder.filter { it in days }.joinToString(" ")
}

private enum class IconType { Pin, Timer, Sound }

@Preview @Composable private fun TimePickerCardPreview() = PreviewSurface { TimePickerCard("06:20", WeekdayOrder, {}) }
@Preview @Composable private fun DestinationCardPreview() = PreviewSurface { DestinationCard("강남역 2번 출구", "서울 강남구 강남대로 지하 396", "현재 위치 기준 1.2 km", {}) }
@Preview @Composable private fun LimitTimeCardPreview() = PreviewSurface { LimitTimeCard(12, {}) }
@Preview @Composable private fun AlarmSoundCardPreview() = PreviewSurface { AlarmSoundCard("Morning Flower", {}) }
@Preview @Composable private fun RepeatScheduleCardPreview() = PreviewSurface { RepeatScheduleCard(true, listOf("월", "화", "수", "목", "금"), {}, {}) }
@Preview @Composable private fun SaveAlarmButtonPreview() = PreviewSurface { SaveAlarmButton({}) }

@Composable
private fun PreviewSurface(content: @Composable () -> Unit) {
    RingoutTheme {
        Box(Modifier.background(Pale).padding(16.dp)) { content() }
    }
}
