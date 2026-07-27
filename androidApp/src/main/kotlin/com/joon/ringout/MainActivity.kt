package com.joon.ringout

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import com.joon.ringout.alarm.ActiveAlarmMission
import com.joon.ringout.alarm.ActiveAlarmMissionStore

class MainActivity : ComponentActivity() {
    private lateinit var activeAlarmMissionStore: ActiveAlarmMissionStore
    private var activeAlarmMission by mutableStateOf<ActiveAlarmMission?>(null)

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
        activeAlarmMissionStore = ActiveAlarmMissionStore(applicationContext)
        activeAlarmMission = activeAlarmMissionStore.read()

        setContent {
            App(
                appVersion = BuildConfig.VERSION_NAME,
                activeAlarmMission = activeAlarmMission,
                onActiveAlarmMissionExpired = ::clearActiveAlarmMission,
            )
        }
    }

    override fun onResume() {
        super.onResume()
        activeAlarmMission = activeAlarmMissionStore.read()
    }

    private fun clearActiveAlarmMission() {
        activeAlarmMissionStore.clear()
        activeAlarmMission = null
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App(appVersion = BuildConfig.VERSION_NAME)
}
