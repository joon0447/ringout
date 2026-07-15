package com.joon.ringout.alarm

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import org.json.JSONArray
import org.json.JSONObject
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId

@Composable
actual fun rememberAlarmController(
    onScheduled: (AlarmScheduleRequest) -> Unit,
    onError: (String) -> Unit,
): AlarmController {
    val context = LocalContext.current
    val scheduler = remember(context) { AndroidAlarmScheduler(context.applicationContext) }
    val permissionPreferences = remember(context) {
        context.getSharedPreferences(PERMISSION_PREFERENCES_NAME, Context.MODE_PRIVATE)
    }
    val pendingRequest = remember { mutableStateOf<AlarmScheduleRequest?>(null) }
    val currentOnScheduled = rememberUpdatedState(onScheduled)
    val currentOnError = rememberUpdatedState(onError)

    fun requestFullScreenPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) return
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        if (notificationManager.canUseFullScreenIntent()) return
        runCatching {
            context.startActivity(
                Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT).apply {
                    data = Uri.parse("package:${context.packageName}")
                },
            )
        }
    }

    fun scheduleNow(request: AlarmScheduleRequest) {
        runCatching { scheduler.schedule(request) }
            .onSuccess {
                pendingRequest.value = null
                currentOnScheduled.value(request)
                requestFullScreenPermissionIfNeeded()
            }
            .onFailure { error ->
                currentOnError.value(error.message ?: "알람 예약 중 오류가 발생했습니다.")
            }
    }

    val exactAlarmPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) {
        val request = pendingRequest.value ?: return@rememberLauncherForActivityResult
        if (canScheduleExactAlarms(context)) {
            scheduleNow(request)
        } else {
            currentOnError.value("정확한 알람 권한을 허용해 주세요.")
        }
    }
    val overlayPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) {
        val request = pendingRequest.value ?: return@rememberLauncherForActivityResult
        if (!Settings.canDrawOverlays(context)) {
            currentOnError.value(
                "화면이 켜져 있어도 전체 화면 알람을 표시하려면 " +
                    "'다른 앱 위에 표시' 권한이 필요합니다.",
            )
        } else if (canScheduleExactAlarms(context)) {
            scheduleNow(request)
        } else {
            exactAlarmPermissionLauncher.launch(exactAlarmPermissionIntent(context))
        }
    }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        val request = pendingRequest.value ?: return@rememberLauncherForActivityResult
        if (!granted) {
            currentOnError.value("알람 화면과 소리를 표시하려면 알림 권한이 필요합니다.")
        } else if (!Settings.canDrawOverlays(context)) {
            overlayPermissionLauncher.launch(overlayPermissionIntent(context))
        } else if (canScheduleExactAlarms(context)) {
            scheduleNow(request)
        } else {
            exactAlarmPermissionLauncher.launch(exactAlarmPermissionIntent(context))
        }
    }

    return remember(
        scheduler,
        notificationPermissionLauncher,
        overlayPermissionLauncher,
        exactAlarmPermissionLauncher,
    ) {
        AlarmController(
            schedule = { request ->
                pendingRequest.value = request
                when {
                    needsNotificationPermission(context) -> {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                    !Settings.canDrawOverlays(context) -> {
                        overlayPermissionLauncher.launch(overlayPermissionIntent(context))
                    }
                    !canScheduleExactAlarms(context) -> {
                        exactAlarmPermissionLauncher.launch(exactAlarmPermissionIntent(context))
                    }
                    else -> scheduleNow(request)
                }
            },
            setEnabled = { alarmId, enabled ->
                if (enabled && !Settings.canDrawOverlays(context)) {
                    overlayPermissionLauncher.launch(overlayPermissionIntent(context))
                }
                runCatching { scheduler.setEnabled(alarmId, enabled) }
                    .onFailure { error ->
                        currentOnError.value(error.message ?: "알람 상태를 변경하지 못했습니다.")
                    }
            },
            savedAlarms = scheduler.loadAll(),
            ensureFullScreenAccess = {
                val hasRequestedOnFirstLaunch = permissionPreferences.getBoolean(
                    KEY_INITIAL_OVERLAY_PERMISSION_REQUESTED,
                    false,
                )
                if (!hasRequestedOnFirstLaunch) {
                    permissionPreferences.edit()
                        .putBoolean(KEY_INITIAL_OVERLAY_PERMISSION_REQUESTED, true)
                        .apply()
                }
                if (!hasRequestedOnFirstLaunch && !Settings.canDrawOverlays(context)) {
                    overlayPermissionLauncher.launch(overlayPermissionIntent(context))
                }
            },
        )
    }
}

