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
    val hasCarrierFrequency: Boolean = false,
    val carrierFrequencyHz: Float = 0f,
    val hasBasebandCn0DbHz: Boolean = false,
    val basebandCn0DbHz: Float = 0f,
    val hasAlmanacData: Boolean = false,
    val hasEphemerisData: Boolean = false,
) {
    fun getConstellationName(context: android.content.Context): String {
        return getConstellationNameStatic(context, constellationType)
    }

    companion object {
        fun getConstellationNameStatic(context: android.content.Context, constellationType: Int): String {
            val resId = when (constellationType) {
                GnssStatus.CONSTELLATION_GPS -> cn.fnrice.gpsinfo.R.string.constellation_gps
                GnssStatus.CONSTELLATION_SBAS -> cn.fnrice.gpsinfo.R.string.constellation_sbas
                GnssStatus.CONSTELLATION_GLONASS -> cn.fnrice.gpsinfo.R.string.constellation_glonass
                GnssStatus.CONSTELLATION_QZSS -> cn.fnrice.gpsinfo.R.string.constellation_qzss
                GnssStatus.CONSTELLATION_BEIDOU -> cn.fnrice.gpsinfo.R.string.constellation_beidou
                GnssStatus.CONSTELLATION_GALILEO -> cn.fnrice.gpsinfo.R.string.constellation_galileo
                GnssStatus.CONSTELLATION_IRNSS -> cn.fnrice.gpsinfo.R.string.constellation_irnss
                else -> cn.fnrice.gpsinfo.R.string.constellation_unknown
            }
            return context.getString(resId)
        }
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
    val provider: String,
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
                provider = location.provider ?: "Unknown",
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
    val azimuth: Float = 0f,
)

data class SatelliteSnapshot(
    val id: Long = System.currentTimeMillis(),
    val timestamp: Long = System.currentTimeMillis(),
    val satellites: List<SatelliteInfo>,
    val location: LocationInfo?,
    val label: String = "",
)
