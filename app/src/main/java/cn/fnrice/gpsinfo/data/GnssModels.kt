package cn.fnrice.gpsinfo.data

import android.location.GnssStatus
import android.location.Location

data class SatelliteInfo(
    val svid: Int,
    val constellationType: Int,
    val cn0DbHz: Float,
    val elevationDegrees: Float,
    val azimuthDegrees: Float,
    val usedInFix: Boolean,
) {
    val constellationName: String
        get() = when (constellationType) {
            GnssStatus.CONSTELLATION_GPS -> "GPS"
            GnssStatus.CONSTELLATION_SBAS -> "SBAS"
            GnssStatus.CONSTELLATION_GLONASS -> "GLONASS"
            GnssStatus.CONSTELLATION_QZSS -> "QZSS"
            GnssStatus.CONSTELLATION_BEIDOU -> "北斗"
            GnssStatus.CONSTELLATION_GALILEO -> "Galileo"
            GnssStatus.CONSTELLATION_IRNSS -> "IRNSS"
            else -> "Unknown"
        }
}

data class LocationInfo(
    val latitude: Double,
    val longitude: Double,
    val altitude: Double,
    val speed: Float,
    val bearing: Float,
    val accuracy: Float,
    val verticalAccuracyMeters: Float,
    val satelliteCount: Int,
) {
    companion object {
        fun fromLocation(location: Location): LocationInfo {
            return LocationInfo(
                latitude = location.latitude,
                longitude = location.longitude,
                altitude = location.altitude,
                speed = location.speed,
                bearing = location.bearing,
                accuracy = location.accuracy,
                verticalAccuracyMeters = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    location.verticalAccuracyMeters
                } else {
                    0f
                },
                satelliteCount = location.extras?.getInt("satellites", 0) ?: 0,
            )
        }
    }
}

data class GnssState(
    val satellites: List<SatelliteInfo> = emptyList(),
    val location: LocationInfo? = null,
    val satellitesUsedInFix: Int = 0,
    val satellitesTotal: Int = 0,
    val isLocationEnabled: Boolean = false,
    val hasPermission: Boolean = false,
)

data class SatelliteSnapshot(
    val id: Long = System.currentTimeMillis(),
    val timestamp: Long = System.currentTimeMillis(),
    val satellites: List<SatelliteInfo>,
    val location: LocationInfo?,
    val label: String = "",
)
