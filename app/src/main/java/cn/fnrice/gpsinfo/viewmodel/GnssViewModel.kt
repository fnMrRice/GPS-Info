package cn.fnrice.gpsinfo.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.location.GnssStatus
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.ViewModel
import cn.fnrice.gpsinfo.data.GnssState
import cn.fnrice.gpsinfo.data.LocationInfo
import cn.fnrice.gpsinfo.data.SatelliteInfo
import cn.fnrice.gpsinfo.data.SatelliteSnapshot
import cn.fnrice.gpsinfo.data.MapProvider
import cn.fnrice.gpsinfo.data.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import androidx.lifecycle.viewModelScope

import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import java.net.InetSocketAddress
import java.net.Socket

class GnssViewModel : ViewModel() {

    private val _state = MutableStateFlow(GnssState())
    val state: StateFlow<GnssState> = _state.asStateFlow()

    private val _snapshots = MutableStateFlow<List<SatelliteSnapshot>>(emptyList())
    val snapshots: StateFlow<List<SatelliteSnapshot>> = _snapshots.asStateFlow()

    private val _mapProvider = MutableStateFlow(MapProvider.AUTO)
    val mapProvider: StateFlow<MapProvider> = _mapProvider.asStateFlow()

    private val _actualMapProvider = MutableStateFlow(MapProvider.GOOGLE)
    val actualMapProvider: StateFlow<MapProvider> = _actualMapProvider.asStateFlow()

    private val _googleApiKey = MutableStateFlow("")
    val googleApiKey: StateFlow<String> = _googleApiKey.asStateFlow()

    private val _amapApiKey = MutableStateFlow("")
    val amapApiKey: StateFlow<String> = _amapApiKey.asStateFlow()

    private val _baiduApiKey = MutableStateFlow("")
    val baiduApiKey: StateFlow<String> = _baiduApiKey.asStateFlow()

    private val _useCustomGoogleKey = MutableStateFlow(false)
    val useCustomGoogleKey: StateFlow<Boolean> = _useCustomGoogleKey.asStateFlow()

    private val _useCustomAmapKey = MutableStateFlow(false)
    val useCustomAmapKey: StateFlow<Boolean> = _useCustomAmapKey.asStateFlow()

    private val _useCustomBaiduKey = MutableStateFlow(false)
    val useCustomBaiduKey: StateFlow<Boolean> = _useCustomBaiduKey.asStateFlow()

    private var settingsRepository: SettingsRepository? = null

    private var locationManager: LocationManager? = null
    private var gnssCallback: GnssStatus.Callback? = null
    private var locationListener: LocationListener? = null

    fun initSettings(context: Context) {
        if (settingsRepository == null) {
            val repo = SettingsRepository(context.applicationContext)
            settingsRepository = repo
            viewModelScope.launch {
                repo.mapProviderFlow.collect { provider ->
                    _mapProvider.value = provider
                    updateActualMapProvider(provider)
                }
            }
            viewModelScope.launch {
                repo.googleApiKeyFlow.collect { _googleApiKey.value = it }
            }
            viewModelScope.launch {
                repo.amapApiKeyFlow.collect { _amapApiKey.value = it }
            }
            viewModelScope.launch {
                repo.baiduApiKeyFlow.collect { _baiduApiKey.value = it }
            }
            viewModelScope.launch {
                repo.useCustomGoogleKeyFlow.collect { _useCustomGoogleKey.value = it }
            }
            viewModelScope.launch {
                repo.useCustomAmapKeyFlow.collect { _useCustomAmapKey.value = it }
            }
            viewModelScope.launch {
                repo.useCustomBaiduKeyFlow.collect { _useCustomBaiduKey.value = it }
            }
        }
    }

    private suspend fun updateActualMapProvider(provider: MapProvider) {
        if (provider != MapProvider.AUTO) {
            _actualMapProvider.value = provider
        } else {
            // Auto mode: Test Google connectivity
            val isGoogleReachable = withContext(Dispatchers.IO) {
                try {
                    Socket().use { socket ->
                        socket.connect(InetSocketAddress("www.google.com", 80), 2000)
                        true
                    }
                } catch (e: Exception) {
                    false
                }
            }
            _actualMapProvider.value = if (isGoogleReachable) MapProvider.GOOGLE else MapProvider.AMAP
        }
    }

    fun setMapProvider(provider: MapProvider) {
        viewModelScope.launch {
            settingsRepository?.setMapProvider(provider)
        }
    }

