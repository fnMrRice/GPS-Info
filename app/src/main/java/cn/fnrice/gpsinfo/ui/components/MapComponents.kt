package cn.fnrice.gpsinfo.ui.components

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Bundle
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.createBitmap
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import cn.fnrice.gpsinfo.data.SatelliteInfo
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.MapView
import com.amap.api.maps.MapsInitializer
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.MarkerOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberUpdatedMarkerState
import kotlin.math.cos
import kotlin.math.sin
import com.amap.api.maps.model.BitmapDescriptorFactory as AMapBitmapDescriptorFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory as GoogleBitmapDescriptorFactory
import com.google.android.gms.maps.model.MapStyleOptions as GoogleMapStyleOptions

@Composable
fun AMapViewContainer(
    modifier: Modifier = Modifier,
    latitude: Double?,
    longitude: Double?,
    satellites: List<SatelliteInfo> = emptyList(),
    azimuth: Float = 0f,
    rotateWithCompass: Boolean = false,
    isDarkTheme: Boolean = isSystemInDarkTheme(),
) {
    val context = LocalContext.current
    val mapView = remember {
        MapsInitializer.updatePrivacyShow(context, true, true)
        MapsInitializer.updatePrivacyAgree(context, true)
        MapView(context)
    }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val markerCache = remember { mutableMapOf<String, com.amap.api.maps.model.Marker>() }
    val bitmapCache = remember { mutableMapOf<String, com.amap.api.maps.model.BitmapDescriptor>() }

    DisposableEffect(lifecycle) {
        // Sync with current lifecycle state first (observer only catches future events)
        val currentState = lifecycle.currentState
        if (currentState.isAtLeast(Lifecycle.State.INITIALIZED)) {
            mapView.onCreate(Bundle())
        }
        if (currentState.isAtLeast(Lifecycle.State.STARTED)) {
            mapView.onResume()
        }

        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_CREATE -> mapView.onCreate(Bundle())
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                else -> {}
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
            mapView.onPause()
            mapView.onDestroy()
        }
    }

    AndroidView(
        factory = { mapView },
        modifier = modifier,
        update = { view ->
            try {
                val amap = view.map ?: return@AndroidView
                amap.mapType = if (isDarkTheme) AMap.MAP_TYPE_NIGHT else AMap.MAP_TYPE_NORMAL

                amap.uiSettings.apply {
                    isZoomControlsEnabled = false
                    isMyLocationButtonEnabled = false
                    setAllGesturesEnabled(false)
                }

                if (latitude != null && longitude != null) {
                    val latLng = LatLng(latitude, longitude)
                    amap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 4f))

                    val currentSatIds = satellites.map { "${it.constellationType}-${it.svid}" }.toSet()

                    val iterator = markerCache.entries.iterator()
                    while (iterator.hasNext()) {
                        val entry = iterator.next()
                        if (entry.key !in currentSatIds) {
                            entry.value.remove()
                            iterator.remove()
                        }
                    }

                    satellites.forEach { sat ->
                        val satId = "${sat.constellationType}-${sat.svid}"
                        val azimRad =
                            Math.toRadians((sat.azimuthDegrees + if (rotateWithCompass) -azimuth else 0f).toDouble())
                        val distance = (1 - sat.elevationDegrees / 90f) * 10.0
                        val dLat = distance * cos(azimRad)
                        val dLng = distance * sin(azimRad)
                        val satLatLng = LatLng(latitude + dLat, longitude + dLng)

                        val satColor = getConstellationColor(sat.getConstellationName(context)).toArgb()
                        val bitmapKey = "${sat.svid}-${satColor}-${sat.usedInFix}"
                        val bitmapDescriptor = bitmapCache.getOrPut(bitmapKey) {
                            AMapBitmapDescriptorFactory.fromBitmap(
                                createSatelliteBitmap(
                                    sat.svid.toString(),
                                    satColor,
                                    sat.usedInFix
                                )
                            )
                        }

                        val existingMarker = markerCache[satId]
                        if (existingMarker != null) {
                            existingMarker.position = satLatLng
                            existingMarker.setIcon(bitmapDescriptor)
                        } else {
                            val markerOptions = MarkerOptions()
                                .position(satLatLng)
                                .anchor(0.5f, 0.5f)
                                .title(sat.svid.toString())
                                .icon(bitmapDescriptor)

                            val newMarker = amap.addMarker(markerOptions)
                            markerCache[satId] = newMarker
                        }
                    }
                } else {
                    markerCache.values.forEach { it.remove() }
                    markerCache.clear()
                }
            } catch (e: Exception) {
                // AMap native layer may throw after onDestroy
            }
        }
    )
}

