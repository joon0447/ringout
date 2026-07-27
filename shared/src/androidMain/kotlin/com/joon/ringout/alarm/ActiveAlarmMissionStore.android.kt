package com.joon.ringout.alarm

import android.content.Context
import android.content.Intent

class ActiveAlarmMissionStore(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences(
        PreferencesName,
        Context.MODE_PRIVATE,
    )

    fun read(): ActiveAlarmMission? {
        val alarmId = preferences.getString(KeyAlarmId, null)
            ?.takeIf(String::isNotBlank)
            ?: return null
        val destinationName = preferences.getString(KeyDestinationName, null)
            ?.takeIf(String::isNotBlank)
            ?: DefaultDestinationName
        val limitMinutes = preferences.getInt(KeyLimitMinutes, DefaultLimitMinutes)
            .coerceAtLeast(0)
        val expiresAtEpochMillis = preferences.getLong(KeyExpiresAtEpochMillis, 0L)
        val mission = ActiveAlarmMission(
            alarmId = alarmId,
            destinationName = destinationName,
            limitMinutes = limitMinutes,
            expiresAtEpochMillis = expiresAtEpochMillis,
        )

        return if (mission.isExpiredAt(System.currentTimeMillis())) {
            clear()
            null
        } else {
            mission
        }
    }

    fun clear() {
        preferences.edit().clear().apply()
    }

    internal fun saveFrom(intent: Intent) {
        val alarmId = intent.getStringExtra(AlarmRuntime.EXTRA_ALARM_ID)
            ?.takeIf(String::isNotBlank)
            ?: return
        val destinationName = intent
            .getStringExtra(AlarmRuntime.EXTRA_DESTINATION_NAME)
            .orEmpty()
            .ifBlank { DefaultDestinationName }
        val limitMinutes = intent
            .getIntExtra(AlarmRuntime.EXTRA_LIMIT_MINUTES, DefaultLimitMinutes)
            .coerceAtLeast(0)
        val expiresAtEpochMillis =
            System.currentTimeMillis() + limitMinutes * MillisPerMinute

        preferences.edit()
            .putString(KeyAlarmId, alarmId)
            .putString(KeyDestinationName, destinationName)
            .putInt(KeyLimitMinutes, limitMinutes)
            .putLong(KeyExpiresAtEpochMillis, expiresAtEpochMillis)
            .apply()
    }

    private companion object {
        const val PreferencesName = "ringout_active_alarm_mission"
        const val KeyAlarmId = "alarm_id"
        const val KeyDestinationName = "destination_name"
        const val KeyLimitMinutes = "limit_minutes"
        const val KeyExpiresAtEpochMillis = "expires_at_epoch_millis"
        const val DefaultDestinationName = "선택한 목적지"
        const val DefaultLimitMinutes = 12
        const val MillisPerMinute = 60_000L
    }
}
