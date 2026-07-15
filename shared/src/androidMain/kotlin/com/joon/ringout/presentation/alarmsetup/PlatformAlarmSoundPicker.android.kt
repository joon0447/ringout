package com.joon.ringout.presentation.alarmsetup

import android.app.Activity
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberAlarmSoundPicker(
    currentSoundUri: String?,
    onSoundSelected: (AlarmSoundSelection) -> Unit,
): () -> Unit {
    val context = LocalContext.current
    val currentOnSoundSelected = rememberUpdatedState(onSoundSelected)
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        if (result.resultCode != Activity.RESULT_OK) return@rememberLauncherForActivityResult

        @Suppress("DEPRECATION")
        val selectedUri = result.data
            ?.getParcelableExtra<Uri>(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
        selectedUri?.let { uri ->
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION,
                )
            }
        }
        val soundName = selectedUri?.let { uri ->
            runCatching {
                RingtoneManager.getRingtone(context, uri)?.getTitle(context)
            }.getOrNull()
        }.orEmpty().ifBlank { "무음" }
        currentOnSoundSelected.value(
            AlarmSoundSelection(
                name = soundName,
                uri = selectedUri?.toString().orEmpty(),
            ),
        )
    }

    return remember(launcher, currentSoundUri) {
        {
            val existingUri = when {
                currentSoundUri == null -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                currentSoundUri.isBlank() -> null
                else -> Uri.parse(currentSoundUri)
            }
            launcher.launch(
                Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                    addFlags(
                        Intent.FLAG_GRANT_READ_URI_PERMISSION or
                            Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION,
                    )
                    putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM)
                    putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "알람음 선택")
                    putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
                    putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true)
                    putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, existingUri)
                },
            )
        }
    }
}