@Composable
fun GoogleMapViewContainer(
    modifier: Modifier = Modifier,
    latitude: Double?,
    longitude: Double?,
    satellites: List<SatelliteInfo> = emptyList(),
    azimuth: Float = 0f,
    rotateWithCompass: Boolean = false,
    isDarkTheme: Boolean = isSystemInDarkTheme(),
) {
    val cameraPositionState = rememberCameraPositionState()
    val googleBitmapCache =
        remember { mutableMapOf<String, com.google.android.gms.maps.model.BitmapDescriptor>() }
    val context = LocalContext.current

    LaunchedEffect(latitude, longitude) {
        if (latitude != null && longitude != null) {
            cameraPositionState.position =
                com.google.android.gms.maps.model.CameraPosition.fromLatLngZoom(
                    com.google.android.gms.maps.model.LatLng(latitude, longitude),
                    4f
                )
        }
    }

    val googleMapStyleOptions = if (isDarkTheme) {
        GoogleMapStyleOptions(googleMapsNightStyle)
    } else null

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        properties = MapProperties(
            mapStyleOptions = googleMapStyleOptions,
            mapType = MapType.NORMAL
        ),
        uiSettings = MapUiSettings(
            zoomControlsEnabled = false,
            compassEnabled = false,
            myLocationButtonEnabled = false,
            scrollGesturesEnabled = false,
            zoomGesturesEnabled = false,
            tiltGesturesEnabled = false,
            rotationGesturesEnabled = false
        )
    ) {
        if (latitude != null && longitude != null) {
            satellites.forEach { sat ->
                val satId = "${sat.constellationType}-${sat.svid}"
                val azimRad =
                    Math.toRadians((sat.azimuthDegrees + if (rotateWithCompass) -azimuth else 0f).toDouble())
                val distance = (1 - sat.elevationDegrees / 90f) * 10.0
                val dLat = distance * cos(azimRad)
                val dLng = distance * sin(azimRad)
                val satLatLng =
                    com.google.android.gms.maps.model.LatLng(latitude + dLat, longitude + dLng)

                val satColor = getConstellationColor(sat.getConstellationName(context)).toArgb()
                val bitmapKey = "${sat.svid}-${satColor}-${sat.usedInFix}"
                val bitmapDescriptor = googleBitmapCache.getOrPut(bitmapKey) {
                    GoogleBitmapDescriptorFactory.fromBitmap(
                        createSatelliteBitmap(
                            sat.svid.toString(),
                            satColor,
                            sat.usedInFix
                        )
                    )
                }

                key(satId) {
                    Marker(
                        state = rememberUpdatedMarkerState(position = satLatLng),
                        anchor = androidx.compose.ui.geometry.Offset(0.5f, 0.5f),
                        title = sat.svid.toString(),
                        icon = bitmapDescriptor
                    )
                }
            }
        }
    }
}

private fun createSatelliteBitmap(text: String, color: Int, usedInFix: Boolean): Bitmap {
    val size = if (usedInFix) 40 else 30
    val bitmap = createBitmap(size, size)
    val canvas = Canvas(bitmap)
    val paint = Paint().apply {
        isAntiAlias = true
        this.color = color
    }

    canvas.drawCircle(size / 2f, size / 2f, size / 3.5f, paint)
    if (usedInFix) {
        paint.alpha = 100
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint)
    }

    paint.apply {
        this.color = android.graphics.Color.WHITE
        textSize = 18f
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
        alpha = 255
    }
    canvas.drawText(text, size / 2f, size / 2f + 7f, paint)

    return bitmap
}

private val googleMapsNightStyle = """
[
  {
    "elementType": "geometry",
    "stylers": [
      {
        "color": "#242f3e"
      }
    ]
  },
  {
    "elementType": "labels.text.fill",
    "stylers": [
      {
        "color": "#746855"
      }
    ]
  },
  {
    "elementType": "labels.text.stroke",
    "stylers": [
      {
        "color": "#242f3e"
      }
    ]
  },
  {
    "featureType": "administrative.locality",
    "elementType": "labels.text.fill",
    "stylers": [
      {
        "color": "#d59563"
      }
    ]
  },
  {
    "featureType": "poi",
    "elementType": "labels.text.fill",
    "stylers": [
      {
        "color": "#d59563"
      }
    ]
  },
  {
    "featureType": "poi.park",
    "elementType": "geometry",
    "stylers": [
      {
        "color": "#263c3f"
      }
    ]
  },
  {
    "featureType": "poi.park",
    "elementType": "labels.text.fill",
    "stylers": [
      {
        "color": "#6b9a76"
      }
    ]
  },
  {
    "featureType": "road",
    "elementType": "geometry",
    "stylers": [
      {
        "color": "#38414e"
      }
    ]
  },
  {
    "featureType": "road",
    "elementType": "geometry.stroke",
    "stylers": [
      {
        "color": "#212a37"
      }
    ]
  },
  {
    "featureType": "road",
    "elementType": "labels.text.fill",
    "stylers": [
      {
        "color": "#9ca5b3"
      }
    ]
  },
  {
    "featureType": "road.highway",
    "elementType": "geometry",
    "stylers": [
      {
        "color": "#746855"
      }
    ]
  },
  {
    "featureType": "road.highway",
    "elementType": "geometry.stroke",
    "stylers": [
      {
        "color": "#1f2835"
      }
    ]
  },
  {
    "featureType": "road.highway",
    "elementType": "labels.text.fill",
    "stylers": [
      {
        "color": "#f3d19c"
      }
    ]
  },
  {
    "featureType": "transit",
    "elementType": "geometry",
    "stylers": [
      {
        "color": "#2f3948"
      }
    ]
  },
  {
    "featureType": "transit.station",
    "elementType": "labels.text.fill",
    "stylers": [
      {
        "color": "#d59563"
      }
    ]
  },
  {
    "featureType": "water",
    "elementType": "geometry",
    "stylers": [
      {
        "color": "#17263c"
      }
    ]
  },
  {
    "featureType": "water",
    "elementType": "labels.text.fill",
    "stylers": [
      {
        "color": "#515c6d"
      }
    ]
  },
  {
    "featureType": "water",
    "elementType": "labels.text.stroke",
    "stylers": [
      {
        "color": "#17263c"
      }
    ]
  }
]
"""