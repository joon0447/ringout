package com.joon.ringout.alarm

import android.content.Context
import android.content.Intent
import android.graphics.Color
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
import com.joon.ringout.rememberThemeController
import com.joon.ringout.presentation.ringing.AlarmRingingScreen

class AlarmRingingActivity : ComponentActivity() {
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
        val targetDistanceKm = intent.getDoubleExtra(AlarmRuntime.EXTRA_TARGET_DISTANCE_KM, 1.2)
        setContent {
            val themeController = rememberThemeController()
            SystemBarAppearanceEffect(themeController.themeMode)
            RingoutTheme(themeMode = themeController.themeMode) {
                AlarmRingingScreen(
                    alarmTime = alarmTime,
                    limitMinutes = limitMinutes,
                    targetDistanceKm = targetDistanceKm,
                    onStartMissionClick = ::stopAlarmAndOpenApp,
                )
            }
        }
    }

    private fun stopAlarmAndOpenApp() {
        stopService(
            Intent(this, AlarmRingingService::class.java).apply {
                action = AlarmRuntime.ACTION_STOP
            },
        )
        packageManager.getLaunchIntentForPackage(packageName)?.let { launchIntent ->
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(launchIntent)
        }
        finishAndRemoveTask()
    }

    companion object {
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
    }
}
