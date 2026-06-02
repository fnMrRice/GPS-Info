package cn.fnrice.gpsinfo.ui.components

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.ExploreOff
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cn.fnrice.gpsinfo.R
import cn.fnrice.gpsinfo.data.GnssState
import cn.fnrice.gpsinfo.data.MapProvider
import cn.fnrice.gpsinfo.data.SatelliteInfo

@Composable
fun SkyViewCard(
    state: GnssState,
    actualMapProvider: MapProvider,
    filteredSatellites: List<SatelliteInfo>,
    skyViewExpanded: Boolean,
    onSkyViewExpandChange: (Boolean) -> Unit,
    isMapInfoExpanded: Boolean,
    onMapInfoExpandChange: (Boolean) -> Unit,
    isCompassEnabled: Boolean,
    onCompassEnabledChange: (Boolean) -> Unit
) {
    AppCard(
        title = stringResource(R.string.sky_view_title),
        isExpandable = true,
        isExpanded = skyViewExpanded,
        onExpandChange = onSkyViewExpandChange,
        icon = Icons.Default.MyLocation,
        headerExtra = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { onMapInfoExpandChange(!isMapInfoExpanded) },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = if (isMapInfoExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                IconButton(
                    onClick = { onCompassEnabledChange(!isCompassEnabled) },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = if (isCompassEnabled) Icons.Default.Explore else Icons.Default.ExploreOff,
                        contentDescription = stringResource(if (isCompassEnabled) R.string.compass_rotate else R.string.compass_north_up),
                        modifier = Modifier.size(18.dp),
                        tint = if (isCompassEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        }
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .padding(top = 8.dp)
            ) {
                // 在底层显示地图
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(8.dp))
                ) {
                    when (actualMapProvider) {
                        MapProvider.AMAP -> {
                            AMapViewContainer(
                                latitude = state.location?.latitude,
                                longitude = state.location?.longitude,
                                satellites = filteredSatellites,
                                azimuth = state.azimuth,
                                rotateWithCompass = isCompassEnabled
                            )
                        }
                        MapProvider.GOOGLE -> {
                            GoogleMapViewContainer(
                                latitude = state.location?.latitude,
                                longitude = state.location?.longitude,
                                satellites = filteredSatellites,
                                azimuth = state.azimuth,
                                rotateWithCompass = isCompassEnabled
                            )
                        }
                        else -> {}
                    }
                }

                // 叠加位置信息 Overlay
                androidx.compose.animation.AnimatedVisibility(
                    visible = isMapInfoExpanded,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Box(
                        modifier = Modifier
                            .padding(8.dp)
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
                                RoundedCornerShape(4.dp)
                            )
                            .padding(8.dp)
                    ) {
                        Column {
                            state.location?.let { loc ->
                                Text(
                                    text = stringResource(R.string.map_info_overlay_latitude, loc.latitude),
                                    style = MaterialTheme.typography.labelSmall
                                )
                                Text(
                                    text = stringResource(R.string.map_info_overlay_longitude, loc.longitude),
                                    style = MaterialTheme.typography.labelSmall
                                )
                                Text(
                                    text = stringResource(R.string.map_info_overlay_speed, loc.speed),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            } ?: Text(
                                text = stringResource(R.string.provider_unknown),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }

                SkyView(
                    satellites = filteredSatellites,
                    azimuth = state.azimuth,
                    rotateWithCompass = isCompassEnabled,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
