package cn.fnrice.gpsinfo.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import cn.fnrice.gpsinfo.R
import cn.fnrice.gpsinfo.ui.components.ApiKeysCard
import cn.fnrice.gpsinfo.ui.components.LogViewDialog
import cn.fnrice.gpsinfo.ui.components.MapSettingsCard
import cn.fnrice.gpsinfo.ui.components.SettingsSectionCard
import cn.fnrice.gpsinfo.viewmodel.GnssViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDialog(viewModel: GnssViewModel, onDismiss: () -> Unit) {
    val currentMapProvider by viewModel.mapProvider.collectAsState()
    val googleApiKey by viewModel.googleApiKey.collectAsState()
    val amapApiKey by viewModel.amapApiKey.collectAsState()
    val baiduApiKey by viewModel.baiduApiKey.collectAsState()
    val useCustomGoogleKey by viewModel.useCustomGoogleKey.collectAsState()
    val useCustomAmapKey by viewModel.useCustomAmapKey.collectAsState()
    val useCustomBaiduKey by viewModel.useCustomBaiduKey.collectAsState()
    val isDeveloperMode by viewModel.isDeveloperMode.collectAsState()
    var showLogDialog by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surface
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text(stringResource(R.string.settings_title)) },
                        navigationIcon = {
                            IconButton(onClick = onDismiss) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                            }
                        }
                    )
                }
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    MapSettingsCard(
                        currentProvider = currentMapProvider,
                        onProviderSelected = { viewModel.setMapProvider(it) }
                    )

                    if (isDeveloperMode) {
                        SettingsSectionCard(
                            title = stringResource(R.string.developer_options),
                            icon = Icons.Default.BugReport
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                ApiKeysCard(
                                    viewModel = viewModel,
                                    googleApiKey = googleApiKey,
                                    onGoogleApiKeyChange = { viewModel.setGoogleApiKey(it) },
                                    amapApiKey = amapApiKey,
                                    onAmapApiKeyChange = { viewModel.setAmapApiKey(it) },
                                    baiduApiKey = baiduApiKey,
                                    onBaiduApiKeyChange = { viewModel.setBaiduApiKey(it) },
                                    useCustomGoogleKey = useCustomGoogleKey,
                                    onUseCustomGoogleKeyChange = { viewModel.setUseCustomGoogleKey(it) },
                                    useCustomAmapKey = useCustomAmapKey,
                                    onUseCustomAmapKeyChange = { viewModel.setUseCustomAmapKey(it) },
                                    useCustomBaiduKey = useCustomBaiduKey,
                                    onUseCustomBaiduKeyChange = { viewModel.setUseCustomBaiduKey(it) }
                                )

                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 4.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                )

                                Button(
                                    onClick = { showLogDialog = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Settings,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(stringResource(R.string.label_view_logs))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }

                if (showLogDialog) {
                    LogViewDialog(viewModel = viewModel, onDismiss = { showLogDialog = false })
                }
            }
        }
    }
}