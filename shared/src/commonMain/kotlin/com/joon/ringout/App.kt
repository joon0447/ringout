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
import com.joon.ringout.alarm.AlarmScheduleRequest
import com.joon.ringout.alarm.rememberAlarmController
import com.joon.ringout.presentation.alarmsound.AlarmSoundScreen
import com.joon.ringout.presentation.alarmsetup.AlarmSoundSelection
import com.joon.ringout.presentation.alarmsetup.AlarmSetupScreen
import com.joon.ringout.presentation.alarmsetup.components.weekdaySummary
import com.joon.ringout.presentation.destination.DefaultDestinationSelection
import com.joon.ringout.presentation.destination.DestinationMapScreen
import com.joon.ringout.presentation.destination.DestinationSelection
import com.joon.ringout.presentation.home.HomeAlarm
import com.joon.ringout.presentation.home.HomeScreen
import com.joon.ringout.presentation.settings.SettingsScreen
import com.joon.ringout.presentation.splash.SplashScreen
import kotlinx.coroutines.delay
import kotlin.random.Random

@Composable
fun App(appVersion: String = "") {
    val themeController = rememberThemeController()
    var isSplashVisible by rememberSaveable { mutableStateOf(true) }

    SystemBarAppearanceEffect(themeController.themeMode)

    LaunchedEffect(Unit) {
        delay(SplashDurationMillis)
        isSplashVisible = false
    }

    RingoutTheme(themeMode = themeController.themeMode) {
        if (isSplashVisible) {
            SplashScreen(themeMode = themeController.themeMode)
        } else {
            RingoutAppContent(
                themeMode = themeController.themeMode,
                appVersion = appVersion,
                onThemeModeChange = themeController::setThemeMode,
            )
        }
    }
}

@Composable
private fun RingoutAppContent(
    themeMode: ThemeMode,
    appVersion: String,
    onThemeModeChange: (ThemeMode) -> Unit,
) {
    var destinationName by rememberSaveable { mutableStateOf(DefaultDestinationSelection.name) }
    var destinationAddress by rememberSaveable { mutableStateOf(DefaultDestinationSelection.address) }
    var destinationLatitude by rememberSaveable { mutableStateOf(DefaultDestinationSelection.latitude) }
    var destinationLongitude by rememberSaveable { mutableStateOf(DefaultDestinationSelection.longitude) }
    var alarmSoundName by rememberSaveable { mutableStateOf("Ring Ring Ring") }
    var alarmSoundUri by rememberSaveable { mutableStateOf<String?>(null) }
    var screenName by rememberSaveable { mutableStateOf(AppScreen.Home.name) }
    var alarms by remember { mutableStateOf<List<HomeAlarm>?>(null) }
    var alarmScheduleError by rememberSaveable { mutableStateOf<String?>(null) }
    val destination = DestinationSelection(
        name = destinationName,
        address = destinationAddress,
        latitude = destinationLatitude,
        longitude = destinationLongitude,
    )
    val alarmSound = AlarmSoundSelection(
        name = alarmSoundName,
        uri = alarmSoundUri,
    )
    val screen = AppScreen.valueOf(screenName)
    val alarmController = rememberAlarmController(
        onScheduled = { request ->
            alarms = alarms.orEmpty()
                .filterNot { it.id == request.id } +
                request.toHomeAlarm(enabled = true)
            screenName = AppScreen.Home.name
        },
        onError = { alarmScheduleError = it },
    )
    val savedAlarms = remember(alarmController) {
        alarmController.savedAlarms.map { saved ->
            saved.request.toHomeAlarm(enabled = saved.enabled)
        }
    }
    val visibleAlarms = alarms ?: savedAlarms

    LaunchedEffect(alarmController) {
        if (alarms == null) alarms = savedAlarms
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
            alarms = visibleAlarms,
            onAddAlarm = { screenName = AppScreen.AddAlarm.name },
            onAlarmClick = {},
            onAlarmEnabledChange = { alarmId, enabled ->
                alarmController.setEnabled(alarmId, enabled)
                alarms = visibleAlarms.map { alarm ->
                    if (alarm.id == alarmId) alarm.copy(isEnabled = enabled) else alarm
                }
            },
            onSettingsClick = { screenName = AppScreen.Settings.name },
        )

        AppScreen.Settings -> SettingsScreen(
            themeMode = themeMode,
            appVersion = appVersion,
            onThemeModeChange = onThemeModeChange,
            onBackClick = { screenName = AppScreen.Home.name },
        )

        AppScreen.AddAlarm,
        AppScreen.Destination,
        AppScreen.AlarmSound,
        -> Box(Modifier.fillMaxSize()) {
            AlarmSetupScreen(
                destination = destination.name,
                alarmSound = alarmSound,
                onBackClick = { screenName = AppScreen.Home.name },
                onDestinationClick = { screenName = AppScreen.Destination.name },
                onAlarmSoundClick = { screenName = AppScreen.AlarmSound.name },
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

            if (screen == AppScreen.AlarmSound) {
                AlarmSoundScreen(
                    selectedSound = alarmSound,
                    onBackClick = { screenName = AppScreen.AddAlarm.name },
                    onSaveClick = { selectedSound ->
                        alarmSoundName = selectedSound.name
                        alarmSoundUri = selectedSound.uri
                        screenName = AppScreen.AddAlarm.name
                    },
                )
            }
        }
    }
}

private const val SplashDurationMillis = 1_200L

private enum class AppScreen {
    Home,
    AddAlarm,
    Destination,
    AlarmSound,
    Settings,
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
