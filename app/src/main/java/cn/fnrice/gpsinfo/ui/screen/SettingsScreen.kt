package cn.fnrice.gpsinfo.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import android.widget.Toast
import cn.fnrice.gpsinfo.R
import cn.fnrice.gpsinfo.data.DefaultApiKeys
import cn.fnrice.gpsinfo.data.MapProvider
import cn.fnrice.gpsinfo.viewmodel.GnssViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: GnssViewModel, onBack: () -> Unit) {
    val currentMapProvider by viewModel.mapProvider.collectAsState()
    val googleApiKey by viewModel.googleApiKey.collectAsState()
    val amapApiKey by viewModel.amapApiKey.collectAsState()
    val baiduApiKey by viewModel.baiduApiKey.collectAsState()
    val useCustomGoogleKey by viewModel.useCustomGoogleKey.collectAsState()
    val useCustomAmapKey by viewModel.useCustomAmapKey.collectAsState()
    val useCustomBaiduKey by viewModel.useCustomBaiduKey.collectAsState()
    val isDeveloperMode by viewModel.isDeveloperMode.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
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
            }
            
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun MapSettingsCard(
    currentProvider: MapProvider,
    onProviderSelected: (MapProvider) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(stringResource(R.string.map_settings), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            
            MapProvider.values().forEach { provider ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RadioButton(
                        selected = currentProvider == provider,
                        onClick = { onProviderSelected(provider) }
                    )
                    Text(
                        when(provider) {
                            MapProvider.AUTO -> stringResource(R.string.map_provider_auto)
                            MapProvider.GOOGLE -> stringResource(R.string.map_provider_google)
                            MapProvider.AMAP -> stringResource(R.string.map_provider_amap)
                            MapProvider.BAIDU -> stringResource(R.string.map_provider_baidu)
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun ApiKeysCard(
    viewModel: GnssViewModel,
    googleApiKey: String,
    onGoogleApiKeyChange: (String) -> Unit,
    amapApiKey: String,
    onAmapApiKeyChange: (String) -> Unit,
    baiduApiKey: String,
    onBaiduApiKeyChange: (String) -> Unit,
    useCustomGoogleKey: Boolean,
    onUseCustomGoogleKeyChange: (Boolean) -> Unit,
    useCustomAmapKey: Boolean,
    onUseCustomAmapKeyChange: (Boolean) -> Unit,
    useCustomBaiduKey: Boolean,
    onUseCustomBaiduKeyChange: (Boolean) -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(stringResource(R.string.developer_options), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            ApiKeyItem(
                label = stringResource(R.string.map_provider_google),
                apiKey = googleApiKey,
                onApiKeyChange = onGoogleApiKeyChange,
                useCustom = useCustomGoogleKey,
                onUseCustomChange = onUseCustomGoogleKeyChange,
                defaultKey = DefaultApiKeys.GOOGLE_MAPS_KEY,
                onTestClick = { viewModel.testApiKey(MapProvider.GOOGLE) }
            )
            
            Spacer(modifier = Modifier.height(12.dp))

            ApiKeyItem(
                label = stringResource(R.string.map_provider_amap),
                apiKey = amapApiKey,
                onApiKeyChange = onAmapApiKeyChange,
                useCustom = useCustomAmapKey,
                onUseCustomChange = onUseCustomAmapKeyChange,
                defaultKey = DefaultApiKeys.AMAP_KEY,
                onTestClick = { viewModel.testApiKey(MapProvider.AMAP) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            ApiKeyItem(
                label = stringResource(R.string.map_provider_baidu),
                apiKey = baiduApiKey,
                onApiKeyChange = onBaiduApiKeyChange,
                useCustom = useCustomBaiduKey,
                onUseCustomChange = onUseCustomBaiduKeyChange,
                defaultKey = DefaultApiKeys.BAIDU_MAPS_KEY,
                onTestClick = { viewModel.testApiKey(MapProvider.BAIDU) }
            )
        }
    }
}

@Composable
private fun ApiKeyItem(
    label: String,
    apiKey: String,
    onApiKeyChange: (String) -> Unit,
    useCustom: Boolean,
    onUseCustomChange: (Boolean) -> Unit,
    defaultKey: String,
    onTestClick: suspend () -> Boolean
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isTesting by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = {
                        isTesting = true
                        scope.launch {
                            val success = onTestClick()
                            isTesting = false
                            val resId = if (success) {
                                R.string.test_api_success
                            } else {
                                R.string.test_api_fail
                            }
                            Toast.makeText(context, resId, Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = !isTesting,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(stringResource(R.string.test_api), style = MaterialTheme.typography.labelMedium)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.use_custom_key), style = MaterialTheme.typography.labelMedium)
                    Switch(checked = useCustom, onCheckedChange = onUseCustomChange)
                }
            }
        }
        
        if (useCustom) {
            OutlinedTextField(
                value = apiKey,
                onValueChange = onApiKeyChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.api_keys)) },
                singleLine = true
            )
        } else {
            val displayKey = if (defaultKey.length > 8) {
                defaultKey.take(4) + "..." + defaultKey.takeLast(4)
            } else {
                "********"
            }
            Text(
                text = "Default: $displayKey",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
