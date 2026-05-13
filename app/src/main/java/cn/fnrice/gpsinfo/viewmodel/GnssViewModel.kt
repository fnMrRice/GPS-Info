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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class GnssViewModel : ViewModel() {

    private val _state = MutableStateFlow(GnssState())
    val state: StateFlow<GnssState> = _state.asStateFlow()

    private val _snapshots = MutableStateFlow<List<SatelliteSnapshot>>(emptyList())
    val snapshots: StateFlow<List<SatelliteSnapshot>> = _snapshots.asStateFlow()

    private var locationManager: LocationManager? = null
    private var gnssCallback: GnssStatus.Callback? = null
    private var locationListener: LocationListener? = null

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
                hasMeasurementCorrections = caps.hasMeasurementCorrections(),
                hasMeasurementCorrelationVectors = caps.hasMeasurementCorrelationVectors(),
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
