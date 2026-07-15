package com.joon.ringout.presentation.ringing

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.joon.ringout.RingoutTheme
import com.joon.ringout.presentation.ringing.components.AlarmPulseVisual
import com.joon.ringout.presentation.ringing.components.MissionRulePanel
import com.joon.ringout.presentation.ringing.components.RingingActionButton

@Composable
fun AlarmRingingScreen(
    alarmTime: String,
    limitMinutes: Int,
    targetDistanceKm: Double,
    onStartMissionClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(RingingBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(start = 20.dp, top = 10.dp, end = 20.dp, bottom = 22.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Text(
                text = alarmTime,
                color = PrimaryText,
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 62.sp,
                    lineHeight = 68.sp,
                    fontWeight = FontWeight.Bold,
                ),
            )
            Text(
                text = "일어나서 러닝 미션을\n시작하세요",
                modifier = Modifier.fillMaxWidth(),
                color = PrimaryText,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = 28.sp,
                    lineHeight = 31.sp,
                    fontWeight = FontWeight.Bold,
                ),
            )
        }

        AlarmPulseVisual()

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            MissionRulePanel(
                targetDistanceKm = targetDistanceKm,
                limitMinutes = limitMinutes,
            )
            RingingActionButton(onClick = onStartMissionClick)
        }
    }
}

private val RingingBackground = Color(0xFFF7F8F5)
private val PrimaryText = Color(0xFF161A17)

@Preview
@Composable
private fun AlarmRingingScreenPreview() {
    RingoutTheme {
        AlarmRingingScreen(
            alarmTime = "06:20",
            limitMinutes = 12,
            targetDistanceKm = 1.2,
            onStartMissionClick = {},
        )
    }
}
