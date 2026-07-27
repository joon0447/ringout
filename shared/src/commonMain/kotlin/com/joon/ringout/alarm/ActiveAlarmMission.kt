package com.joon.ringout.alarm

data class ActiveAlarmMission(
    val alarmId: String,
    val destinationName: String,
    val limitMinutes: Int,
    val expiresAtEpochMillis: Long,
)

internal fun ActiveAlarmMission.isExpiredAt(epochMillis: Long): Boolean =
    expiresAtEpochMillis <= epochMillis
