package com.joon.ringout.presentation.alarmsetup

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.joon.ringout.RingoutTheme
import com.joon.ringout.presentation.alarmsetup.components.DestinationCard
import com.joon.ringout.presentation.alarmsetup.components.LimitTimeCard
import com.joon.ringout.presentation.alarmsetup.components.RepeatScheduleCard
import com.joon.ringout.presentation.alarmsetup.components.SaveAlarmButton
import com.joon.ringout.presentation.alarmsetup.components.AlarmSoundCard
import com.joon.ringout.presentation.alarmsetup.components.TimePickerCard
import com.joon.ringout.presentation.alarmsetup.components.TimeSettingDialog

@Composable
fun AlarmSetupScreen(
    destination: String,
    destinationAddress: String,
    onBackClick: () -> Unit,
    onDestinationClick: () -> Unit,
    onSaveClick: (
        time: String,
        selectedDays: List<String>,
        limitMinutes: Int,
        alarmSound: AlarmSoundSelection,
    ) -> Unit,
    modifier: Modifier = Modifier,
) {
    var limitMinutes by rememberSaveable { mutableStateOf(12) }
    var repeatEnabled by rememberSaveable { mutableStateOf(true) }
    var selectedDaysValue by rememberSaveable { mutableStateOf("월,화,수,목,금") }
    var alarmTime by rememberSaveable { mutableStateOf("06:20") }
    var showTimeDialog by rememberSaveable { mutableStateOf(false) }
    var alarmSoundName by rememberSaveable { mutableStateOf("기본 알람음") }
    var alarmSoundUri by rememberSaveable { mutableStateOf<String?>(null) }
    val selectedDays = selectedDaysValue.split(",").filter(String::isNotBlank)
    val openAlarmSoundPicker = rememberAlarmSoundPicker(
        currentSoundUri = alarmSoundUri,
        onSoundSelected = { selection ->
            alarmSoundName = selection.name
            alarmSoundUri = selection.uri
        },
    )

    if (showTimeDialog) {
        TimeSettingDialog(
            initialTime = alarmTime,
            onDismissRequest = { showTimeDialog = false },
            onConfirm = { selectedTime ->
                alarmTime = selectedTime
                showTimeDialog = false
            },
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, top = 10.dp, end = 16.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        SetupHeader(onBackClick = onBackClick)

        Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
            TimePickerCard(
                time = alarmTime,
                days = selectedDays,
                onClick = { showTimeDialog = true },
            )
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                DestinationCard(
                    destination = destination,
                    address = destinationAddress,
                    distance = "지도를 눌러 목표 지점을 변경할 수 있어요",
                    onClick = onDestinationClick,
                )
                LimitTimeCard(
                    minutes = limitMinutes,
                    onMinutesChange = { limitMinutes = it },
                )
                AlarmSoundCard(
                    soundName = alarmSoundName,
                    onClick = openAlarmSoundPicker,
                )
            }
            RepeatScheduleCard(
                repeatEnabled = repeatEnabled,
                selectedDays = selectedDays,
                onRepeatEnabledChange = { repeatEnabled = it },
                onDayClick = { day ->
                    selectedDaysValue = (
                        if (day in selectedDays) selectedDays - day else selectedDays + day
                    ).joinToString(",")
                },
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SaveAlarmButton(
                onClick = {
                    onSaveClick(
                        alarmTime,
                        selectedDays,
                        limitMinutes,
                        AlarmSoundSelection(alarmSoundName, alarmSoundUri),
                    )
                },
            )
            Text(
                text = "실패하면 알람이 다시 울리고 미션이 새로 시작돼요.",
                modifier = Modifier.fillMaxWidth(),
                color = SecondaryText,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
        }
    }
}

@Composable
private fun SetupHeader(onBackClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clickable(onClick = onBackClick),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(Modifier.size(22.dp)) {
                val stroke = 1.8.dp.toPx()
                drawLine(PrimaryText, Offset(size.width * .65f, size.height * .2f), Offset(size.width * .35f, size.height * .5f), stroke, StrokeCap.Round)
                drawLine(PrimaryText, Offset(size.width * .35f, size.height * .5f), Offset(size.width * .65f, size.height * .8f), stroke, StrokeCap.Round)
            }
        }
        Spacer(Modifier.size(8.dp))
        Text(
            text = "알람 설정",
            color = PrimaryText,
            style = MaterialTheme.typography.titleLarge.copy(
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
            ),
        )
    }
}

private val Background = Color(0xFFF7F8F5)
private val PrimaryText = Color(0xFF161A17)
private val SecondaryText = Color(0xFF6E756F)

@Preview
@Composable
private fun AlarmSetupScreenPreview() {
    RingoutTheme {
        AlarmSetupScreen(
            destination = "강남역 2번 출구",
            destinationAddress = "서울 강남구 강남대로 지하 396",
            onBackClick = {},
            onDestinationClick = {},
            onSaveClick = { _, _, _, _ -> },
        )
    }
}
