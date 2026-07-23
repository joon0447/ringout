package com.joon.ringout.presentation.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.joon.ringout.RingoutTheme
import com.joon.ringout.ThemeMode
import org.jetbrains.compose.resources.painterResource
import ringout.shared.generated.resources.Res
import ringout.shared.generated.resources.splash_logo_dark
import ringout.shared.generated.resources.splash_logo_light

@Composable
fun SplashScreen(
    themeMode: ThemeMode,
    modifier: Modifier = Modifier,
) {
    val isDarkTheme = themeMode == ThemeMode.Dark

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(if (isDarkTheme) Color.Black else SplashLightBackground),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(
                if (isDarkTheme) {
                    Res.drawable.splash_logo_dark
                } else {
                    Res.drawable.splash_logo_light
                },
            ),
            contentDescription = null,
            modifier = Modifier.size(SplashLogoSize),
            contentScale = ContentScale.Crop,
        )
    }
}

private val SplashLightBackground = Color(0xFFFF6D2E)
private val SplashLogoSize = 164.dp

@Preview
@Composable
private fun DarkSplashScreenPreview() {
    RingoutTheme(themeMode = ThemeMode.Dark) {
        SplashScreen(themeMode = ThemeMode.Dark)
    }
}

@Preview
@Composable
private fun LightSplashScreenPreview() {
    RingoutTheme(themeMode = ThemeMode.Light) {
        SplashScreen(themeMode = ThemeMode.Light)
    }
}
