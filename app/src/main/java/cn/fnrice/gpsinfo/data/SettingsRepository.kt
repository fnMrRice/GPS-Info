package cn.fnrice.gpsinfo.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "settings")

enum class MapProvider(val displayName: String) {
    AUTO("自动"),
    GOOGLE("Google"),
    AMAP("高德"),
    BAIDU("百度")
}

class SettingsRepository(private val context: Context) {
    private val MAP_PROVIDER_KEY = stringPreferencesKey("map_provider")

    val mapProviderFlow: Flow<MapProvider> = context.dataStore.data
        .map { preferences ->
            val providerName = preferences[MAP_PROVIDER_KEY] ?: MapProvider.AUTO.name
            try {
                MapProvider.valueOf(providerName)
            } catch (e: Exception) {
                MapProvider.AUTO
            }
        }

    suspend fun setMapProvider(provider: MapProvider) {
        context.dataStore.edit { preferences ->
            preferences[MAP_PROVIDER_KEY] = provider.name
        }
    }
}
