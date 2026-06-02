package cn.fnrice.gpsinfo.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cn.fnrice.gpsinfo.R
import cn.fnrice.gpsinfo.ui.components.DeviceInfoCard
import cn.fnrice.gpsinfo.ui.components.GnssCapabilitiesCard
import cn.fnrice.gpsinfo.ui.components.SettingsEntryCard
import cn.fnrice.gpsinfo.ui.components.ToastUtils
import cn.fnrice.gpsinfo.viewmodel.GnssViewModel

@Composable
fun DeviceScreen(viewModel: GnssViewModel, innerPadding: PaddingValues, onNavigateToSettings: () -> Unit) {
    val context = LocalContext.current
    val capabilities = remember { viewModel.getGnssCapabilities(context) }
    val isDeveloperMode by viewModel.isDeveloperMode.collectAsState()
    var clickCount by remember { mutableIntStateOf(0) }

    val stepsMessage = if (clickCount > 2 && !isDeveloperMode) {
        stringResource(R.string.developer_mode_steps, 7 - clickCount)
    } else ""

    val onVersionClick = {
        if (!isDeveloperMode) {
            clickCount++
            if (clickCount >= 7) {
                viewModel.setDeveloperMode(true)
                ToastUtils.showToast(context, R.string.developer_mode_enabled)
            } else if (clickCount > 2) {
                ToastUtils.showToast(context, stepsMessage)
            }
        } else {
            ToastUtils.showToast(context, R.string.developer_mode_enabled)
        }
    }

    // Renders scrollable profile UI with conditional capability cards
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            stringResource(R.string.profile_title),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
        )

        DeviceInfoCard(onVersionClick)

        GnssCapabilitiesCard(capabilities)

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            stringResource(R.string.settings_title),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        SettingsEntryCard(onNavigateToSettings)
        
        Spacer(modifier = Modifier.height(12.dp))
    }
}