private fun needsNotificationPermission(context: Context): Boolean =
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) !=
        android.content.pm.PackageManager.PERMISSION_GRANTED

private fun canScheduleExactAlarms(context: Context): Boolean =
    Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
        context.getSystemService(AlarmManager::class.java).canScheduleExactAlarms()

private fun exactAlarmPermissionIntent(context: Context): Intent =
    Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
        data = Uri.parse("package:${context.packageName}")
    }

private fun overlayPermissionIntent(context: Context): Intent =
    Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
        data = Uri.parse("package:${context.packageName}")
    }

private const val PERMISSION_PREFERENCES_NAME = "ringout_permission_prompts"
private const val KEY_INITIAL_OVERLAY_PERMISSION_REQUESTED = "initial_overlay_permission_requested"

internal class AndroidAlarmScheduler(private val context: Context) {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun schedule(request: AlarmScheduleRequest) {
        save(StoredAlarm(request = request, enabled = true))
        scheduleNext(request, afterMillis = System.currentTimeMillis())
    }

    fun setEnabled(alarmId: String, enabled: Boolean) {
        val storedAlarm = load(alarmId) ?: return
        save(storedAlarm.copy(enabled = enabled))
        if (enabled) {
            scheduleNext(storedAlarm.request, afterMillis = System.currentTimeMillis())
        } else {
            cancel(alarmId)
        }
    }

    fun onTriggered(alarmId: String) {
        val storedAlarm = load(alarmId) ?: return
        val request = storedAlarm.request
        if (request.repeatEnabled && request.selectedDays.isNotEmpty()) {
            scheduleNext(request, afterMillis = System.currentTimeMillis() + 60_000L)
        } else {
            save(storedAlarm.copy(enabled = false))
        }
    }

    fun rescheduleAll() {
        loadAll()
            .filter(SavedAlarmSchedule::enabled)
            .forEach { stored ->
                runCatching {
                    scheduleNext(stored.request, afterMillis = System.currentTimeMillis())
                }
            }
    }

    fun loadAll(): List<SavedAlarmSchedule> =
        preferences.all.values
            .filterIsInstance<String>()
            .mapNotNull(::decode)
            .map { stored ->
                SavedAlarmSchedule(
                    request = stored.request,
                    enabled = stored.enabled,
                )
            }

