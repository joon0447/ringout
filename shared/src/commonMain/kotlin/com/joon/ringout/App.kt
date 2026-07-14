package com.joon.ringout

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.joon.ringout.presentation.home.HomeScreen

@Composable
@Preview
fun App() {
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
