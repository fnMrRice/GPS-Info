package cn.fnrice.gpsinfo.viewmodel

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.GnssStatus
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.fnrice.gpsinfo.data.*
import cn.fnrice.gpsinfo.ui.components.ToastUtils
import cn.fnrice.gpsinfo.R
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import cn.fnrice.gpsinfo.data.GnssState

import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import java.net.InetSocketAddress
import java.net.Socket

class GnssViewModel : ViewModel() {

    private val _state = MutableStateFlow(GnssState())
    val state: StateFlow<GnssState> = _state.asStateFlow()

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

    private val _isDeveloperMode = MutableStateFlow(false)
    val isDeveloperMode: StateFlow<Boolean> = _isDeveloperMode.asStateFlow()

    private val _logs = MutableStateFlow<List<String>>(emptyList())
    val logs: StateFlow<List<String>> = _logs.asStateFlow()

    private fun addLog(msg: String) {
        val timestamp = java.text.SimpleDateFormat("HH:mm:ss.SSS", java.util.Locale.getDefault()).format(java.util.Date())
        val logEntry = "[$timestamp] $msg"
        Log.d("GnssViewModel", logEntry)
        _logs.value = (listOf(logEntry) + _logs.value).take(500) // Keep last 500 logs, newest first
    }

    private var settingsRepository: SettingsRepository? = null

    private var locationManager: LocationManager? = null
    private var gnssCallback: GnssStatus.Callback? = null
    private var locationListener: LocationListener? = null
    private var sensorManager: SensorManager? = null
    private var rotationSensor: Sensor? = null
    private var sensorListener: SensorEventListener? = null

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
            viewModelScope.launch {
                repo.isDeveloperModeFlow.collect { _isDeveloperMode.value = it }
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

    fun setDeveloperMode(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository?.setDeveloperMode(enabled)
        }
    }

    suspend fun testApiKey(provider: MapProvider): Boolean {
        val host = when (provider) {
            MapProvider.GOOGLE -> "maps.googleapis.com"
            MapProvider.AMAP -> "restapi.amap.com"
            MapProvider.BAIDU -> "api.map.baidu.com"
            else -> return false
        }
        addLog("Testing API Key for $provider (host: $host)")
        return withContext(Dispatchers.IO) {
            try {
                Socket().use { socket ->
                    socket.connect(InetSocketAddress(host, 80), 3000)
                    addLog("API test success: $provider")
                    true
                }
            } catch (e: Exception) {
                addLog("API test failed: $provider, error: ${e.message}")
                false
            }
        }
    }

    fun updatePermissionState(hasPermission: Boolean) {
        _state.value = _state.value.copy(hasPermission = hasPermission)
    }

    @SuppressLint("MissingPermission")
    fun clearLogs() {
        _logs.value = emptyList()
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    fun startGnss(context: Context) {
        val startTime = System.currentTimeMillis()
        if (gnssCallback != null) {
            return
        }
        addLog("startGnss called")
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationManager = lm

        // Check if cached location is available to speed up initial display
        try {
            val lastKnownLocation = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER) 
                ?: lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            if (lastKnownLocation != null) {
                addLog("Last known location found: ${lastKnownLocation.provider}")
                _state.value = _state.value.copy(
                    location = LocationInfo.fromLocation(lastKnownLocation).copy(provider = "lastKnown")
                )
            }
        } catch (e: SecurityException) {
            addLog("SecurityException when getting last known location: ${e.message}")
        }

        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        this.sensorManager = sensorManager
        rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
            ?: sensorManager.getDefaultSensor(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR)

        val isGpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val hasPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        
        if (hasPermission != _state.value.hasPermission) {
            updatePermissionState(hasPermission)
        }
        
        addLog("GPS Enabled: $isGpsEnabled, Permission: $hasPermission")
        _state.value = _state.value.copy(isLocationEnabled = isGpsEnabled)

        if (!isGpsEnabled) {
            addLog("startGnss aborted: GPS is disabled")
            ToastUtils.showToast(context, R.string.toast_gps_disabled)
            return
        }
        if (!hasPermission) {
            addLog("startGnss aborted: Location permission not granted")
            ToastUtils.showToast(context, R.string.toast_permission_denied)
            return
        }

        sensorListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR || event.sensor.type == Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR) {
                    val rotationMatrix = FloatArray(9)
                    SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                    val orientation = FloatArray(3)
                    SensorManager.getOrientation(rotationMatrix, orientation)
                    val azimuth = Math.toDegrees(orientation[0].toDouble()).toFloat()
                    _state.value = _state.value.copy(azimuth = azimuth)
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
        rotationSensor?.let {
            sensorManager.registerListener(sensorListener, it, SensorManager.SENSOR_DELAY_UI)
        }

        var firstSatelliteStatusReceived = false
        val callback = object : GnssStatus.Callback() {
            override fun onSatelliteStatusChanged(status: GnssStatus) {
                if (status.satelliteCount == 0 && _state.value.satellites.isNotEmpty()) {
                    addLog("Received empty satellite status (0 satellites)")
                }
                if (!firstSatelliteStatusReceived) {
                    val timeTaken = System.currentTimeMillis() - startTime
                    addLog("First GNSS Status received after ${timeTaken}ms. Satellite count: ${status.satelliteCount}")
                    firstSatelliteStatusReceived = true
                }
                val satellites = (0 until status.satelliteCount).map { i ->
                    val hasCarrier = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        status.hasCarrierFrequencyHz(i)
                    } else false
                    val carrierFreq = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && hasCarrier) {
                        status.getCarrierFrequencyHz(i)
                    } else 0f
                    val hasBasebandCn0 = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        status.hasBasebandCn0DbHz(i)
                    } else false
                    val basebandCn0 = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && hasBasebandCn0) {
                        status.getBasebandCn0DbHz(i)
                    } else 0f