    fun setGoogleApiKey(key: String) {
        viewModelScope.launch {
            settingsRepository?.setGoogleApiKey(key)
        }
    }

    fun setAmapApiKey(key: String) {
        viewModelScope.launch {
            settingsRepository?.setAmapApiKey(key)
        }
    }

    fun setBaiduApiKey(key: String) {
        viewModelScope.launch {
            settingsRepository?.setBaiduApiKey(key)
        }
    }

    fun setUseCustomGoogleKey(use: Boolean) {
        viewModelScope.launch {
            settingsRepository?.setUseCustomGoogleKey(use)
        }
    }

    fun setUseCustomAmapKey(use: Boolean) {
        viewModelScope.launch {
            settingsRepository?.setUseCustomAmapKey(use)
        }
    }

    fun setUseCustomBaiduKey(use: Boolean) {
        viewModelScope.launch {
            settingsRepository?.setUseCustomBaiduKey(use)
        }
    }

    fun updatePermissionState(hasPermission: Boolean) {
        _state.value = _state.value.copy(hasPermission = hasPermission)
    }

    @SuppressLint("MissingPermission")
    fun startGnss(context: Context) {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationManager = lm

        val isGpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
        _state.value = _state.value.copy(isLocationEnabled = isGpsEnabled)

        if (!isGpsEnabled || !_state.value.hasPermission) return

        val callback = object : GnssStatus.Callback() {
            override fun onSatelliteStatusChanged(status: GnssStatus) {
                val satellites = (0 until status.satelliteCount).map { i ->
                    SatelliteInfo(
                        svid = status.getSvid(i),
                        constellationType = status.getConstellationType(i),
                        cn0DbHz = status.getCn0DbHz(i),
                        elevationDegrees = status.getElevationDegrees(i),
                        azimuthDegrees = status.getAzimuthDegrees(i),
                        usedInFix = status.usedInFix(i),
                    )
                }
                _state.value = _state.value.copy(
                    satellites = satellites,
                    satellitesTotal = status.satelliteCount,
                    satellitesUsedInFix = satellites.count { it.usedInFix },
                )
            }

            override fun onStarted() {}
            override fun onStopped() {}
            override fun onFirstFix(ttffMillis: Int) {}
        }
        gnssCallback = callback
        lm.registerGnssStatusCallback(callback, Handler(Looper.getMainLooper()))

        val listener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                _state.value = _state.value.copy(
                    location = LocationInfo.fromLocation(location)
                )
            }

            override fun onProviderEnabled(provider: String) {
                _state.value = _state.value.copy(isLocationEnabled = true)
            }

            override fun onProviderDisabled(provider: String) {
                _state.value = _state.value.copy(isLocationEnabled = false)
            }
        }
        locationListener = listener
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000L, 0f, listener, Looper.getMainLooper())
    }

    fun stopGnss() {
        gnssCallback?.let { locationManager?.unregisterGnssStatusCallback(it) }
        locationListener?.let { locationManager?.removeUpdates(it) }
        gnssCallback = null
        locationListener = null
        locationManager = null
    }

    fun saveSnapshot(label: String = "") {
        val current = _state.value
        _snapshots.value = _snapshots.value + SatelliteSnapshot(
            satellites = current.satellites,
            location = current.location,
            label = label,
        )
    }

    fun deleteSnapshot(id: Long) {
        _snapshots.value = _snapshots.value.filterNot { it.id == id }
    }

    fun getGnssCapabilities(context: Context): GnssCapabilitiesInfo? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val caps = lm.gnssCapabilities
            return GnssCapabilitiesInfo(
                hasNavigationMessages = caps.hasNavigationMessages(),
                hasMeasurements = caps.hasMeasurements(),
                hasAntennaInfo = caps.hasAntennaInfo(),
                hasMeasurementCorrections = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                     caps.hasMeasurementCorrections()
                } else false,
                hasMeasurementCorrelationVectors = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    caps.hasMeasurementCorrelationVectors()
                } else false,
            )
        }
        return null
    }

    override fun onCleared() {
        super.onCleared()
        stopGnss()
    }
}

data class GnssCapabilitiesInfo(
    val hasNavigationMessages: Boolean,
    val hasMeasurements: Boolean,
    val hasAntennaInfo: Boolean,
    val hasMeasurementCorrections: Boolean,
    val hasMeasurementCorrelationVectors: Boolean,
)
