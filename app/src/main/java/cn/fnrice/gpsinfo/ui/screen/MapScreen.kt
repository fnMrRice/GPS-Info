package cn.fnrice.gpsinfo.ui.screen

import android.os.Bundle
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import cn.fnrice.gpsinfo.R
import cn.fnrice.gpsinfo.data.MapProvider
import cn.fnrice.gpsinfo.viewmodel.GnssViewModel
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.MapsInitializer
import com.amap.api.maps.MapView
import com.amap.api.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions as GoogleMapStyleOptions
import com.google.maps.android.compose.*

@Composable
fun MapScreen(viewModel: GnssViewModel, innerPadding: PaddingValues) {
    val actualMapProvider by viewModel.actualMapProvider.collectAsState()
    val state by viewModel.state.collectAsState()
    val isDarkTheme = isSystemInDarkTheme()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
    ) {
        when (actualMapProvider) {
            MapProvider.AMAP -> {
                AMapViewContainer(state.location?.latitude, state.location?.longitude, isDarkTheme = isDarkTheme)
            }
            MapProvider.GOOGLE -> {
                GoogleMapViewContainer(state.location?.latitude, state.location?.longitude, isDarkTheme = isDarkTheme)
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = stringResource(R.string.current_provider, actualMapProvider.displayName),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = stringResource(R.string.map_integration_soon),
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AMapViewContainer(
    latitude: Double?,
    longitude: Double?,
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    modifier: Modifier = Modifier.fillMaxSize()
) {
    val context = LocalContext.current
    val mapView = remember {
        // 在构造 MapView 之前进行合规检查
        MapsInitializer.updatePrivacyShow(context, true, true)
        MapsInitializer.updatePrivacyAgree(context, true)
        MapView(context)
    }
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    // 映射生命周期
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
        factory = { 
            mapView
        },
        modifier = modifier,
        update = { view ->
            val amap = view.map
            amap.mapType = if (isDarkTheme) AMap.MAP_TYPE_NIGHT else AMap.MAP_TYPE_NORMAL
            // 禁用不必要的 UI 控件以保持简洁
            amap.uiSettings.isZoomControlsEnabled = false
            amap.uiSettings.isMyLocationButtonEnabled = false

            if (latitude != null && longitude != null) {
                val latLng = LatLng(latitude, longitude)
                // 移动相机
                amap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 3f))
            }
        }
    )
}

