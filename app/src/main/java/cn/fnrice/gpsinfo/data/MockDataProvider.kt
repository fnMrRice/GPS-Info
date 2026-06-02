package cn.fnrice.gpsinfo.data

import android.location.GnssStatus
import kotlin.random.Random

object MockDataProvider {

    private val mockSatellites = listOf(
        // GPS (USA)
        Triple(GnssStatus.CONSTELLATION_GPS, 1, 45f),
        Triple(GnssStatus.CONSTELLATION_GPS, 3, 52f),
        Triple(GnssStatus.CONSTELLATION_GPS, 6, 38f),
        Triple(GnssStatus.CONSTELLATION_GPS, 9, 61f),
        Triple(GnssStatus.CONSTELLATION_GPS, 11, 28f),
        Triple(GnssStatus.CONSTELLATION_GPS, 14, 55f),
        Triple(GnssStatus.CONSTELLATION_GPS, 19, 33f),
        Triple(GnssStatus.CONSTELLATION_GPS, 21, 42f),
        // BeiDou (China)
        Triple(GnssStatus.CONSTELLATION_BEIDOU, 1, 50f),
        Triple(GnssStatus.CONSTELLATION_BEIDOU, 3, 47f),
        Triple(GnssStatus.CONSTELLATION_BEIDOU, 5, 39f),
        Triple(GnssStatus.CONSTELLATION_BEIDOU, 7, 55f),
        Triple(GnssStatus.CONSTELLATION_BEIDOU, 10, 31f),
        Triple(GnssStatus.CONSTELLATION_BEIDOU, 12, 44f),
        // Galileo (EU)
        Triple(GnssStatus.CONSTELLATION_GALILEO, 1, 48f),
        Triple(GnssStatus.CONSTELLATION_GALILEO, 3, 53f),
        Triple(GnssStatus.CONSTELLATION_GALILEO, 5, 36f),
        Triple(GnssStatus.CONSTELLATION_GALILEO, 7, 41f),
        Triple(GnssStatus.CONSTELLATION_GALILEO, 9, 29f),
        // GLONASS (Russia)
        Triple(GnssStatus.CONSTELLATION_GLONASS, 1, 40f),
        Triple(GnssStatus.CONSTELLATION_GLONASS, 5, 35f),
        Triple(GnssStatus.CONSTELLATION_GLONASS, 10, 43f),
        Triple(GnssStatus.CONSTELLATION_GLONASS, 15, 37f),
        // QZSS (Japan)
        Triple(GnssStatus.CONSTELLATION_QZSS, 1, 58f),
        Triple(GnssStatus.CONSTELLATION_QZSS, 4, 51f),
        // SBAS
        Triple(GnssStatus.CONSTELLATION_SBAS, 120, 25f),
        Triple(GnssStatus.CONSTELLATION_SBAS, 124, 30f),
    )

    private val mockLocation = LocationInfo(
        latitude = 39.9042,
        longitude = 116.4074,
        altitude = 43.5,
        speed = 0f,
        bearing = 0f,
        accuracy = 5f,
        verticalAccuracyMeters = 8f,
        satelliteCount = 12,
        provider = "mock"
    )

    fun generateSatellites(): List<SatelliteInfo> {
        return mockSatellites.map { (constellation, svid, baseCn0) ->
            val jitter = Random.nextFloat() * 6f - 3f
            val elevation = Random.nextFloat() * 80f + 5f
            val azimuth = Random.nextFloat() * 360f
            val usedInFix = baseCn0 + jitter > 30f
            SatelliteInfo(
                svid = svid,
                constellationType = constellation,
                cn0DbHz = (baseCn0 + jitter).coerceIn(0f, 65f),
                elevationDegrees = elevation,
                azimuthDegrees = azimuth,
                usedInFix = usedInFix,
                hasAlmanacData = true,
                hasEphemerisData = usedInFix,
            )
        }
    }

    fun generateLocation(): LocationInfo {
        val latJitter = Random.nextDouble(-0.0005, 0.0005)
        val lonJitter = Random.nextDouble(-0.0005, 0.0005)
        return mockLocation.copy(
            latitude = mockLocation.latitude + latJitter,
            longitude = mockLocation.longitude + lonJitter,
            accuracy = 3f + Random.nextFloat() * 10f,
        )
    }
}
