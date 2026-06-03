package cn.fnrice.gpsinfo.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeveloperMode
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cn.fnrice.gpsinfo.R

@Composable
fun DeviceInfoCard(onVersionClick: () -> Unit) {
    val context = LocalContext.current
    var isExpanded by rememberSaveable { mutableStateOf(false) }
    val packageInfo = remember(context) {
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(context.packageName, android.content.pm.PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0)
            }
        } catch (_: Exception) {
            null
        }
    }
    val versionName = packageInfo?.versionName ?: "1.0.0"
    val versionCode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
        packageInfo?.longVersionCode?.toString() ?: "1"
    } else {
        @Suppress("DEPRECATION")
        packageInfo?.versionCode?.toString() ?: "1"
    }

    DeviceDetailCard(
        title = stringResource(R.string.device_info),
        icon = Icons.Default.DeveloperMode,
        isExpandable = true,
        isExpanded = isExpanded,
        onExpandChange = { isExpanded = it }
    ) {
        // 基本信息
        InfoRow(stringResource(R.string.label_manufacturer), android.os.Build.MANUFACTURER)
        InfoRow(stringResource(R.string.label_model), android.os.Build.MODEL)
        InfoRow(stringResource(R.string.label_brand), android.os.Build.BRAND)
        InfoRow(stringResource(R.string.label_product), android.os.Build.PRODUCT)
        InfoRow(stringResource(R.string.label_device), android.os.Build.DEVICE)
        InfoRow(stringResource(R.string.label_hardware), android.os.Build.HARDWARE)
        InfoRow(stringResource(R.string.label_board), android.os.Build.BOARD)

        // 系统信息
        InfoRow(
            stringResource(R.string.label_android_version),
            "${android.os.Build.VERSION.RELEASE} (API ${android.os.Build.VERSION.SDK_INT})"
        )
        InfoRow(stringResource(R.string.label_build_id), android.os.Build.ID)
        InfoRow(stringResource(R.string.label_build_display), android.os.Build.DISPLAY)
        InfoRow(stringResource(R.string.label_security_patch), android.os.Build.VERSION.SECURITY_PATCH.ifEmpty { "---" })

        // 应用信息
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
                "$versionName ($versionCode)",
                fontWeight = FontWeight.Medium,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
