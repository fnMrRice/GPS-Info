package cn.fnrice.gpsinfo

import cn.fnrice.gpsinfo.data.GnssState
import cn.fnrice.gpsinfo.data.LocationInfo
import cn.fnrice.gpsinfo.data.SatelliteInfo
import cn.fnrice.gpsinfo.data.MapProvider
import org.junit.Assert.*
import org.junit.Test

class GnssStateTest {

    @Test
    fun defaultState_hasEmptySatellites() {
        val state = GnssState()
        assertTrue(state.satellites.isEmpty())
        assertEquals(0, state.satellitesTotal)
        assertEquals(0, state.satellitesUsedInFix)
    }

    @Test
    fun defaultState_hasNullLocation() {
        val state = GnssState()
        assertNull(state.location)
    }

    @Test
    fun defaultState_locationDisabled() {
        val state = GnssState()
        assertFalse(state.isLocationEnabled)
    }

    @Test
    fun defaultState_noPermission() {
        val state = GnssState()
        assertFalse(state.hasPermission)
    }

    @Test
    fun defaultState_zeroAzimuth() {
        val state = GnssState()
        assertEquals(0f, state.azimuth)
    }

    @Test
    fun defaultState_emptySensorValues() {
        val state = GnssState()
        assertTrue(state.sensorValues.isEmpty())
    }

    @Test
    fun copyState_preservesUnchangedFields() {
        val original = GnssState(
            satellitesTotal = 10,
            satellitesUsedInFix = 5,
            isLocationEnabled = true,
            hasPermission = true,
            azimuth = 45f
        )
        val copied = original.copy(satellitesTotal = 20)
        assertEquals(20, copied.satellitesTotal)
        assertEquals(5, copied.satellitesUsedInFix)
        assertTrue(copied.isLocationEnabled)
        assertTrue(copied.hasPermission)
        assertEquals(45f, copied.azimuth)
    }

    @Test
    fun copyState_canUpdateLocation() {
        val location = LocationInfo(
            latitude = 39.9,
            longitude = 116.4,
            altitude = 50.0,
            speed = 0f,
            bearing = 0f,
            accuracy = 10f,
            verticalAccuracyMeters = 5f,
            satelliteCount = 8,
            provider = "gps"
        )
        val state = GnssState().copy(location = location)
        assertNotNull(state.location)
        assertEquals(39.9, state.location!!.latitude, 0.001)
        assertEquals("gps", state.location!!.provider)
    }

    @Test
    fun satelliteInfo_usedInFixCount() {
        val satellites = listOf(
            SatelliteInfo(1, 1, 45f, 30f, 100f, true),
            SatelliteInfo(2, 1, 20f, 10f, 200f, false),
            SatelliteInfo(3, 1, 50f, 60f, 300f, true),
            SatelliteInfo(4, 1, 15f, 5f, 40f, false),
        )
        val usedCount = satellites.count { it.usedInFix }
        assertEquals(2, usedCount)
    }
}

class MapProviderTest {

    @Test
    fun mapProvider_hasFourEntries() {
        assertEquals(4, MapProvider.entries.size)
    }

    @Test
    fun mapProvider_containsExpectedProviders() {
        assertTrue(MapProvider.entries.contains(MapProvider.AUTO))
        assertTrue(MapProvider.entries.contains(MapProvider.GOOGLE))
        assertTrue(MapProvider.entries.contains(MapProvider.AMAP))
        assertTrue(MapProvider.entries.contains(MapProvider.BAIDU))
    }

    @Test
    fun mapProvider_displayNamesNotEmpty() {
        MapProvider.entries.forEach { provider ->
            assertFalse("Display name should not be empty for $provider", provider.displayName.isEmpty())
        }
    }

    @Test
    fun mapProvider_displayNamesAreUnique() {
        val names = MapProvider.entries.map { it.displayName }
        assertEquals("Display names should be unique", names.size, names.distinct().size)
    }
}
