package com.joon.ringout.presentation.home.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.joon.ringout.LocalRingoutThemeMode
import com.joon.ringout.RingoutTheme
import com.joon.ringout.ThemeMode
import com.joon.ringout.presentation.home.HomeAlarm
import com.joon.ringout.presentation.toTwelveHourDisplay
import org.jetbrains.compose.resources.painterResource
import ringout.shared.generated.resources.Res
import ringout.shared.generated.resources.home_add
import ringout.shared.generated.resources.home_settings_dark
import ringout.shared.generated.resources.home_settings_light
import ringout.shared.generated.resources.home_toggle_off
import ringout.shared.generated.resources.home_toggle_on_dark
import ringout.shared.generated.resources.home_toggle_on_light

@Composable
internal fun AlarmListHeader(
    nextAlarmDescription: String,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = homeAlarmColors()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(65.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 10.dp),
        ) {
            Text(
                text = "알람",
                color = colors.primaryText,
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 28.sp,
                    lineHeight = 34.sp,
                    fontWeight = FontWeight.Black,
                ),
            )
            Text(
                text = nextAlarmDescription,
                color = colors.secondaryText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 18.sp,
                    lineHeight = 21.6.sp,
                    fontWeight = FontWeight.Medium,
                ),
            )
        }

        HomeSettingsButton(onClick = onSettingsClick)
    }
}

@Composable
internal fun HomeSettingsButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isDarkTheme = LocalRingoutThemeMode.current == ThemeMode.Dark

    Box(
        modifier = modifier
            .size(48.dp)
            .clickable(
                role = Role.Button,
                onClickLabel = "설정 열기",
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(
                if (isDarkTheme) {
                    Res.drawable.home_settings_dark
                } else {
                    Res.drawable.home_settings_light
                },
            ),
            contentDescription = "설정",
            modifier = Modifier
                .offset(x = 10.65.dp)
                .size(width = 21.3.dp, height = 21.7.dp),
        )
    }
}

@Composable
internal fun AlarmRow(
    alarm: HomeAlarm,
    onClick: () -> Unit,
    onEnabledChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = homeAlarmColors()
    val displayTime = alarm.time.toTwelveHourDisplay()
    val shape = RoundedCornerShape(20.dp)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.cardBackground)
            .clickable(
                role = Role.Button,
                onClickLabel = "알람 편집",
                onClick = onClick,
            )
            .padding(horizontal = 20.dp, vertical = 10.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = alarm.days,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        lineHeight = 14.sp,
                        fontWeight = FontWeight.Medium,
                    ),
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = displayTime.period,
                        modifier = Modifier.alignByBaseline(),
                        color = colors.secondaryText,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontSize = 12.sp,
                            lineHeight = 14.sp,
                            fontWeight = FontWeight.Medium,
                        ),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = displayTime.time,
                        modifier = Modifier.alignByBaseline(),
                        color = colors.primaryText,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontSize = 28.sp,
                            lineHeight = 30.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                    )
                }
            }

            AlarmToggle(
                alarmTime = displayTime.time,
                enabled = alarm.isEnabled,
                onEnabledChange = onEnabledChange,
            )
        }

        Spacer(Modifier.height(10.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(38.dp),
            verticalAlignment = Alignment.Top,
        ) {
            AlarmInfoColumn(
                label = "목적지",
                value = alarm.destination,
                valueColor = colors.primaryText,
                modifier = Modifier.weight(1f),
            )
            AlarmInfoColumn(
                label = "제한시간",
                value = "${alarm.timeLimitMinutes}분",
                valueColor = MaterialTheme.colorScheme.primary,
                horizontalAlignment = Alignment.End,
            )
        }
    }
}

@Composable
private fun AlarmInfoColumn(
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
) {
    val colors = homeAlarmColors()

    Column(
        modifier = modifier,
        horizontalAlignment = horizontalAlignment,
    ) {
        Text(
            text = label,
            color = colors.secondaryText,
            maxLines = 1,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 11.sp,
                lineHeight = 14.sp,
                fontWeight = FontWeight.Medium,
            ),
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = value,
            color = valueColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = if (label == "목적지") 16.sp else 14.sp,
                lineHeight = if (label == "목적지") 20.sp else 18.sp,
                fontWeight = if (label == "목적지") FontWeight.ExtraBold else FontWeight.Bold,
            ),
        )
    }
}

@Composable
private fun AlarmToggle(
    alarmTime: String,
    enabled: Boolean,
    onEnabledChange: (Boolean) -> Unit,
) {
    val isDarkTheme = LocalRingoutThemeMode.current == ThemeMode.Dark
    val painter = when {
        !enabled -> painterResource(Res.drawable.home_toggle_off)
        isDarkTheme -> painterResource(Res.drawable.home_toggle_on_dark)
        else -> painterResource(Res.drawable.home_toggle_on_light)
    }

    Box(
        modifier = Modifier
            .size(48.dp)
            .semantics {
                contentDescription = "$alarmTime 알람"
            }
            .toggleable(
                value = enabled,
                role = Role.Switch,
                onValueChange = onEnabledChange,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painter,
            contentDescription = null,
            modifier = Modifier.size(width = 48.dp, height = 28.dp),
        )
    }
}

@Composable
internal fun HomeAddAlarmButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(58.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary)
            .clickable(
                role = Role.Button,
                onClickLabel = "알람 추가",
                onClick = onClick,
            )
            .semantics {
                contentDescription = "알람 추가"
            },
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(Res.drawable.home_add),
            contentDescription = null,
            modifier = Modifier.size(width = 22.17.dp, height = 24.87.dp),
        )
    }
}

@Composable
internal fun homeAlarmColors(): HomeAlarmColors {
    val isDarkTheme = LocalRingoutThemeMode.current == ThemeMode.Dark
    return if (isDarkTheme) {
        HomeAlarmColors(
            screenBackground = Color.Black,
            cardBackground = Color(0xFF171717),
            primaryText = Color.White,
            secondaryText = Color(0xFF8C8C8C),
        )
    } else {
        HomeAlarmColors(
            screenBackground = Color.White,
            cardBackground = Color(0xFFF5F5F5),
            primaryText = Color(0xFF111827),
            secondaryText = Color(0xFF6B7280),
        )
    }
}

internal data class HomeAlarmColors(
    val screenBackground: Color,
    val cardBackground: Color,
    val primaryText: Color,
    val secondaryText: Color,
)

internal val SecondaryText = Color(0xFF6E756F)
internal val Orange = Color(0xFFFF6D2E)

@Preview
@Composable
private fun DarkAlarmRowPreview() {
    RingoutTheme(themeMode = ThemeMode.Dark) {
        Box(
            modifier = Modifier
                .background(Color.Black)
                .padding(20.dp),
        ) {
            AlarmRow(
                alarm = previewAlarm(isEnabled = true),
                onClick = {},
                onEnabledChange = {},
            )
        }
    }
}

@Preview
@Composable
private fun LightAlarmRowPreview() {
    RingoutTheme(themeMode = ThemeMode.Light) {
        Box(
            modifier = Modifier
                .background(Color.White)
                .padding(20.dp),
        ) {
            AlarmRow(
                alarm = previewAlarm(isEnabled = false),
                onClick = {},
                onEnabledChange = {},
            )
        }
    }
}

private fun previewAlarm(isEnabled: Boolean) = HomeAlarm(
    id = "preview-alarm",
    time = "06:20",
    days = "주말",
    destination = "헬스장",
    timeLimitMinutes = 12,
    isEnabled = isEnabled,
    selectedDays = listOf("토", "일"),
)
