package cn.fnrice.gpsinfo.ui.screen

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cn.fnrice.gpsinfo.R
import cn.fnrice.gpsinfo.data.MapProvider
import cn.fnrice.gpsinfo.ui.components.AMapViewContainer
import cn.fnrice.gpsinfo.ui.components.GoogleMapViewContainer
import cn.fnrice.gpsinfo.viewmodel.GnssViewModel

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
                AMapViewContainer(
                    latitude = state.location?.latitude,
                    longitude = state.location?.longitude,
                    satellites = state.satellites,
                    azimuth = state.azimuth,
                    isDarkTheme = isDarkTheme
                )
            }
            MapProvider.GOOGLE -> {
                GoogleMapViewContainer(
                    latitude = state.location?.latitude,
                    longitude = state.location?.longitude,
                    satellites = state.satellites,
                    azimuth = state.azimuth,
                    isDarkTheme = isDarkTheme
                )
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
