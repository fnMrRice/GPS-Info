package cn.fnrice.gpsinfo.ui.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import cn.fnrice.gpsinfo.data.SatelliteInfo
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberUpdatedMarkerState
import kotlin.math.cos
import kotlin.math.sin
import com.google.android.gms.maps.model.BitmapDescriptorFactory as GoogleBitmapDescriptorFactory
import com.google.android.gms.maps.model.MapStyleOptions as GoogleMapStyleOptions

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

    LaunchedEffect(latitude, longitude, rotateWithCompass, azimuth) {
        if (latitude != null && longitude != null) {
            val bearing = if (rotateWithCompass) -azimuth else 0f
            cameraPositionState.position =
                com.google.android.gms.maps.model.CameraPosition(
                    com.google.android.gms.maps.model.LatLng(latitude, longitude),
                    4f,
                    0f,
                    bearing
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
                val azimRad = Math.toRadians(sat.azimuthDegrees.toDouble())
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
