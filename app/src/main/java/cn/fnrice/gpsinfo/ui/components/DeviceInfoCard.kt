package cn.fnrice.gpsinfo.ui.components

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.util.DisplayMetrics
import android.view.WindowManager
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.DeveloperMode
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.ScreenRotation
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
import java.io.File
import java.util.Locale

@Composable
fun DeviceInfoCard(onVersionClick: () -> Unit) {
    val context = LocalContext.current
    var isExpanded by rememberSaveable { mutableStateOf(false) }

    DeviceDetailCard(
        title = stringResource(R.string.device_info),
        icon = Icons.Default.PhoneAndroid,
        isExpandable = true,
        isExpanded = isExpanded,
        onExpandChange = { isExpanded = it }
    ) {
        // 设备硬件
        SectionLabel(stringResource(R.string.section_hardware))
        InfoRow(stringResource(R.string.label_manufacturer), Build.MANUFACTURER)
        InfoRow(stringResource(R.string.label_brand), Build.BRAND)
        InfoRow(stringResource(R.string.label_model), Build.MODEL)
        InfoRow(stringResource(R.string.label_product), Build.PRODUCT)
        InfoRow(stringResource(R.string.label_device), Build.DEVICE)
        InfoRow(stringResource(R.string.label_board), Build.BOARD)
        InfoRow(stringResource(R.string.label_hardware), Build.HARDWARE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            InfoRow(stringResource(R.string.label_soc_manufacturer), Build.SOC_MANUFACTURER)
            InfoRow(stringResource(R.string.label_soc_model), Build.SOC_MODEL)
        }

        Spacer(Modifier.height(8.dp))

        // 系统软件
        SectionLabel(stringResource(R.string.section_system))
        InfoRow(stringResource(R.string.label_android_version), "${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
        InfoRow(stringResource(R.string.label_build_id), Build.ID)
        InfoRow(stringResource(R.string.label_build_display), Build.DISPLAY)
        InfoRow(stringResource(R.string.label_build_type), Build.TYPE)
        InfoRow(stringResource(R.string.label_build_tags), Build.TAGS)
        InfoRow(stringResource(R.string.label_bootloader), Build.BOOTLOADER)
        InfoRow(stringResource(R.string.label_security_patch), Build.VERSION.SECURITY_PATCH.ifEmpty { "---" })
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val baseOs = Build.VERSION.BASE_OS
            InfoRow(stringResource(R.string.label_base_os), if (baseOs.isNotEmpty()) baseOs else "---")
        }
        InfoRow(stringResource(R.string.label_kernel), getKernelVersion())

        Spacer(Modifier.height(8.dp))

        // 屏幕信息
        SectionLabel(stringResource(R.string.section_display))
        val displayInfo = remember { getDisplayInfo(context) }
        InfoRow(stringResource(R.string.label_resolution), displayInfo.resolution)
        InfoRow(stringResource(R.string.label_density), displayInfo.density)
        InfoRow(stringResource(R.string.label_density_dpi), displayInfo.densityDpi)

        Spacer(Modifier.height(8.dp))

        // 内存信息
        SectionLabel(stringResource(R.string.section_memory))
        val memInfo = remember { getMemoryInfo(context) }
        InfoRow(stringResource(R.string.label_total_ram), memInfo.totalRam)
        InfoRow(stringResource(R.string.label_available_ram), memInfo.availableRam)

        Spacer(Modifier.height(8.dp))

        // 区域与语言
        SectionLabel(stringResource(R.string.section_locale))
        val locale = Locale.getDefault()
        InfoRow(stringResource(R.string.label_language), "${locale.language} (${locale.displayLanguage})")
        InfoRow(stringResource(R.string.label_country), "${locale.country} (${locale.displayCountry})")
        InfoRow(stringResource(R.string.label_timezone), java.util.TimeZone.getDefault().id)

        Spacer(Modifier.height(8.dp))

        // 应用信息
        SectionLabel(stringResource(R.string.section_app))
        val packageInfo = remember(context) { getPackageInfo(context) }
        val versionName = packageInfo?.versionName ?: "1.0.0"
        val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            packageInfo?.longVersionCode?.toString() ?: "1"
        } else {
            @Suppress("DEPRECATION")
            packageInfo?.versionCode?.toString() ?: "1"
        }
        InfoRow(stringResource(R.string.label_package_name), context.packageName)
        InfoRow(stringResource(R.string.label_min_sdk), "API ${context.applicationInfo.minSdkVersion}")
        InfoRow(stringResource(R.string.label_target_sdk), "API ${context.applicationInfo.targetSdkVersion}")

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

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
    )
}

private fun getKernelVersion(): String {
    return try {
        File("/proc/version").readText().trim().let { full ->
            // 提取版本号部分，如 "5.15.104-android14-..."
            val match = Regex("""\d+\.\d+\.\d+[-\w]*""").find(full)
            match?.value ?: full.take(60)
        }
    } catch (_: Exception) {
        System.getProperty("os.version") ?: "---"
    }
}

private data class DisplayInfo(val resolution: String, val density: String, val densityDpi: String)

private fun getDisplayInfo(context: Context): DisplayInfo {
    return try {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val metrics = DisplayMetrics()
        @Suppress("DEPRECATION")
        wm.defaultDisplay.getRealMetrics(metrics)
        val densityBucket = when {
            metrics.densityDpi <= 120 -> "ldpi"
            metrics.densityDpi <= 160 -> "mdpi"
            metrics.densityDpi <= 240 -> "hdpi"
            metrics.densityDpi <= 320 -> "xhdpi"
            metrics.densityDpi <= 480 -> "xxhdpi"
            metrics.densityDpi <= 640 -> "xxxhdpi"
            else -> "xxxhdpi+"
        }
        DisplayInfo(
            resolution = "${metrics.widthPixels} × ${metrics.heightPixels}",
            density = "${metrics.density}x ($densityBucket)",
            densityDpi = "${metrics.densityDpi} dpi"
        )
    } catch (_: Exception) {
        DisplayInfo("---", "---", "---")
    }
}

private data class MemoryInfo(val totalRam: String, val availableRam: String)

private fun getMemoryInfo(context: Context): MemoryInfo {
    return try {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        am.getMemoryInfo(memInfo)
        MemoryInfo(
            totalRam = formatBytes(memInfo.totalMem),
            availableRam = formatBytes(memInfo.availMem)
        )
    } catch (_: Exception) {
        MemoryInfo("---", "---")
    }
}

private fun formatBytes(bytes: Long): String {
    val gb = bytes / (1024.0 * 1024.0 * 1024.0)
    return if (gb >= 1.0) "%.1f GB".format(gb) else "%.0f MB".format(bytes / (1024.0 * 1024.0))
}

private fun getPackageInfo(context: Context) = try {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        context.packageManager.getPackageInfo(context.packageName, android.content.pm.PackageManager.PackageInfoFlags.of(0))
    } else {
        @Suppress("DEPRECATION")
        context.packageManager.getPackageInfo(context.packageName, 0)
    }
} catch (_: Exception) {
    null
}
