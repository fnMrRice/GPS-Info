package cn.fnrice.gpsinfo.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import cn.fnrice.gpsinfo.ui.components.EnvironmentCard
import cn.fnrice.gpsinfo.ui.components.MotionCard
import cn.fnrice.gpsinfo.ui.components.OrientationCard
import cn.fnrice.gpsinfo.ui.components.Phone3DView
import cn.fnrice.gpsinfo.ui.components.SupportedSensorsCard
import cn.fnrice.gpsinfo.viewmodel.GnssViewModel

@Composable
fun SensorScreen(viewModel: GnssViewModel, innerPadding: PaddingValues) {
    val state by viewModel.state.collectAsState()
    val sensorValues = state.sensorValues
    val context = LocalContext.current
    val sensorCaps = remember { viewModel.getSensorCapabilities(context) }

    var supportedExpanded by remember { mutableStateOf(true) }
    var orientationExpanded by remember { mutableStateOf(false) }
    var motionExpanded by remember { mutableStateOf(false) }
    var environmentExpanded by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }
    var showOrientationHelp by remember { mutableStateOf(false) }
    var showPhone3D by remember { mutableStateOf(false) }

    LaunchedEffect(orientationExpanded, motionExpanded, environmentExpanded) {
        viewModel.setSensorUiActive(orientationExpanded || motionExpanded || environmentExpanded)
    }

    DisposableEffect(Unit) {
        onDispose { viewModel.setSensorUiActive(false) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Spacer(modifier = Modifier.height(4.dp))

        SupportedSensorsCard(
            sensorCaps = sensorCaps,
            sensorValues = sensorValues,
            isExpanded = supportedExpanded,
            onExpandChange = { supportedExpanded = it },
            onHelpClick = { showHelpDialog = true }
        )

        OrientationCard(
            azimuth = state.azimuth,
            sensorValues = sensorValues,
            isExpanded = orientationExpanded,
            onExpandChange = { orientationExpanded = it },
            onHelpClick = { showOrientationHelp = true },
            onOrientationClick = { _, _, _ -> showPhone3D = true }
        )

        MotionCard(
            sensorValues = sensorValues,
            isExpanded = motionExpanded,
            onExpandChange = { motionExpanded = it }
        )

        EnvironmentCard(
            sensorValues = sensorValues,
            isExpanded = environmentExpanded,
            onExpandChange = { environmentExpanded = it }
        )

        Spacer(modifier = Modifier.height(4.dp))
    }

    // 帮助弹窗
    SensorHelpDialog(show = showHelpDialog, onDismiss = { showHelpDialog = false })
    OrientationHelpDialog(show = showOrientationHelp, onDismiss = { showOrientationHelp = false })

    // 3D 手机姿态弹窗 — 从实时传感器数据读取姿态
    if (showPhone3D) {
        val liveRv = sensorValues[android.hardware.Sensor.TYPE_ROTATION_VECTOR]
            ?: sensorValues[android.hardware.Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR]
        val liveOrientation = liveRv?.let { v ->
            val m = FloatArray(9)
            android.hardware.SensorManager.getRotationMatrixFromVector(m, v)
            val o = FloatArray(3)
            android.hardware.SensorManager.getOrientation(m, o)
            Triple(
                Math.toDegrees(o[1].toDouble()).toFloat(),  // pitch
                Math.toDegrees(o[2].toDouble()).toFloat(),  // roll
                Math.toDegrees(o[0].toDouble()).toFloat()   // azimuth
            )
        }
        AlertDialog(
            onDismissRequest = { showPhone3D = false },
            title = { Text(stringResource(R.string.sensor_section_orientation)) },
            text = {
                Phone3DView(
                    pitch = liveOrientation?.first ?: 0f,
                    roll = liveOrientation?.second ?: 0f,
                    azimuth = liveOrientation?.third ?: state.azimuth
                )
            },
            confirmButton = {
                Text(
                    text = stringResource(android.R.string.ok),
                    modifier = Modifier.clickable { showPhone3D = false },
                    color = MaterialTheme.colorScheme.primary
                )
            }
        )
    }
}

@Composable
private fun SensorHelpDialog(show: Boolean, onDismiss: () -> Unit) {
    if (!show) return
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.sensor_help_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.sensor_help_available),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.sensor_help_this_bg),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.sensor_help_unavailable),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.sensor_help_this_bg),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Text(
                    text = stringResource(R.string.sensor_help_tap_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Text(
                text = stringResource(android.R.string.ok),
                modifier = Modifier.clickable { onDismiss() },
                color = MaterialTheme.colorScheme.primary
            )
        }
    )
}

@Composable
private fun OrientationHelpDialog(show: Boolean, onDismiss: () -> Unit) {
    if (!show) return
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.sensor_orientation_help_title)) },
        text = { Text(stringResource(R.string.sensor_orientation_help_message)) },
        confirmButton = {
            Text(
                text = stringResource(android.R.string.ok),
                modifier = Modifier.clickable { onDismiss() },
                color = MaterialTheme.colorScheme.primary
            )
        }
    )
}
