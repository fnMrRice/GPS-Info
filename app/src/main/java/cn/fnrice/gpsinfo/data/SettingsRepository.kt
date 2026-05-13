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
    private val GOOGLE_API_KEY = stringPreferencesKey("google_api_key")
    private val AMAP_API_KEY = stringPreferencesKey("amap_api_key")
    private val BAIDU_API_KEY = stringPreferencesKey("baidu_api_key")

    val mapProviderFlow: Flow<MapProvider> = context.dataStore.data
        .map { preferences ->
            val providerName = preferences[MAP_PROVIDER_KEY] ?: MapProvider.AUTO.name
            try {
                MapProvider.valueOf(providerName)
            } catch (e: Exception) {
                MapProvider.AUTO
            }
        }

    val googleApiKeyFlow: Flow<String> = context.dataStore.data
        .map { preferences -> preferences[GOOGLE_API_KEY] ?: "" }

    val amapApiKeyFlow: Flow<String> = context.dataStore.data
        .map { preferences -> preferences[AMAP_API_KEY] ?: "" }

    val baiduApiKeyFlow: Flow<String> = context.dataStore.data
        .map { preferences -> preferences[BAIDU_API_KEY] ?: "" }

    suspend fun setMapProvider(provider: MapProvider) {
        context.dataStore.edit { preferences ->
            preferences[MAP_PROVIDER_KEY] = provider.name
        }
    }

    suspend fun setGoogleApiKey(key: String) {
        context.dataStore.edit { preferences ->
            preferences[GOOGLE_API_KEY] = key
        }
    }

    suspend fun setAmapApiKey(key: String) {
        context.dataStore.edit { preferences ->
            preferences[AMAP_API_KEY] = key
        }
    }

    suspend fun setBaiduApiKey(key: String) {
        context.dataStore.edit { preferences ->
            preferences[BAIDU_API_KEY] = key
        }
    }
}
