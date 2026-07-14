package com.joon.ringout

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import org.jetbrains.compose.resources.Font
import ringout.shared.generated.resources.Res
import ringout.shared.generated.resources.pretendard_black
import ringout.shared.generated.resources.pretendard_bold
import ringout.shared.generated.resources.pretendard_extra_bold
import ringout.shared.generated.resources.pretendard_light
import ringout.shared.generated.resources.pretendard_medium
import ringout.shared.generated.resources.pretendard_thin

@Composable
fun RingoutTheme(content: @Composable () -> Unit) {
    val pretendard = FontFamily(
        Font(Res.font.pretendard_thin, FontWeight.Thin),
        Font(Res.font.pretendard_light, FontWeight.Light),
        Font(Res.font.pretendard_medium, FontWeight.Normal),
        Font(Res.font.pretendard_medium, FontWeight.Medium),
        Font(Res.font.pretendard_bold, FontWeight.Bold),
        Font(Res.font.pretendard_extra_bold, FontWeight.ExtraBold),
        Font(Res.font.pretendard_black, FontWeight.Black),
    )
    val defaults = Typography()
    val typography = defaults.copy(
        displayLarge = defaults.displayLarge.copy(fontFamily = pretendard),
        displayMedium = defaults.displayMedium.copy(fontFamily = pretendard),
        displaySmall = defaults.displaySmall.copy(fontFamily = pretendard),
        headlineLarge = defaults.headlineLarge.copy(fontFamily = pretendard),
        headlineMedium = defaults.headlineMedium.copy(fontFamily = pretendard),
        headlineSmall = defaults.headlineSmall.copy(fontFamily = pretendard),
        titleLarge = defaults.titleLarge.copy(fontFamily = pretendard),
        titleMedium = defaults.titleMedium.copy(fontFamily = pretendard),
        titleSmall = defaults.titleSmall.copy(fontFamily = pretendard),
        bodyLarge = defaults.bodyLarge.copy(fontFamily = pretendard),
        bodyMedium = defaults.bodyMedium.copy(fontFamily = pretendard),
        bodySmall = defaults.bodySmall.copy(fontFamily = pretendard),
        labelLarge = defaults.labelLarge.copy(fontFamily = pretendard),
        labelMedium = defaults.labelMedium.copy(fontFamily = pretendard),
        labelSmall = defaults.labelSmall.copy(fontFamily = pretendard),
    )

    MaterialTheme(
        typography = typography,
        content = content,
    )
}
