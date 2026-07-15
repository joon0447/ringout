package com.joon.ringout.presentation.alarmsetup

import androidx.compose.runtime.Composable

data class AlarmSoundSelection(
    val name: String,
    val uri: String?,
)

@Composable
expect fun rememberAlarmSoundPicker(
    currentSoundUri: String?,
    onSoundSelected: (AlarmSoundSelection) -> Unit,
): () -> Unit
