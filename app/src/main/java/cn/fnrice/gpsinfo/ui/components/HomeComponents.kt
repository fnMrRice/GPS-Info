package cn.fnrice.gpsinfo.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.ExploreOff
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.math.sqrt
import cn.fnrice.gpsinfo.R
import cn.fnrice.gpsinfo.data.GnssState
import cn.fnrice.gpsinfo.data.MapProvider
import cn.fnrice.gpsinfo.data.SatelliteInfo
import cn.fnrice.gpsinfo.ui.screen.SatelliteFilterStatus

@Composable
fun SensorCard(
    state: GnssState,
    isExpanded: Boolean,
    onExpandChange: (Boolean) -> Unit
) {
    AppCard(
        title = stringResource(R.string.sensor_card_title),
        isExpandable = true,
        isExpanded = isExpanded,
        onExpandChange = onExpandChange,
        icon = Icons.Default.Sensors
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = stringResource(R.string.sensor_azimuth, state.azimuth),
                style = MaterialTheme.typography.bodyMedium
            )

            state.sensorValues.forEach { (type, values) ->
                val text = when (type) {
                    android.hardware.Sensor.TYPE_ACCELEROMETER ->
                        stringResource(R.string.sensor_accelerometer, values[0], values[1], values[2])
                    android.hardware.Sensor.TYPE_GYROSCOPE ->
                        stringResource(R.string.sensor_gyroscope, values[0], values[1], values[2])
                    android.hardware.Sensor.TYPE_MAGNETIC_FIELD ->
                        stringResource(R.string.sensor_magnetic_field, values[0], values[1], values[2])
                    android.hardware.Sensor.TYPE_PRESSURE ->
                        stringResource(R.string.sensor_pressure, values[0])
                    android.hardware.Sensor.TYPE_LIGHT ->
                        stringResource(R.string.sensor_light, values[0])
                    android.hardware.Sensor.TYPE_PROXIMITY ->
                        stringResource(R.string.sensor_proximity, values[0])
                    android.hardware.Sensor.TYPE_GRAVITY ->
                        stringResource(R.string.sensor_gravity, values[0], values[1], values[2])
                    android.hardware.Sensor.TYPE_LINEAR_ACCELERATION ->
                        stringResource(R.string.sensor_linear_acceleration, values[0], values[1], values[2])
                    android.hardware.Sensor.TYPE_ROTATION_VECTOR, android.hardware.Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR -> {
                        val rotationMatrix = FloatArray(9)
                        android.hardware.SensorManager.getRotationMatrixFromVector(rotationMatrix, values)
                        val orientation = FloatArray(3)
                        android.hardware.SensorManager.getOrientation(rotationMatrix, orientation)
                        stringResource(
                            R.string.sensor_orientation,
                            Math.toDegrees(orientation[0].toDouble()).toFloat(),
                            Math.toDegrees(orientation[1].toDouble()).toFloat(),
                            Math.toDegrees(orientation[2].toDouble()).toFloat()
                        )
                    }
                    else -> null
                }
                text?.let {
                    Text(text = it, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

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
                        .graphicsLayer {
                            if (isCompassEnabled) {
                                rotationZ = -state.azimuth
                                // 放大以避免旋转后出现空白角落
                                val scale = sqrt(2f)
                                scaleX = scale
                                scaleY = scale
                            }
                        }
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SatelliteFilterSection(
    isFilterVisible: Boolean,
    onFilterVisibleChange: (Boolean) -> Unit,
    filterStatus: SatelliteFilterStatus,
    onFilterStatusChange: (SatelliteFilterStatus) -> Unit,
    filterConstellation: String?,
    onFilterConstellationChange: (String?) -> Unit,
    constellations: List<String>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onFilterVisibleChange(!isFilterVisible) }
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = if (filterConstellation != null || filterStatus != SatelliteFilterStatus.ALL)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.filter_all),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
            }

            Icon(
                imageVector = if (isFilterVisible) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }

        AnimatedVisibility(
            visible = isFilterVisible,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SatelliteFilterStatus.entries.forEach { status ->
                        FilterChip(
                            selected = filterStatus == status,
                            onClick = { onFilterStatusChange(status) },
                            label = {
                                Text(
                                    when (status) {
                                        SatelliteFilterStatus.ALL -> stringResource(R.string.filter_all)
                                        SatelliteFilterStatus.IN_USE -> stringResource(R.string.filter_in_use)
                                        SatelliteFilterStatus.NOT_USED -> stringResource(R.string.filter_not_used)
                                    }
                                )
                            },
                            leadingIcon = if (filterStatus == status) {
                                {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            } else null
                        )
                    }
                }

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    FilterChip(
                        selected = filterConstellation == null,
                        onClick = { onFilterConstellationChange(null) },
                        label = { Text(stringResource(R.string.constellation_all)) },
                    )
                    constellations.forEach { name ->
                        FilterChip(
                            selected = filterConstellation == name,
                            onClick = { onFilterConstellationChange(name) },
                            label = { Text(name) },
                        )
                    }
                }
            }
        }
    }
}
