package com.joon.ringout.presentation.alarmsetup

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun rememberAlarmSoundPicker(
    currentSoundUri: String?,
    onSoundSelected: (AlarmSoundSelection) -> Unit,
): () -> Unit = remember { {} }
