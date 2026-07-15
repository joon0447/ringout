package com.joon.ringout

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.joon.ringout.presentation.alarmsetup.AlarmSetupScreen
import com.joon.ringout.presentation.alarmsetup.components.weekdaySummary
import com.joon.ringout.presentation.destination.DefaultDestinationSelection
import com.joon.ringout.presentation.destination.DestinationMapScreen
import com.joon.ringout.presentation.destination.DestinationSelection
import com.joon.ringout.presentation.home.HomeAlarm
import com.joon.ringout.presentation.home.HomeScreen

@Composable
@Preview
fun App() {
    RingoutTheme {
        var destinationName by rememberSaveable { mutableStateOf(DefaultDestinationSelection.name) }
        var destinationAddress by rememberSaveable { mutableStateOf(DefaultDestinationSelection.address) }
        var destinationLatitude by rememberSaveable { mutableStateOf(DefaultDestinationSelection.latitude) }
        var destinationLongitude by rememberSaveable { mutableStateOf(DefaultDestinationSelection.longitude) }
        var screenName by rememberSaveable { mutableStateOf(AppScreen.Home.name) }
        var alarms by remember { mutableStateOf(emptyList<HomeAlarm>()) }
        val destination = DestinationSelection(
            name = destinationName,
            address = destinationAddress,
            latitude = destinationLatitude,
            longitude = destinationLongitude,
        )
        val screen = AppScreen.valueOf(screenName)

        when (screen) {
            AppScreen.Home -> HomeScreen(
                alarms = alarms,
                onAddAlarm = { screenName = AppScreen.AddAlarm.name },
                onAlarmClick = {},
                onAlarmEnabledChange = { alarmId, enabled ->
                    alarms = alarms.map { alarm ->
                        if (alarm.id == alarmId) alarm.copy(isEnabled = enabled) else alarm
                    }
                },
                onSettingsClick = {},
            )

            AppScreen.AddAlarm,
            AppScreen.Destination,
            -> Box(Modifier.fillMaxSize()) {
                AlarmSetupScreen(
                    destination = destination.name,
                    destinationAddress = destination.address,
                    onBackClick = { screenName = AppScreen.Home.name },
                    onDestinationClick = { screenName = AppScreen.Destination.name },
                    onSaveClick = { time, selectedDays, limitMinutes ->
                        alarms = alarms + HomeAlarm(
                            id = "alarm-${alarms.size + 1}",
                            time = time,
                            days = weekdaySummary(selectedDays),
                            destination = destination.name,
                            timeLimitMinutes = limitMinutes,
                            isEnabled = true,
                            targetAddress = destination.address,
                            targetLatitude = destination.latitude,
                            targetLongitude = destination.longitude,
                        )
                        screenName = AppScreen.Home.name
                    },
                )

                if (screen == AppScreen.Destination) {
                    DestinationMapScreen(
                        initialSelection = destination,
                        onBackClick = { screenName = AppScreen.AddAlarm.name },
                        onConfirmClick = { selectedDestination ->
                            destinationName = selectedDestination.name
                            destinationAddress = selectedDestination.address
                            destinationLatitude = selectedDestination.latitude
                            destinationLongitude = selectedDestination.longitude
                            screenName = AppScreen.AddAlarm.name
                        },
                    )
                }
            }
        }
    }
}

private enum class AppScreen {
    Home,
    AddAlarm,
    Destination,
}
