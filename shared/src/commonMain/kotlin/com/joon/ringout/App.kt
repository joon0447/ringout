package com.joon.ringout

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.joon.ringout.alarm.AlarmScheduleRequest
import com.joon.ringout.alarm.rememberAlarmController
import com.joon.ringout.presentation.alarmsetup.AlarmSetupScreen
import com.joon.ringout.presentation.alarmsetup.components.weekdaySummary
import com.joon.ringout.presentation.destination.DefaultDestinationSelection
import com.joon.ringout.presentation.destination.DestinationMapScreen
import com.joon.ringout.presentation.destination.DestinationSelection
import com.joon.ringout.presentation.home.HomeAlarm
import com.joon.ringout.presentation.home.HomeScreen
import kotlin.random.Random

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
        var alarmScheduleError by rememberSaveable { mutableStateOf<String?>(null) }
        val destination = DestinationSelection(
            name = destinationName,
            address = destinationAddress,
            latitude = destinationLatitude,
            longitude = destinationLongitude,
        )
        val screen = AppScreen.valueOf(screenName)
        val alarmController = rememberAlarmController(
            onScheduled = { request ->
                alarms = alarms
                    .filterNot { it.id == request.id } +
                    request.toHomeAlarm(enabled = true)
                screenName = AppScreen.Home.name
            },
            onError = { alarmScheduleError = it },
        )
        LaunchedEffect(alarmController) {
            if (alarms.isEmpty()) {
                alarms = alarmController.savedAlarms.map { saved ->
                    saved.request.toHomeAlarm(enabled = saved.enabled)
                }
            }
            alarmController.ensureFullScreenAccess()
        }

        if (alarmScheduleError != null) {
            AlertDialog(
                onDismissRequest = { alarmScheduleError = null },
                title = { Text("알람을 예약할 수 없습니다") },
                text = { Text(alarmScheduleError.orEmpty()) },
                confirmButton = {
                    TextButton(onClick = { alarmScheduleError = null }) {
                        Text("확인")
                    }
                },
            )
        }

        when (screen) {
            AppScreen.Home -> HomeScreen(
                alarms = alarms,
                onAddAlarm = { screenName = AppScreen.AddAlarm.name },
                onAlarmClick = {},
                onAlarmEnabledChange = { alarmId, enabled ->
                    alarmController.setEnabled(alarmId, enabled)
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
                    onSaveClick = { time, selectedDays, repeatEnabled, limitMinutes, alarmSound ->
                        alarmController.schedule(
                            AlarmScheduleRequest(
                                id = "alarm-${Random.nextInt(1, Int.MAX_VALUE)}",
                                time = time,
                                selectedDays = selectedDays,
                                repeatEnabled = repeatEnabled,
                                limitMinutes = limitMinutes,
                                destinationName = destination.name,
                                destinationAddress = destination.address,
                                destinationLatitude = destination.latitude,
                                destinationLongitude = destination.longitude,
                                alarmSoundName = alarmSound.name,
                                alarmSoundUri = alarmSound.uri,
                            ),
                        )
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

private fun AlarmScheduleRequest.toHomeAlarm(enabled: Boolean): HomeAlarm = HomeAlarm(
    id = id,
    time = time,
    days = if (repeatEnabled) weekdaySummary(selectedDays) else "한 번",
    destination = destinationName,
    timeLimitMinutes = limitMinutes,
    isEnabled = enabled,
    targetAddress = destinationAddress,
    targetLatitude = destinationLatitude,
    targetLongitude = destinationLongitude,
    alarmSoundName = alarmSoundName,
    alarmSoundUri = alarmSoundUri,
    selectedDays = selectedDays,
    repeatEnabled = repeatEnabled,
)
