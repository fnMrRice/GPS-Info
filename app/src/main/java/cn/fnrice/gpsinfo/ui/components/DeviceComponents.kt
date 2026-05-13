package cn.fnrice.gpsinfo.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DeveloperMode
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cn.fnrice.gpsinfo.R
import cn.fnrice.gpsinfo.viewmodel.GnssCapabilitiesInfo
import cn.fnrice.gpsinfo.viewmodel.SensorCapabilitiesInfo

@Composable
fun DeviceDetailCard(
    title: String,
    icon: ImageVector,
    isExpandable: Boolean = false,
    isExpanded: Boolean = false,
    onExpandChange: (Boolean) -> Unit = {},
    content: @Composable ColumnScope.() -> Unit
) {
    val rotation by animateFloatAsState(targetValue = if (isExpanded) 180f else 0f, label = "rotation")

    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = SolidColor(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
        ),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.animateContentSize(animationSpec = spring(stiffness = Spring.StiffnessLow))) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (isExpandable) Modifier.clickable { onExpandChange(!isExpanded) } else Modifier)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                if (isExpandable) {
                    Icon(
                        imageVector = Icons.Default.ExpandMore,
                        contentDescription = null,
                        modifier = Modifier
                            .size(24.dp)
                            .rotate(rotation),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (isExpandable) {
                AnimatedVisibility(
                    visible = isExpanded,
                    enter = expandVertically(animationSpec = spring(stiffness = Spring.StiffnessLow)) + fadeIn(),
                    exit = shrinkVertically(animationSpec = spring(stiffness = Spring.StiffnessLow)) + fadeOut()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 16.dp)
                    ) {
                        HorizontalDivider(
                            modifier = Modifier.padding(bottom = 12.dp),
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                        )
                        content()
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp)
                ) {
                    content()
                }
            }
        }
    }
}

@Composable
fun DeviceInfoCard(onVersionClick: () -> Unit) {
    val context = LocalContext.current
    var isExpanded by rememberSaveable { mutableStateOf(false) }
    val packageInfo = remember(context) {
        try {
            context.packageManager.getPackageInfo(context.packageName, 0)
        } catch (_: Exception) {
            null
        }
    }
    val versionName = packageInfo?.versionName ?: "1.0.0"

    DeviceDetailCard(
        title = stringResource(R.string.device_info),
        icon = Icons.Default.DeveloperMode,
        isExpandable = true,
        isExpanded = isExpanded,
        onExpandChange = { isExpanded = it }
    ) {
        InfoRow(stringResource(R.string.label_manufacturer), android.os.Build.MANUFACTURER)
        InfoRow(stringResource(R.string.label_model), android.os.Build.MODEL)
        InfoRow(
            stringResource(R.string.label_android_version),
            "${android.os.Build.VERSION.RELEASE} (API ${android.os.Build.VERSION.SDK_INT})"
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onVersionClick)
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                stringResource(R.string.label_app_version),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                versionName,
                fontWeight = FontWeight.Medium,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun GnssCapabilitiesCard(caps: GnssCapabilitiesInfo?) {
    var isExpanded by rememberSaveable { mutableStateOf(false) }
    DeviceDetailCard(
        title = stringResource(R.string.gnss_capabilities),
        icon = Icons.Default.Memory,
        isExpandable = true,
        isExpanded = isExpanded,
        onExpandChange = { isExpanded = it }
    ) {
        if (caps != null) {
            InfoRow(stringResource(R.string.label_nav_messages), caps.hasNavigationMessages.yesNo())
            InfoRow(stringResource(R.string.label_measurements), caps.hasMeasurements.yesNo())
            InfoRow(stringResource(R.string.label_antenna_info), caps.hasAntennaInfo.yesNo())
            InfoRow(
                stringResource(R.string.label_meas_corrections),
                caps.hasMeasurementCorrections.yesNo()
            )
            InfoRow(
                stringResource(R.string.label_correlation_vectors),
                caps.hasMeasurementCorrelationVectors.yesNo()
            )
        } else {
            Text(
                stringResource(R.string.gnss_android_version_required),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun SensorCapabilitiesCard(caps: SensorCapabilitiesInfo) {
    var isExpanded by rememberSaveable { mutableStateOf(false) }
    DeviceDetailCard(
        title = stringResource(R.string.label_sensor_features),
        icon = Icons.Default.Sensors,
        isExpandable = true,
        isExpanded = isExpanded,
        onExpandChange = { isExpanded = it }
    ) {
        InfoRow(stringResource(R.string.label_accelerometer), caps.hasAccelerometer.yesNo())
        InfoRow(stringResource(R.string.label_gyroscope), caps.hasGyroscope.yesNo())
        InfoRow(stringResource(R.string.label_magnetometer), caps.hasMagnetometer.yesNo())
        InfoRow(stringResource(R.string.label_pressure), caps.hasPressure.yesNo())
        InfoRow(stringResource(R.string.label_proximity), caps.hasProximity.yesNo())
        InfoRow(stringResource(R.string.label_light), caps.hasLight.yesNo())
        InfoRow(stringResource(R.string.label_rotation_vector), caps.hasRotationVector.yesNo())
        InfoRow(stringResource(R.string.label_gravity), caps.hasGravity.yesNo())
    }
}

@Composable
fun SettingsEntryCard(onClick: () -> Unit) {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = SolidColor(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
        ),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        ListItem(
            modifier = Modifier.clickable(onClick = onClick),
            headlineContent = {
                Text(
                    stringResource(R.string.settings_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            },
            leadingContent = {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            },
            trailingContent = {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            colors = ListItemDefaults.colors(
                containerColor = androidx.compose.ui.graphics.Color.Transparent
            )
        )
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            value,
            fontWeight = FontWeight.Medium,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun Boolean.yesNo(): String {
    return if (this) stringResource(R.string.yes) else stringResource(R.string.no)
}
