package com.joon.ringout.presentation.destination

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun PlatformDestinationMap(
    initialLatitude: Double,
    initialLongitude: Double,
    onCameraMoveStarted: () -> Unit,
    onCameraIdle: (
        latitude: Double,
        longitude: Double,
        placeName: String?,
        address: String?,
    ) -> Unit,
    onMapError: (String) -> Unit,
    modifier: Modifier = Modifier,
)
