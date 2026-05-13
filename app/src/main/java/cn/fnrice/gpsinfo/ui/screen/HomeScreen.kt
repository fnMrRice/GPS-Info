package cn.fnrice.gpsinfo.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.ExploreOff
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cn.fnrice.gpsinfo.R
import cn.fnrice.gpsinfo.data.SatelliteInfo
import cn.fnrice.gpsinfo.ui.components.*
import cn.fnrice.gpsinfo.viewmodel.GnssViewModel
import androidx.compose.ui.draw.clip

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
                title = stringResource(R.string.sky_view_title),
                isExpandable = true,
                isExpanded = skyViewExpanded,
                onExpandChange = { skyViewExpanded = it },
                icon = Icons.Default.MyLocation,
                headerExtra = {
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
            ) {
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .padding(top = 8.dp)
                    ) {
                        // 在底层显示地图
                        if (state.location != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(8.dp))
                            ) {
                                when (actualMapProvider) {
                                    cn.fnrice.gpsinfo.data.MapProvider.AMAP -> {
                                        AMapViewContainer(state.location?.latitude, state.location?.longitude)
                                    }
                                    cn.fnrice.gpsinfo.data.MapProvider.GOOGLE -> {
                                        GoogleMapViewContainer(state.location?.latitude, state.location?.longitude)
                                    }
                                    else -> {}
                                }
                            }
                        }

                        // 叠加卫星星历图
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