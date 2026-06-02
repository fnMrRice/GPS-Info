package cn.fnrice.gpsinfo

import cn.fnrice.gpsinfo.data.MockDataProvider
import org.junit.Assert.*
import org.junit.Test

class MockDataProviderTest {

    @Test
    fun generateSatellites_returnsNonEmptyList() {
        val satellites = MockDataProvider.generateSatellites()
        assertTrue("Should return at least one satellite", satellites.isNotEmpty())
    }

    @Test
    fun generateSatellites_containsMultipleConstellations() {
        val satellites = MockDataProvider.generateSatellites()
        val constellations = satellites.map { it.constellationType }.distinct()
        assertTrue("Should have at least 4 constellation types", constellations.size >= 4)
    }

    @Test
    fun generateSatellites_cn0InValidRange() {
        val satellites = MockDataProvider.generateSatellites()
        satellites.forEach { sat ->
            assertTrue("CN0 should be >= 0 for SV ${sat.svid}", sat.cn0DbHz >= 0f)
            assertTrue("CN0 should be <= 65 for SV ${sat.svid}", sat.cn0DbHz <= 65f)
        }
    }

    @Test
    fun generateSatellites_elevationInValidRange() {
        val satellites = MockDataProvider.generateSatellites()
        satellites.forEach { sat ->
            assertTrue("Elevation should be >= 0 for SV ${sat.svid}", sat.elevationDegrees >= 0f)
            assertTrue("Elevation should be <= 90 for SV ${sat.svid}", sat.elevationDegrees <= 90f)
        }
    }

    @Test
    fun generateSatellites_azimuthInValidRange() {
        val satellites = MockDataProvider.generateSatellites()
        satellites.forEach { sat ->
            assertTrue("Azimuth should be >= 0 for SV ${sat.svid}", sat.azimuthDegrees >= 0f)
            assertTrue("Azimuth should be <= 360 for SV ${sat.svid}", sat.azimuthDegrees <= 360f)
        }
    }

    @Test
    fun generateSatellites_usedInFixBasedOnCn0() {
        val satellites = MockDataProvider.generateSatellites()
        // Satellites with high CN0 should generally be used in fix
        val highCn0Sats = satellites.filter { it.cn0DbHz > 33f }
        val usedHighCn0 = highCn0Sats.count { it.usedInFix }
        assertTrue("Most high-CN0 satellites should be used in fix", usedHighCn0 > 0)
    }

    @Test
    fun generateSatellites_hasAlmanacAndEphemeris() {
        val satellites = MockDataProvider.generateSatellites()
        satellites.forEach { sat ->
            assertTrue("All mock satellites should have almanac data", sat.hasAlmanacData)
        }
    }

    @Test
    fun generateSatellites_uniqueSvidPerConstellation() {
        val satellites = MockDataProvider.generateSatellites()
        val pairs = satellites.map { "${it.constellationType}-${it.svid}" }
        assertEquals("SVID+constellation pairs should be unique", pairs.size, pairs.distinct().size)
    }

    @Test
    fun generateLocation_returnsValidCoordinates() {
        val location = MockDataProvider.generateLocation()
        assertTrue("Latitude should be in valid range", location.latitude in -90.0..90.0)
        assertTrue("Longitude should be in valid range", location.longitude in -180.0..180.0)
    }

    @Test
    fun generateLocation_hasMockProvider() {
        val location = MockDataProvider.generateLocation()
        assertEquals("mock", location.provider)
    }

    @Test
    fun generateLocation_hasReasonableAccuracy() {
        val location = MockDataProvider.generateLocation()
        assertTrue("Accuracy should be positive", location.accuracy > 0f)
        assertTrue("Accuracy should be reasonable", location.accuracy < 50f)
    }

    @Test
    fun generateLocation_variesBetweenCalls() {
        val loc1 = MockDataProvider.generateLocation()
        val loc2 = MockDataProvider.generateLocation()
        // With random jitter, coordinates should differ (with very high probability)
        val latDiff = kotlin.math.abs(loc1.latitude - loc2.latitude)
        val lonDiff = kotlin.math.abs(loc1.longitude - loc2.longitude)
        // At least one should have some variation (allowing for rare coincidences)
        assertTrue("Locations should have some jitter variation",
            latDiff > 0.0 || lonDiff > 0.0)
    }

    @Test
    fun generateSatellites_variesBetweenCalls() {
        val sats1 = MockDataProvider.generateSatellites()
        val sats2 = MockDataProvider.generateSatellites()
        // CN0 values should differ due to random jitter
        val cn0Changed = sats1.zip(sats2).any { (a, b) -> a.cn0DbHz != b.cn0DbHz }
        assertTrue("Satellite data should vary between calls due to jitter", cn0Changed)
    }
}