@Composable
fun GoogleMapViewContainer(
    latitude: Double?,
    longitude: Double?,
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
        GoogleMapStyleOptions(
            "[\n" +
                    "  {\n" +
                    "    \"elementType\": \"geometry\",\n" +
                    "    \"stylers\": [\n" +
                    "      {\n" +
                    "        \"color\": \"#242f3e\"\n" +
                    "      }\n" +
                    "    ]\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"elementType\": \"labels.text.fill\",\n" +
                    "    \"stylers\": [\n" +
                    "      {\n" +
                    "        \"color\": \"#746855\"\n" +
                    "      }\n" +
                    "    ]\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"elementType\": \"labels.text.stroke\",\n" +
                    "    \"stylers\": [\n" +
                    "      {\n" +
                    "        \"color\": \"#242f3e\"\n" +
                    "      }\n" +
                    "    ]\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"featureType\": \"administrative.locality\",\n" +
                    "    \"elementType\": \"labels.text.fill\",\n" +
                    "    \"stylers\": [\n" +
                    "      {\n" +
                    "        \"color\": \"#d59563\"\n" +
                    "      }\n" +
                    "    ]\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"featureType\": \"poi\",\n" +
                    "    \"elementType\": \"labels.text.fill\",\n" +
                    "    \"stylers\": [\n" +
                    "      {\n" +
                    "        \"color\": \"#d59563\"\n" +
                    "      }\n" +
                    "    ]\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"featureType\": \"poi.park\",\n" +
                    "    \"elementType\": \"geometry\",\n" +
                    "    \"stylers\": [\n" +
                    "      {\n" +
                    "        \"color\": \"#263c3f\"\n" +
                    "      }\n" +
                    "    ]\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"featureType\": \"poi.park\",\n" +
                    "    \"elementType\": \"labels.text.fill\",\n" +
                    "    \"stylers\": [\n" +
                    "      {\n" +
                    "        \"color\": \"#6b9a76\"\n" +
                    "      }\n" +
                    "    ]\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"featureType\": \"road\",\n" +
                    "    \"elementType\": \"geometry\",\n" +
                    "    \"stylers\": [\n" +
                    "      {\n" +
                    "        \"color\": \"#38414e\"\n" +
                    "      }\n" +
                    "    ]\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"featureType\": \"road\",\n" +
                    "    \"elementType\": \"geometry.stroke\",\n" +
                    "    \"stylers\": [\n" +
                    "      {\n" +
                    "        \"color\": \"#212a37\"\n" +
                    "      }\n" +
                    "    ]\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"featureType\": \"road\",\n" +
                    "    \"elementType\": \"labels.text.fill\",\n" +
                    "    \"stylers\": [\n" +
                    "      {\n" +
                    "        \"color\": \"#9ca5b3\"\n" +
                    "      }\n" +
                    "    ]\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"featureType\": \"road.highway\",\n" +
                    "    \"elementType\": \"geometry\",\n" +
                    "    \"stylers\": [\n" +
                    "      {\n" +
                    "        \"color\": \"#746855\"\n" +
                    "      }\n" +
                    "    ]\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"featureType\": \"road.highway\",\n" +
                    "    \"elementType\": \"geometry.stroke\",\n" +
                    "    \"stylers\": [\n" +
                    "      {\n" +
                    "        \"color\": \"#1f2835\"\n" +
                    "      }\n" +
                    "    ]\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"featureType\": \"road.highway\",\n" +
                    "    \"elementType\": \"labels.text.fill\",\n" +
                    "    \"stylers\": [\n" +
                    "      {\n" +
                    "        \"color\": \"#f3d19c\"\n" +
                    "      }\n" +
                    "    ]\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"featureType\": \"transit\",\n" +
                    "    \"elementType\": \"geometry\",\n" +
                    "    \"stylers\": [\n" +
                    "      {\n" +
                    "        \"color\": \"#2f3948\"\n" +
                    "      }\n" +
                    "    ]\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"featureType\": \"transit.station\",\n" +
                    "    \"elementType\": \"labels.text.fill\",\n" +
                    "    \"stylers\": [\n" +
                    "      {\n" +
                    "        \"color\": \"#d59563\"\n" +
                    "      }\n" +
                    "    ]\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"featureType\": \"water\",\n" +
                    "    \"elementType\": \"geometry\",\n" +
                    "    \"stylers\": [\n" +
                    "      {\n" +
                    "        \"color\": \"#17263c\"\n" +
                    "      }\n" +
                    "    ]\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"featureType\": \"water\",\n" +
                    "    \"elementType\": \"labels.text.fill\",\n" +
                    "    \"stylers\": [\n" +
                    "      {\n" +
                    "        \"color\": \"#515c6d\"\n" +
                    "      }\n" +
                    "    ]\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"featureType\": \"water\",\n" +
                    "    \"elementType\": \"labels.text.stroke\",\n" +
                    "    \"stylers\": [\n" +
                    "      {\n" +
                    "        \"color\": \"#17263c\"\n" +
                    "      }\n" +
                    "    ]\n" +
                    "  }\n" +
                    "]"
        )
    } else null

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        properties = MapProperties(
            mapType = MapType.NORMAL,
            mapStyleOptions = googleMapStyleOptions
        ),
        uiSettings = MapUiSettings(zoomControlsEnabled = false, myLocationButtonEnabled = false)
    ) {
    }
}
