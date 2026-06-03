package cn.fnrice.gpsinfo.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.SatelliteAlt
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import cn.fnrice.gpsinfo.R

/**
 * 所有页面路由定义
 */
sealed class Screen(val route: String) {
    // 顶级目的地（显示在底部栏）
    data object Satellites : TopLevel("satellites", R.string.nav_home, Icons.Default.SatelliteAlt)
    data object Sensors : TopLevel("sensors", R.string.nav_sensors, Icons.Default.Sensors)
    data object Device : TopLevel("device", R.string.nav_profile, Icons.Default.Devices)
    data object Settings : TopLevel("settings", R.string.settings_title, Icons.Default.Settings)

    // 二级页面（不显示在导航栏）
    data object Map : Screen("map")
    data object SatelliteDetail : Screen("satellite/{prn}") {
        fun createRoute(prn: Int) = "satellite/$prn"
    }

    /**
     * 顶级目的地基类，携带底部栏所需的 label 和 icon
     */
    sealed class TopLevel(
        route: String,
        val label: Int,
        val icon: ImageVector,
    ) : Screen(route)
}

/**
 * 顶级目的地列表，用于底部导航栏
 */
val topLevelDestinations = listOf(Screen.Satellites, Screen.Sensors, Screen.Device, Screen.Settings)
