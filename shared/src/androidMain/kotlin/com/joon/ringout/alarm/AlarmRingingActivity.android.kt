package com.joon.ringout.alarm

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.joon.ringout.RingoutTheme
import com.joon.ringout.SystemBarAppearanceEffect
import com.joon.ringout.ThemeMode
import com.joon.ringout.presentation.ringing.AlarmRingingScreen
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class AlarmRingingActivity : ComponentActivity() {
    private val activeAlarmMissionStore by lazy {
        ActiveAlarmMissionStore(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(
                scrim = Color.TRANSPARENT,
            ),
            navigationBarStyle = SystemBarStyle.dark(
                scrim = Color.TRANSPARENT,
            ),
        )
        super.onCreate(savedInstanceState)
        if (intent.action == AlarmRuntime.ACTION_STOP) {
            stopAlarmAndOpenApp()
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
            )
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() = Unit
            },
        )

        val alarmTime = intent.getStringExtra(AlarmRuntime.EXTRA_ALARM_TIME).orEmpty()
        val limitMinutes = intent.getIntExtra(AlarmRuntime.EXTRA_LIMIT_MINUTES, 12)
        val destinationName = intent
            .getStringExtra(AlarmRuntime.EXTRA_DESTINATION_NAME)
            .orEmpty()
            .ifBlank { "선택한 목적지" }
        setContent {
            SystemBarAppearanceEffect(ThemeMode.Dark)
            RingoutTheme(themeMode = ThemeMode.Dark) {
                AlarmRingingScreen(
                    alarmTime = alarmTime,
                    dateText = currentDateText(),
                    limitMinutes = limitMinutes,
                    destinationName = destinationName,
                    onDismissAndNavigateClick = ::stopAlarmAndOpenApp,
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        when (intent.action) {
            AlarmRuntime.ACTION_STOP -> stopAlarmAndOpenApp()
            AlarmRuntime.ACTION_RING -> recreate()
        }
    }

    private fun stopAlarmAndOpenApp() {
        activeAlarmMissionStore.saveFrom(intent)
        stopService(
            Intent(this, AlarmRingingService::class.java).apply {
                action = AlarmRuntime.ACTION_STOP
            },
        )
        packageManager.getLaunchIntentForPackage(packageName)?.let { launchIntent ->
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(launchIntent)
        }
        finish()
    }

    private fun currentDateText(): String =
        LocalDate.now().format(AlarmDateFormatter)

    companion object {
        private val AlarmDateFormatter =
            DateTimeFormatter.ofPattern("yyyy년 M월 d일 EEEE", Locale.KOREAN)

        fun intent(context: Context, request: AlarmScheduleRequest): Intent =
            Intent(context, AlarmRingingActivity::class.java).apply {
                action = AlarmRuntime.ACTION_RING
                putAlarmExtras(request)
                addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP,
                )
            }

        fun intentFromRuntime(context: Context, source: Intent): Intent =
            Intent(context, AlarmRingingActivity::class.java).apply {
                action = AlarmRuntime.ACTION_RING
                replaceExtras(source)
                data = source.data
                addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP,
                )
            }

        fun dismissIntentFromRuntime(context: Context, source: Intent): Intent {
            val alarmId = source.getStringExtra(AlarmRuntime.EXTRA_ALARM_ID).orEmpty()
            return Intent(context, AlarmRingingActivity::class.java).apply {
                action = AlarmRuntime.ACTION_STOP
                replaceExtras(source)
                data = Uri.parse(
                    "ringout://alarm/${Uri.encode(alarmId)}/stop",
                )
                addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP,
                )
            }
        }
    }
}
