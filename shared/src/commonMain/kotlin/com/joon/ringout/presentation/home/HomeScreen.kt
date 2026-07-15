package com.joon.ringout.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import com.joon.ringout.RingoutTheme
import com.joon.ringout.presentation.home.components.AlarmListHeader
import com.joon.ringout.presentation.home.components.AlarmRow
import com.joon.ringout.presentation.home.components.HomeBottomBar

data class HomeAlarm(
    val id: String,
    val time: String,
    val days: String,
    val destination: String,
    val timeLimitMinutes: Int,
    val isEnabled: Boolean,
    val targetAddress: String = "",
    val targetLatitude: Double? = null,
    val targetLongitude: Double? = null,
)

@Composable
fun HomeScreen(
    alarms: List<HomeAlarm>,
    onAddAlarm: () -> Unit,
    onAlarmClick: (String) -> Unit,
    onAlarmEnabledChange: (String, Boolean) -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(HomeBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(start = 16.dp, top = 10.dp, end = 16.dp, bottom = 16.dp),
    ) {
        AlarmListHeader(onAddAlarm = onAddAlarm)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            if (alarms.isEmpty()) {
                Text(
                    text = "등록된 알람이 없습니다.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                    ),
                    color = EmptyStateText,
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    alarms.forEach { alarm ->
                        AlarmRow(
                            alarm = alarm,
                            onClick = { onAlarmClick(alarm.id) },
                            onEnabledChange = { enabled -> onAlarmEnabledChange(alarm.id, enabled) },
                        )
                    }
                }
            }
        }

        HomeBottomBar(onSettingsClick = onSettingsClick)
    }
}

private val HomeBackground = Color(0xFFF7F8F5)
private val EmptyStateText = Color(0xFF161A17)

@Preview
@Composable
private fun HomeScreenPreview() {
    RingoutTheme {
        HomeScreen(
            alarms = emptyList(),
            onAddAlarm = {},
            onAlarmClick = {},
            onAlarmEnabledChange = { _, _ -> },
            onSettingsClick = {},
        )
    }
}
