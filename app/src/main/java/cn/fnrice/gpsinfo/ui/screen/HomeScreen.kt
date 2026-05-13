package cn.fnrice.gpsinfo.ui.screen

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cn.fnrice.gpsinfo.R
import cn.fnrice.gpsinfo.data.SatelliteInfo
import cn.fnrice.gpsinfo.ui.components.*
import cn.fnrice.gpsinfo.viewmodel.GnssViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: GnssViewModel, innerPadding: PaddingValues) {
    val state by viewModel.state.collectAsState()
    val actualMapProvider by viewModel.actualMapProvider.collectAsState()

    var filterConstellation by remember { mutableStateOf<String?>(null) }
    var filterUsableOnly by remember { mutableStateOf(false) }
    var filterMenuExpanded by remember { mutableStateOf(false) }
    var skyViewExpanded by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val filteredSatellites = remember(state.satellites, filterConstellation, filterUsableOnly, context) {
        val list = state.satellites.filter {
            (filterConstellation == null || it.getConstellationName(context) == filterConstellation) &&
                    (!filterUsableOnly || it.cn0DbHz > 0 || (it.hasBasebandCn0DbHz && it.basebandCn0DbHz > 0))
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
                // Create a dummy SatelliteInfo to get the localized name
                val dummy = SatelliteInfo(svid = 0, constellationType = type, cn0DbHz = 0f, elevationDegrees = 0f, azimuthDegrees = 0f, usedInFix = false)
                dummy.getConstellationName(context)
            }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 12.dp),
    ) {
        item {
            StatusHeader(state, actualMapProvider)
        }

        if (state.location != null) {
            item {
                LocationCard(state)
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            stringResource(R.string.sky_view_title),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                        IconButton(onClick = { skyViewExpanded = !skyViewExpanded }) {
                            Icon(
                                imageVector = if (skyViewExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null
                            )
                        }
                    }
                    if (skyViewExpanded) {
                        SkyView(
                            satellites = filteredSatellites,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .padding(8.dp)
                        )
                    }
                }
            }
        }

        if (constellations.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .horizontalScroll(rememberScrollState()),
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

                    Box {
                        IconButton(onClick = { filterMenuExpanded = true }) {
                            Icon(Icons.Default.FilterList, contentDescription = null)
                        }
                        DropdownMenu(
                            expanded = filterMenuExpanded,
                            onDismissRequest = { filterMenuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        androidx.compose.material3.Checkbox(
                                            checked = filterUsableOnly,
                                            onCheckedChange = null
                                        )
                                        Text(stringResource(R.string.filter_usable))
                                    }
                                },
                                onClick = {
                                    filterUsableOnly = !filterUsableOnly
                                    filterMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        items(filteredSatellites, key = { "${it.constellationType}-${it.svid}" }) { sat ->
            SatelliteCard(sat)
        }

        if (filteredSatellites.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        stringResource(R.string.no_satellites_detected),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
