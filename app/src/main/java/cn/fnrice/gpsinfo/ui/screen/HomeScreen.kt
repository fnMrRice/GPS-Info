package cn.fnrice.gpsinfo.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.foundation.background
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cn.fnrice.gpsinfo.R
import cn.fnrice.gpsinfo.data.SatelliteInfo
import cn.fnrice.gpsinfo.ui.components.*
import cn.fnrice.gpsinfo.viewmodel.GnssViewModel

enum class SatelliteFilterStatus {
    ALL,
    IN_USE,
    NOT_USED
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(viewModel: GnssViewModel, innerPadding: PaddingValues) {
    val state by viewModel.state.collectAsState()
    var filterConstellation by remember { mutableStateOf<String?>(null) }
    var filterStatus by remember { mutableStateOf(SatelliteFilterStatus.ALL) }
    var skyViewExpanded by remember { mutableStateOf(false) }
    var isFilterVisible by remember { mutableStateOf(false) }
    var isCompassEnabled by remember { mutableStateOf(false) }
    var isMapInfoExpanded by remember { mutableStateOf(false) }
    var sensorCardExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val actualMapProvider by viewModel.actualMapProvider.collectAsState()
    val filteredSatellites = remember(state.satellites, filterConstellation, filterStatus, context) {
        val list = state.satellites.filter {
            val matchesConstellation = filterConstellation == null || it.getConstellationName(context) == filterConstellation
            val matchesStatus = when (filterStatus) {
                SatelliteFilterStatus.ALL -> true
                SatelliteFilterStatus.IN_USE -> it.usedInFix
                SatelliteFilterStatus.NOT_USED -> !it.usedInFix
            }
            matchesConstellation && matchesStatus
        }
        
        // 排序逻辑
        val country = java.util.Locale.getDefault().country
        val isChinaRegion = country == "CN" || country == "HK" || country == "MO"
        
        list.sortedWith(compareBy<SatelliteInfo> {
            val priority = when (it.constellationType) {
                android.location.GnssStatus.CONSTELLATION_BEIDOU -> if (isChinaRegion) 0 else 2
                android.location.GnssStatus.CONSTELLATION_GPS -> 1
                android.location.GnssStatus.CONSTELLATION_GALILEO -> if (isChinaRegion) 2 else 0
                else -> 3
            }
            priority
        }.thenBy { it.svid })
    }
    
    val constellations = remember(state.satellites, context) {
        val country = java.util.Locale.getDefault().country
        val isChinaRegion = country == "CN" || country == "HK" || country == "MO"
        
        state.satellites
            .map { it.constellationType }
            .distinct()
            .sortedWith(compareBy { type ->
                when (type) {
                    android.location.GnssStatus.CONSTELLATION_BEIDOU -> if (isChinaRegion) 0 else 2
                    android.location.GnssStatus.CONSTELLATION_GPS -> 1
                    android.location.GnssStatus.CONSTELLATION_GALILEO -> if (isChinaRegion) 2 else 0
                    else -> 3
                }
            })
            .map { type ->
                SatelliteInfo.getConstellationNameStatic(context, type)
            }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 8.dp),
    ) {
        item {
            StatusHeader(state, actualMapProvider)
        }

        item {
            LocationCard(state)
        }

        item {
            AppCard(
                title = stringResource(R.string.sensor_card_title),
                isExpandable = true,
                isExpanded = sensorCardExpanded,
                onExpandChange = { sensorCardExpanded = it },
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

        item {
            AppCard(
                title = stringResource(R.string.sky_view_title),
                isExpandable = true,
                isExpanded = skyViewExpanded,
                onExpandChange = { skyViewExpanded = it },
                icon = Icons.Default.MyLocation,
                headerExtra = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { isMapInfoExpanded = !isMapInfoExpanded },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = if (isMapInfoExpanded) Icons.Default.Info else Icons.Default.Info,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = if (isMapInfoExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        IconButton(
                            onClick = { isCompassEnabled = !isCompassEnabled },
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
                                cn.fnrice.gpsinfo.data.MapProvider.AMAP -> {
                                    AMapViewContainer(
                                        latitude = state.location?.latitude,
                                        longitude = state.location?.longitude,
                                        satellites = filteredSatellites,
                                        azimuth = state.azimuth,
                                        rotateWithCompass = isCompassEnabled
                                    )
                                }
                                cn.fnrice.gpsinfo.data.MapProvider.GOOGLE -> {
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

                        // 叠加卫星星历图 (保留原有的 SkyView 也可以，或者用户指的是不再需要叠加而是直接用地图 API)
                        // 根据需求“卫星标最好直接用地图api在地图上显示出来”，我们已经把卫星画在地图上了。
                        // 这里我们暂时保留 SkyView，但可以给它一个开关，或者根据用户指示移除。
                        // 目前我选择保留它，因为地图层级很低（3f），SkyView 提供了一个更清晰的相对关系。
                        // 如果用户明确要求移除叠加，再移除。
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

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isFilterVisible = !isFilterVisible }
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
                                    onClick = { filterStatus = status },
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
                                onClick = { filterConstellation = null },
                                label = { Text(stringResource(R.string.constellation_all)) },
                            )
                            constellations.forEach { name ->
                                FilterChip(
                                    selected = filterConstellation == name,
                                    onClick = { filterConstellation = name },
                                    label = { Text(name) },
                                )
                            }
                        }
                    }
                }
            }
        }

        if (filteredSatellites.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(32.dp),
                            strokeWidth = 3.dp,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.waiting_for_satellites),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(filteredSatellites, key = { "${it.constellationType}-${it.svid}" }) { sat ->
                SatelliteCard(sat)
            }
        }
    }
}