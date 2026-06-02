package cn.fnrice.gpsinfo.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cn.fnrice.gpsinfo.R
import cn.fnrice.gpsinfo.ui.screen.SatelliteFilterStatus

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
