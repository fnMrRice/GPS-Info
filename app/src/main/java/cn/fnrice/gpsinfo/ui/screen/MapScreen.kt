package cn.fnrice.gpsinfo.ui.screen

import android.os.Bundle
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
import com.amap.api.maps.MapView
import com.amap.api.maps.model.BitmapDescriptorFactory
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.MarkerOptions
import com.google.maps.android.compose.*

@Composable
fun MapScreen(viewModel: GnssViewModel, innerPadding: PaddingValues) {
    val actualMapProvider by viewModel.actualMapProvider.collectAsState()
    val state by viewModel.state.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
    ) {
        when (actualMapProvider) {
            MapProvider.AMAP -> {
                AMapViewContainer(state.location?.latitude, state.location?.longitude)
            }
            MapProvider.GOOGLE -> {
                GoogleMapViewContainer(state.location?.latitude, state.location?.longitude)
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
fun AMapViewContainer(latitude: Double?, longitude: Double?, modifier: Modifier = Modifier.fillMaxSize()) {
    val context = LocalContext.current
    val mapView = remember { MapView(context) }
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
        factory = { mapView },
        modifier = modifier,
        update = { view ->
            val amap = view.map
            amap.mapType = AMap.MAP_TYPE_SATELLITE
            // 禁用不必要的 UI 控件以保持简洁
            amap.uiSettings.isZoomControlsEnabled = false
            amap.uiSettings.isMyLocationButtonEnabled = false

            if (latitude != null && longitude != null) {
                val latLng = LatLng(latitude, longitude)
                // 移动相机
                amap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                
                // 添加/更新标记
                amap.clear()
                amap.addMarker(
                    MarkerOptions()
                        .position(latLng)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                )
            }
        }
    )
}

@Composable
fun GoogleMapViewContainer(latitude: Double?, longitude: Double?, modifier: Modifier = Modifier.fillMaxSize()) {
    val cameraPositionState = rememberCameraPositionState()

    LaunchedEffect(latitude, longitude) {
        if (latitude != null && longitude != null) {
            cameraPositionState.position = com.google.android.gms.maps.model.CameraPosition.fromLatLngZoom(
                com.google.android.gms.maps.model.LatLng(latitude, longitude),
                15f
            )
        }
    }

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        properties = MapProperties(mapType = MapType.SATELLITE),
        uiSettings = MapUiSettings(zoomControlsEnabled = false, myLocationButtonEnabled = false)
    ) {
        if (latitude != null && longitude != null) {
            Marker(
                state = rememberUpdatedMarkerState(position = com.google.android.gms.maps.model.LatLng(latitude, longitude)),
                title = "Current Location"
            )
        }
    }
}
