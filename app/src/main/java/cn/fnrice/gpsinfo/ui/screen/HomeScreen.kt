package cn.fnrice.gpsinfo.ui.screen

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cn.fnrice.gpsinfo.R
import cn.fnrice.gpsinfo.data.SatelliteGroup
import cn.fnrice.gpsinfo.data.SatelliteInfo
import cn.fnrice.gpsinfo.ui.components.LocationCard
import cn.fnrice.gpsinfo.ui.components.SatelliteCard
import cn.fnrice.gpsinfo.ui.components.SatelliteFilterSection
import cn.fnrice.gpsinfo.ui.components.SensorCard
import cn.fnrice.gpsinfo.ui.components.SkyViewCard
import cn.fnrice.gpsinfo.ui.components.StatusHeader
import cn.fnrice.gpsinfo.viewmodel.GnssViewModel
import kotlinx.coroutines.delay

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
    var showSearchHint by remember { mutableStateOf(false) }

    // 搜星超时提示：30秒后如果仍无卫星数据，显示提示
    LaunchedEffect(state.satellitesTotal) {
        if (state.satellitesTotal == 0) {
            showSearchHint = false
            delay(30_000L)
            showSearchHint = true
        } else {
            showSearchHint = false
        }
    }

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

    // 按 (constellationType, svid) 分组，同一颗卫星多频段信号合并显示
    val satelliteGroups = remember(filteredSatellites, filterStatus, context) {
        val groups = filteredSatellites
            .groupBy { "${it.constellationType}-${it.svid}" }
            .map { (_, entries) -> SatelliteGroup(entries[0].svid, entries[0].constellationType, entries) }

        groups.filter { group ->
            when (filterStatus) {
                SatelliteFilterStatus.ALL -> true
                SatelliteFilterStatus.IN_USE -> group.usedInFix
                SatelliteFilterStatus.NOT_USED -> !group.usedInFix
            }
        }.sortedWith(compareBy<SatelliteGroup> {
            val country = java.util.Locale.getDefault().country
            val isChinaRegion = country == "CN" || country == "HK" || country == "MO"
            when (it.constellationType) {
                android.location.GnssStatus.CONSTELLATION_BEIDOU -> if (isChinaRegion) 0 else 2
                android.location.GnssStatus.CONSTELLATION_GPS -> 1
                android.location.GnssStatus.CONSTELLATION_GALILEO -> if (isChinaRegion) 2 else 0
                else -> 3
            }
        }.thenBy { it.svid })
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
            StatusHeader(
                state = state,
                mapProvider = actualMapProvider,
                onGpsDisabledClick = {
                    context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }
            )
        }

        item {
            LocationCard(state)
        }

        item {
            SensorCard(
                state = state,
                isExpanded = sensorCardExpanded,
                onExpandChange = { sensorCardExpanded = it }
            )
        }

        item {
            SkyViewCard(
                state = state,
                actualMapProvider = actualMapProvider,
                filteredSatellites = filteredSatellites,
                skyViewExpanded = skyViewExpanded,
                onSkyViewExpandChange = { skyViewExpanded = it },
                isMapInfoExpanded = isMapInfoExpanded,
                onMapInfoExpandChange = { isMapInfoExpanded = it },
                isCompassEnabled = isCompassEnabled,
                onCompassEnabledChange = { isCompassEnabled = it }
            )
        }

        item {
            SatelliteFilterSection(
                isFilterVisible = isFilterVisible,
                onFilterVisibleChange = { isFilterVisible = it },
                filterStatus = filterStatus,
                onFilterStatusChange = { filterStatus = it },
                filterConstellation = filterConstellation,
                onFilterConstellationChange = { filterConstellation = it },
                constellations = constellations
            )
        }

        if (satelliteGroups.isEmpty()) {
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
                        if (showSearchHint) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(R.string.satellite_search_timeout_hint),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        } else {
            items(satelliteGroups, key = { "${it.constellationType}-${it.svid}" }) { group ->
                SatelliteCard(group)
            }
        }
    }
}