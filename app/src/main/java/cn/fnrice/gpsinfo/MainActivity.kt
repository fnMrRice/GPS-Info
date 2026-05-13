package cn.fnrice.gpsinfo

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SatelliteAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import cn.fnrice.gpsinfo.ui.screen.HomeScreen
import cn.fnrice.gpsinfo.ui.screen.MapScreen
import cn.fnrice.gpsinfo.ui.screen.DeviceScreen
import cn.fnrice.gpsinfo.ui.screen.SettingsScreen
import cn.fnrice.gpsinfo.ui.theme.GPSInfoTheme
import cn.fnrice.gpsinfo.viewmodel.GnssViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GPSInfoTheme {
                GPSInfoApp()
            }
        }
    }
}

enum class AppDestinations(
    val label: Int,
    val icon: ImageVector,
) {
    SATELLITES(R.string.nav_home, Icons.Default.Home),
    MAP(R.string.nav_map, Icons.Default.SatelliteAlt),
    DEVICE(R.string.nav_profile, Icons.Default.Person),
    SETTINGS(R.string.settings_title, Icons.Default.Settings),
}

@Composable
fun GPSInfoApp() {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.SATELLITES) }
    val viewModel: GnssViewModel = viewModel()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.initSettings(context)
    }

    var hasLocationPermission by rememberSaveable {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    LaunchedEffect(hasLocationPermission) {
        viewModel.updatePermissionState(hasLocationPermission)
        if (hasLocationPermission) {
            viewModel.startGnss(context)
        }
    }

    DisposableEffect(Unit) {
        onDispose { viewModel.stopGnss() }
    }

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestinations.entries.filter { it != AppDestinations.SETTINGS }.forEach { destination ->
                item(
                    icon = { Icon(destination.icon, contentDescription = stringResource(destination.label)) },
                    label = { Text(stringResource(destination.label)) },
                    selected = destination == currentDestination,
                    onClick = { currentDestination = destination },
                )
            }
        }
    ) {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            if (!hasLocationPermission) {
                PermissionRequestScreen(
                    onRequestPermission = {
                        permissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                            )
                        )
                    },
                    onOpenSettings = {
                        context.startActivity(
                            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                            }
                        )
                    },
                )
            } else {
                AnimatedContent(
                    targetState = currentDestination,
                    transitionSpec = {
                        if (targetState.ordinal > initialState.ordinal) {
                            (slideInHorizontally { it } + fadeIn()).togetherWith(slideOutHorizontally { -it } + fadeOut())
                        } else {
                            (slideInHorizontally { -it } + fadeIn()).togetherWith(slideOutHorizontally { it } + fadeOut())
                        }
                    },
                    label = "NavigationTransition"
                ) { destination ->
                    when (destination) {
                        AppDestinations.SATELLITES -> HomeScreen(viewModel, innerPadding)
                        AppDestinations.MAP -> MapScreen(viewModel, innerPadding)
                        AppDestinations.DEVICE -> DeviceScreen(viewModel, innerPadding, onNavigateToSettings = {
                            currentDestination = AppDestinations.SETTINGS
                        })
                        AppDestinations.SETTINGS -> SettingsScreen(viewModel, onBack = {
                            currentDestination = AppDestinations.DEVICE
                        })
                    }
                }
            }
        }
    }
}

@Composable
private fun PermissionRequestScreen(
    onRequestPermission: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            Icons.Default.SatelliteAlt,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(16.dp))
        Text(
            stringResource(R.string.permission_required_title),
            style = MaterialTheme.typography.titleLarge,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            stringResource(R.string.permission_required_desc),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(24.dp))
        Button(onClick = onRequestPermission) {
            Text(stringResource(R.string.grant_permission))
        }
        Spacer(Modifier.height(8.dp))
        Button(onClick = onOpenSettings) {
            Text(stringResource(R.string.open_settings))
        }
    }
}
