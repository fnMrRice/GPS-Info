package cn.fnrice.gpsinfo.ui.screen

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.layout.Box
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.OutlinedCard
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
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
import cn.fnrice.gpsinfo.ui.components.ToastUtils
import cn.fnrice.gpsinfo.R
import cn.fnrice.gpsinfo.data.DefaultApiKeys
import cn.fnrice.gpsinfo.data.MapProvider
import cn.fnrice.gpsinfo.viewmodel.GnssViewModel
import kotlinx.coroutines.launch

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogViewDialog(viewModel: GnssViewModel, onDismiss: () -> Unit) {
    val logs by viewModel.logs.collectAsState()
    val listState = rememberLazyListState()
    var autoScroll by remember { mutableStateOf(true) }

    LaunchedEffect(logs.size, autoScroll) {
        if (autoScroll && logs.isNotEmpty()) {
            listState.animateScrollToItem(logs.size - 1)
        }
    }

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
                        title = { Text(stringResource(R.string.logs_title)) },
                        navigationIcon = {
                            IconButton(onClick = onDismiss) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                            }
                        },
                        actions = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(end = 8.dp)
                                    .clickable { autoScroll = !autoScroll }
                            ) {
                                Text(
                                    stringResource(R.string.logs_auto_scroll),
                                    style = MaterialTheme.typography.labelMedium
                                )
                                Checkbox(
                                    checked = autoScroll,
                                    onCheckedChange = { autoScroll = it }
                                )
                            }
                            IconButton(onClick = { viewModel.clearLogs() }) {
                                Icon(Icons.Default.Delete, contentDescription = "Clear Logs")
                            }
                        }
                    )
                }
            ) { innerPadding ->
                Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                    if (logs.isEmpty()) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                stringResource(R.string.logs_empty),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(logs) { log ->
                                Text(
                                    text = log,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                )
                            }
                        }
                    }

                    // Quick scroll to top/bottom FABs could go here if needed
                }
            }
        }
    }
}

@Composable
private fun SettingsSectionCard(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = SolidColor(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
        )
    ) {
        Column(modifier = Modifier.padding(16.dp).animateContentSize()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun MapSettingsCard(
    currentProvider: MapProvider,
    onProviderSelected: (MapProvider) -> Unit
) {
    SettingsSectionCard(
        title = stringResource(R.string.map_settings),
        icon = Icons.Default.Memory
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            MapProvider.entries.forEach { provider ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onProviderSelected(provider) }
                        .padding(vertical = 4.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RadioButton(
                        selected = currentProvider == provider,
                        onClick = { onProviderSelected(provider) }
                    )
                    Text(
                        when (provider) {
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
    val scope = rememberCoroutineScope()
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ApiKeyItem(
            label = stringResource(R.string.map_provider_google),
            apiKey = googleApiKey,
            onApiKeyChange = onGoogleApiKeyChange,
            useCustom = useCustomGoogleKey,
            onUseCustomChange = onUseCustomGoogleKeyChange,
            defaultKey = DefaultApiKeys.GOOGLE_MAPS_KEY,
            onTestClick = { viewModel.testApiKey(MapProvider.GOOGLE) }
        )

        ApiKeyItem(
            label = stringResource(R.string.map_provider_amap),
            apiKey = amapApiKey,
            onApiKeyChange = onAmapApiKeyChange,
            useCustom = useCustomAmapKey,
            onUseCustomChange = onUseCustomAmapKeyChange,
            defaultKey = DefaultApiKeys.AMAP_KEY,
            onTestClick = { viewModel.testApiKey(MapProvider.AMAP) }
        )

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
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val testSuccess = stringResource(R.string.test_api_success)
    val testFailed = stringResource(R.string.test_api_fail)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(R.string.use_custom_key),
                    style = MaterialTheme.typography.labelMedium
                )
                Switch(
                    checked = useCustom,
                    onCheckedChange = onUseCustomChange,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (useCustom) {
            OutlinedTextField(
                value = apiKey,
                onValueChange = onApiKeyChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("API Key") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
        } else {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = defaultKey,
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = {
                scope.launch {
                    val result = onTestClick()
                    ToastUtils.showToast(context, if (result) testSuccess else testFailed)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(stringResource(R.string.test_api))
        }
    }
}