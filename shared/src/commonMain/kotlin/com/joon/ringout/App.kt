package com.joon.ringout

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import com.joon.ringout.presentation.alarmsetup.AlarmSetupScreen
import com.joon.ringout.presentation.home.HomeAlarm
import com.joon.ringout.presentation.home.HomeScreen

@Composable
@Preview
fun App() {
    RingoutTheme {
        var destination by remember { mutableStateOf("강남역 2번 출구") }
        var alarms by remember { mutableStateOf(emptyList<HomeAlarm>()) }
        var screen by remember { mutableStateOf(AppScreen.Home) }

        when (screen) {
            AppScreen.Home -> HomeScreen(
                alarms = alarms,
                onAddAlarm = { screen = AppScreen.AddAlarm },
                onAlarmClick = {},
                onAlarmEnabledChange = { alarmId, enabled ->
                    alarms = alarms.map { alarm ->
                        if (alarm.id == alarmId) alarm.copy(isEnabled = enabled) else alarm
                    }
                },
                onSettingsClick = {},
            )

            AppScreen.AddAlarm -> AlarmSetupScreen(
                destination = destination,
                onBackClick = { screen = AppScreen.Home },
                onDestinationClick = { destination = "강남역 2번 출구" },
                onSaveClick = { time, selectedDays, limitMinutes ->
                    alarms = alarms + HomeAlarm(
                        id = "alarm-${alarms.size + 1}",
                        time = time,
                        days = selectedDays.joinToString(" "),
                        destination = destination,
                        timeLimitMinutes = limitMinutes,
                        isEnabled = true,
                    )
                    screen = AppScreen.Home
                },
            )
        }
    }
}

private enum class AppScreen {
    Home,
    AddAlarm,
}
