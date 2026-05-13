package cn.fnrice.gpsinfo.ui.components

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Bundle
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import cn.fnrice.gpsinfo.data.SatelliteInfo
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.MapsInitializer
import com.amap.api.maps.MapView
import com.google.android.gms.maps.model.BitmapDescriptorFactory as GoogleBitmapDescriptorFactory
import com.amap.api.maps.model.BitmapDescriptorFactory as AMapBitmapDescriptorFactory
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.MarkerOptions
import com.google.android.gms.maps.model.MapStyleOptions as GoogleMapStyleOptions
import com.google.maps.android.compose.*
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AMapViewContainer(
    latitude: Double?,
    longitude: Double?,
    satellites: List<SatelliteInfo> = emptyList(),
    azimuth: Float = 0f,
    rotateWithCompass: Boolean = false,
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    modifier: Modifier = Modifier.fillMaxSize()
) {
    val context = LocalContext.current
    val mapView = remember {
        MapsInitializer.updatePrivacyShow(context, true, true)
        MapsInitializer.updatePrivacyAgree(context, true)
        MapView(context)
    }
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_CREATE -> mapView.onCreate(Bundle())
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> {}
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
            mapView.onDestroy()
        }
    }

    AndroidView(
        factory = { mapView },
        modifier = modifier,
        update = { view ->
            val amap = view.map
            amap.mapType = if (isDarkTheme) AMap.MAP_TYPE_NIGHT else AMap.MAP_TYPE_NORMAL
            
            // 禁用手势和 UI 控件
            amap.uiSettings.apply {
                isZoomControlsEnabled = false
                isMyLocationButtonEnabled = false
                setAllGesturesEnabled(false) // 使用正确的 setter 方法
            }

            if (latitude != null && longitude != null) {
                val latLng = LatLng(latitude, longitude)
                amap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 3f))
                
                amap.clear()
                satellites.forEach { sat ->
                    val azimRad = Math.toRadians((sat.azimuthDegrees + if (rotateWithCompass) -azimuth else 0f).toDouble())
                    val distance = (1 - sat.elevationDegrees / 90f) * 10.0 
                    val dLat = distance * cos(azimRad)
                    val dLng = distance * sin(azimRad)
                    
                    val satLatLng = LatLng(latitude + dLat, longitude + dLng)
                    val satColor = getConstellationColor(sat.getConstellationName(context)).toArgb()
                    
                    val markerOptions = MarkerOptions()
                        .position(satLatLng)
                        .anchor(0.5f, 0.5f)
                        .title(sat.svid.toString())
                        .icon(AMapBitmapDescriptorFactory.fromBitmap(createSatelliteBitmap(sat.svid.toString(), satColor, sat.usedInFix)))
                    
                    amap.addMarker(markerOptions)
                }
            }
        }
    )
}

@Composable
fun GoogleMapViewContainer(
    latitude: Double?,
    longitude: Double?,
    satellites: List<SatelliteInfo> = emptyList(),
    azimuth: Float = 0f,
    rotateWithCompass: Boolean = false,
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    modifier: Modifier = Modifier.fillMaxSize()
) {
    val cameraPositionState = rememberCameraPositionState()

    LaunchedEffect(latitude, longitude) {
        if (latitude != null && longitude != null) {
            cameraPositionState.position = com.google.android.gms.maps.model.CameraPosition.fromLatLngZoom(
                com.google.android.gms.maps.model.LatLng(latitude, longitude),
                3f
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
                val azimRad = Math.toRadians((sat.azimuthDegrees + if (rotateWithCompass) -azimuth else 0f).toDouble())
                val distance = (1 - sat.elevationDegrees / 90f) * 10.0
                val dLat = distance * cos(azimRad)
                val dLng = distance * sin(azimRad)
                val satLatLng = com.google.android.gms.maps.model.LatLng(latitude + dLat, longitude + dLng)
                val context = LocalContext.current
                val satColor = getConstellationColor(sat.getConstellationName(context)).toArgb()
                
                Marker(
                    state = rememberMarkerState(position = satLatLng),
                    anchor = androidx.compose.ui.geometry.Offset(0.5f, 0.5f),
                    title = sat.svid.toString(),
                    icon = GoogleBitmapDescriptorFactory.fromBitmap(createSatelliteBitmap(sat.svid.toString(), satColor, sat.usedInFix))
                )
            }
        }
    }
}

private fun createSatelliteBitmap(text: String, color: Int, usedInFix: Boolean): Bitmap {
    val size = if (usedInFix) 40 else 30
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
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