    private fun scheduleNext(request: AlarmScheduleRequest, afterMillis: Long) {
        val triggerAtMillis = calculateNextTrigger(request, afterMillis)
        val alarmIntent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmRuntime.ACTION_RING
            putAlarmExtras(request)
        }
        val operation = PendingIntent.getBroadcast(
            context,
            request.id.hashCode(),
            alarmIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val showIntent = PendingIntent.getActivity(
            context,
            request.id.hashCode() xor Int.MIN_VALUE,
            AlarmRingingActivity.intent(context, request),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        alarmManager.setAlarmClock(
            AlarmManager.AlarmClockInfo(triggerAtMillis, showIntent),
            operation,
        )
    }

    private fun cancel(alarmId: String) {
        val operation = PendingIntent.getBroadcast(
            context,
            alarmId.hashCode(),
            Intent(context, AlarmReceiver::class.java).apply {
                action = AlarmRuntime.ACTION_RING
                data = Uri.parse("ringout://alarm/${Uri.encode(alarmId)}")
            },
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE,
        ) ?: return
        alarmManager.cancel(operation)
        operation.cancel()
    }

    private fun save(storedAlarm: StoredAlarm) {
        preferences.edit()
            .putString(storedAlarm.request.id, encode(storedAlarm).toString())
            .apply()
    }

    private fun load(alarmId: String): StoredAlarm? =
        preferences.getString(alarmId, null)?.let(::decode)

    private fun calculateNextTrigger(request: AlarmScheduleRequest, afterMillis: Long): Long {
        val parts = request.time.split(":")
        val hour = parts.getOrNull(0)?.toIntOrNull()?.coerceIn(0, 23) ?: 0
        val minute = parts.getOrNull(1)?.toIntOrNull()?.coerceIn(0, 59) ?: 0
        val alarmTime = LocalTime.of(hour, minute)
        val zone = ZoneId.systemDefault()
        val after = Instant.ofEpochMilli(afterMillis).atZone(zone)
        val repeatDays = if (request.repeatEnabled) {
            request.selectedDays.mapNotNull(DAY_OF_WEEK_BY_KOREAN::get).toSet()
        } else {
            emptySet()
        }

        if (repeatDays.isEmpty()) {
            var candidate = after.toLocalDate().atTime(alarmTime).atZone(zone)
            if (candidate.toInstant().toEpochMilli() <= afterMillis) {
                candidate = candidate.plusDays(1)
            }
            return candidate.toInstant().toEpochMilli()
        }

        for (dayOffset in 0..7L) {
            val candidate = after.toLocalDate()
                .plusDays(dayOffset)
                .atTime(alarmTime)
                .atZone(zone)
            if (
                candidate.dayOfWeek in repeatDays &&
                candidate.toInstant().toEpochMilli() > afterMillis
            ) {
                return candidate.toInstant().toEpochMilli()
            }
        }
        error("다음 알람 시각을 계산하지 못했습니다.")
    }

    private fun encode(storedAlarm: StoredAlarm): JSONObject {
        val request = storedAlarm.request
        return JSONObject()
            .put(KEY_ID, request.id)
            .put(KEY_TIME, request.time)
            .put(KEY_DAYS, JSONArray(request.selectedDays))
            .put(KEY_REPEAT_ENABLED, request.repeatEnabled)
            .put(KEY_LIMIT_MINUTES, request.limitMinutes)
            .put(KEY_DESTINATION_NAME, request.destinationName)
            .put(KEY_DESTINATION_ADDRESS, request.destinationAddress)
            .put(KEY_DESTINATION_LATITUDE, request.destinationLatitude)
            .put(KEY_DESTINATION_LONGITUDE, request.destinationLongitude)
            .put(KEY_TARGET_DISTANCE_KM, request.targetDistanceKm)
            .put(KEY_SOUND_NAME, request.alarmSoundName)
            .put(KEY_SOUND_URI, request.alarmSoundUri ?: JSONObject.NULL)
            .put(KEY_ENABLED, storedAlarm.enabled)
    }

    private fun decode(raw: String): StoredAlarm? = runCatching {
        val json = JSONObject(raw)
        val daysJson = json.optJSONArray(KEY_DAYS) ?: JSONArray()
        val days = buildList {
            for (index in 0 until daysJson.length()) {
                daysJson.optString(index).takeIf(String::isNotBlank)?.let(::add)
            }
        }
        StoredAlarm(
            request = AlarmScheduleRequest(
                id = json.getString(KEY_ID),
                time = json.getString(KEY_TIME),
                selectedDays = days,
                repeatEnabled = json.optBoolean(KEY_REPEAT_ENABLED, true),
                limitMinutes = json.optInt(KEY_LIMIT_MINUTES, 12),
                destinationName = json.optString(KEY_DESTINATION_NAME),
                destinationAddress = json.optString(KEY_DESTINATION_ADDRESS),
                destinationLatitude = json.optDouble(KEY_DESTINATION_LATITUDE),
                destinationLongitude = json.optDouble(KEY_DESTINATION_LONGITUDE),
                targetDistanceKm = json.optDouble(KEY_TARGET_DISTANCE_KM, 1.2),
                alarmSoundName = json.optString(KEY_SOUND_NAME, "기본 알람음"),
                alarmSoundUri = if (json.isNull(KEY_SOUND_URI)) null else json.optString(KEY_SOUND_URI),
            ),
            enabled = json.optBoolean(KEY_ENABLED, true),
        )
    }.getOrNull()

    private data class StoredAlarm(
        val request: AlarmScheduleRequest,
        val enabled: Boolean,
    )

    private companion object {
        const val PREFERENCES_NAME = "ringout_scheduled_alarms"
        const val KEY_ID = "id"
        const val KEY_TIME = "time"
        const val KEY_DAYS = "days"
        const val KEY_REPEAT_ENABLED = "repeatEnabled"
        const val KEY_LIMIT_MINUTES = "limitMinutes"
        const val KEY_DESTINATION_NAME = "destinationName"
        const val KEY_DESTINATION_ADDRESS = "destinationAddress"
        const val KEY_DESTINATION_LATITUDE = "destinationLatitude"
        const val KEY_DESTINATION_LONGITUDE = "destinationLongitude"
        const val KEY_TARGET_DISTANCE_KM = "targetDistanceKm"
        const val KEY_SOUND_NAME = "soundName"
        const val KEY_SOUND_URI = "soundUri"
        const val KEY_ENABLED = "enabled"

        val DAY_OF_WEEK_BY_KOREAN = mapOf(
            "월" to DayOfWeek.MONDAY,
            "화" to DayOfWeek.TUESDAY,
            "수" to DayOfWeek.WEDNESDAY,
            "목" to DayOfWeek.THURSDAY,
            "금" to DayOfWeek.FRIDAY,
            "토" to DayOfWeek.SATURDAY,
            "일" to DayOfWeek.SUNDAY,
        )
    }
}