                    SatelliteInfo(
                        svid = status.getSvid(i),
                        constellationType = status.getConstellationType(i),
                        cn0DbHz = status.getCn0DbHz(i),
                        elevationDegrees = status.getElevationDegrees(i),
                        azimuthDegrees = status.getAzimuthDegrees(i),
                        usedInFix = status.usedInFix(i),
                        hasCarrierFrequency = hasCarrier,
                        carrierFrequencyHz = carrierFreq,
                        hasBasebandCn0DbHz = hasBasebandCn0,
                        basebandCn0DbHz = basebandCn0,
                        hasAlmanacData = status.hasAlmanacData(i),
                        hasEphemerisData = status.hasEphemerisData(i),
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
            override fun onFirstFix(ttffMillis: Int) {
                addLog("GNSS First Fix in ${ttffMillis}ms")
            }
        }
        gnssCallback = callback
        lm.registerGnssStatusCallback(callback, Handler(Looper.getMainLooper()))
        addLog("GNSS Status Callback registered")

        val listener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                addLog("Location changed: ${location.provider}, accuracy: ${location.accuracy}m")
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
        addLog("GPS Location updates requested")
        // Also request from NETWORK_PROVIDER for faster initial location
        try {
            if (lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000L, 0f, listener, Looper.getMainLooper())
            }
        } catch (e: Exception) {
            addLog("Network provider not available: ${e.message}")
        }
    }

    fun stopGnss() {
        addLog("stopGnss called")
        if (gnssCallback == null) {
            addLog("GNSS already stopped, ignoring")
            return
        }
        gnssCallback?.let { locationManager?.unregisterGnssStatusCallback(it) }
        locationListener?.let { locationManager?.removeUpdates(it) }
        sensorListener?.let { sensorManager?.unregisterListener(it) }
        gnssCallback = null
        locationListener = null
        locationManager = null
        sensorListener = null
        sensorManager = null
        rotationSensor = null
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

    fun getSensorCapabilities(context: Context): SensorCapabilitiesInfo {
        val sm = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        return SensorCapabilitiesInfo(
            hasAccelerometer = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null,
            hasGyroscope = sm.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null,
            hasMagnetometer = sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null,
            hasPressure = sm.getDefaultSensor(Sensor.TYPE_PRESSURE) != null,
            hasProximity = sm.getDefaultSensor(Sensor.TYPE_PROXIMITY) != null,
            hasLight = sm.getDefaultSensor(Sensor.TYPE_LIGHT) != null,
            hasRotationVector = sm.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) != null,
            hasGravity = sm.getDefaultSensor(Sensor.TYPE_GRAVITY) != null
        )
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
    val hasMeasurementCorrelationVectors: Boolean
)

data class SensorCapabilitiesInfo(
    val hasAccelerometer: Boolean,
    val hasGyroscope: Boolean,
    val hasMagnetometer: Boolean,
    val hasPressure: Boolean,
    val hasProximity: Boolean,
    val hasLight: Boolean,
    val hasRotationVector: Boolean,
    val hasGravity: Boolean
)