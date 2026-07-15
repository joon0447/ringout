package com.joon.ringout.presentation.destination

import android.content.Context
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.kakao.vectormap.GestureType
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.MapView
import com.kakao.vectormap.camera.CameraPosition
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors
import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

@Composable
actual fun PlatformDestinationMap(
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
    modifier: Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentOnCameraMoveStarted = rememberUpdatedState(onCameraMoveStarted)
    val currentOnCameraIdle = rememberUpdatedState(onCameraIdle)
    val currentOnMapError = rememberUpdatedState(onMapError)
    val mainHandler = remember { Handler(Looper.getMainLooper()) }
    val requestSerial = remember { AtomicInteger() }
    val reverseGeocoder = remember(context) { KakaoReverseGeocoder(context.applicationContext) }
    val mapView = remember(context, initialLatitude, initialLongitude) { MapView(context) }
    val addressExecutor = remember(mapView) { Executors.newSingleThreadExecutor() }

    DisposableEffect(mapView, lifecycleOwner) {
        var isFinished = false
        var addressRequest: java.util.concurrent.Future<*>? = null
        val isDisposed = AtomicBoolean(false)

        fun finishMap() {
            if (!isFinished) {
                isFinished = true
                mapView.finish()
            }
        }

        mapView.start(
            object : MapLifeCycleCallback() {
                override fun onMapDestroy() = Unit

                override fun onMapError(error: Exception) {
                    if (isDisposed.get()) return
                    mainHandler.post {
                        if (!isDisposed.get()) {
                            currentOnMapError.value(error.message ?: "카카오 지도 인증에 실패했습니다.")
                        }
                    }
                }
            },
            object : KakaoMapReadyCallback() {
                override fun getPosition(): LatLng = LatLng.from(initialLatitude, initialLongitude)

                override fun getZoomLevel(): Int = 17

                override fun onMapReady(kakaoMap: KakaoMap) {
                    if (isDisposed.get()) return
                    kakaoMap.setOnCameraMoveStartListener(
                        object : KakaoMap.OnCameraMoveStartListener {
                            override fun onCameraMoveStart(
                                kakaoMap: KakaoMap,
                                gestureType: GestureType,
                            ) {
                                if (isDisposed.get()) return
                                requestSerial.incrementAndGet()
                                addressRequest?.cancel(true)
                                currentOnCameraMoveStarted.value()
                            }
                        },
                    )
                    kakaoMap.setOnCameraMoveEndListener(
                        object : KakaoMap.OnCameraMoveEndListener {
                            override fun onCameraMoveEnd(
                                kakaoMap: KakaoMap,
                                cameraPosition: CameraPosition,
                                gestureType: GestureType,
                            ) {
                                if (isDisposed.get()) return
                                val center = cameraPosition.position
                                val serial = requestSerial.incrementAndGet()
                                addressRequest?.cancel(true)
                                try {
                                    addressRequest = addressExecutor.submit {
                                        val resolved = reverseGeocoder.resolve(
                                            latitude = center.latitude,
                                            longitude = center.longitude,
                                        )
                                        mainHandler.post {
                                            if (!isDisposed.get() && serial == requestSerial.get()) {
                                                currentOnCameraIdle.value(
                                                    center.latitude,
                                                    center.longitude,
                                                    resolved?.placeName,
                                                    resolved?.address,
                                                )
                                            }
                                        }
                                    }
                                } catch (_: RejectedExecutionException) {
                                    // The map left composition while the camera callback was being delivered.
                                }
                            }
                        },
                    )
                }
            },
        )
        mapView.setFinishManually(true)

        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.resume()
                Lifecycle.Event.ON_PAUSE -> mapView.pause()
                Lifecycle.Event.ON_DESTROY -> finishMap()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            mapView.resume()
        }

        onDispose {
            isDisposed.set(true)
            lifecycleOwner.lifecycle.removeObserver(observer)
            requestSerial.incrementAndGet()
            addressRequest?.cancel(true)
            addressExecutor.shutdownNow()
            finishMap()
        }
    }

    AndroidView(
        factory = { mapView },
        modifier = modifier,
    )
}

private class KakaoReverseGeocoder(context: Context) {
    @Suppress("DEPRECATION")
    private val restApiKey = context.packageManager
        .getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
        .metaData
        ?.getString(REST_API_KEY_METADATA_NAME)
        .orEmpty()

    fun resolve(latitude: Double, longitude: Double): ResolvedAddress? {
        if (restApiKey.isBlank()) return null

        val url = URL(
            "https://dapi.kakao.com/v2/local/geo/coord2address.json" +
                "?x=$longitude&y=$latitude&input_coord=WGS84",
        )
        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 5_000
            readTimeout = 5_000
            setRequestProperty("Authorization", "KakaoAK $restApiKey")
        }

        return try {
            if (connection.responseCode !in 200..299) return null

            val body = connection.inputStream.bufferedReader().use { it.readText() }
            val document = JSONObject(body)
                .optJSONArray("documents")
                ?.optJSONObject(0)
                ?: return null
            val roadAddress = document.optJSONObject("road_address")
            val lotAddress = document.optJSONObject("address")
            val address = roadAddress?.optString("address_name").orEmpty()
                .ifBlank { lotAddress?.optString("address_name").orEmpty() }
                .takeIf(String::isNotBlank)
                ?: return null
            val placeName = roadAddress
                ?.optString("building_name")
                .orEmpty()
                .takeIf(String::isNotBlank)

            ResolvedAddress(placeName = placeName, address = address)
        } catch (_: Exception) {
            null
        } finally {
            connection.disconnect()
        }
    }

    private companion object {
        const val REST_API_KEY_METADATA_NAME = "com.joon.ringout.KAKAO_REST_API_KEY"
    }
}

private data class ResolvedAddress(
    val placeName: String?,
    val address: String,
)
