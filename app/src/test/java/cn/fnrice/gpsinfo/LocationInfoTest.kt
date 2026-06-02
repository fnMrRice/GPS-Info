package cn.fnrice.gpsinfo

import cn.fnrice.gpsinfo.data.LocationInfo
import org.junit.Assert.*
import org.junit.Test

class LocationInfoTest {

    private fun createLocation(
        lat: Double = 39.9042,
        lon: Double = 116.4074,
        alt: Double = 43.5,
        speed: Float = 1.5f,
        bearing: Float = 90f,
        accuracy: Float = 8f,
        provider: String = "gps"
    ) = LocationInfo(
        latitude = lat,
        longitude = lon,
        altitude = alt,
        speed = speed,
        bearing = bearing,
        accuracy = accuracy,
        verticalAccuracyMeters = 10f,
        satelliteCount = 12,
        provider = provider,
    )

    @Test
    fun dataClass_equality() {
        val loc1 = createLocation()
        val loc2 = createLocation()
        assertEquals(loc1, loc2)
    }

    @Test
    fun dataClass_inequality_differentCoordinates() {
        val loc1 = createLocation(lat = 39.9)
        val loc2 = createLocation(lat = 40.0)
        assertNotEquals(loc1, loc2)
    }

    @Test
    fun dataClass_inequality_differentProvider() {
        val loc1 = createLocation(provider = "gps")
        val loc2 = createLocation(provider = "network")
        assertNotEquals(loc1, loc2)
    }

    @Test
    fun copy_updatesOnlyTargetField() {
        val original = createLocation()
        val copied = original.copy(latitude = 40.0, provider = "network")
        assertEquals(40.0, copied.latitude, 0.001)
        assertEquals("network", copied.provider)
        assertEquals(original.longitude, copied.longitude, 0.001)
        assertEquals(original.altitude, copied.altitude, 0.001)
        assertEquals(original.speed, copied.speed)
    }

    @Test
    fun coordinates_inValidRange() {
        val loc = createLocation()
        assertTrue(loc.latitude in -90.0..90.0)
        assertTrue(loc.longitude in -180.0..180.0)
    }

    @Test
    fun accuracy_isPositive() {
        val loc = createLocation()
        assertTrue(loc.accuracy > 0f)
    }

    @Test
    fun satelliteCount_isNonNegative() {
        val loc = createLocation()
        assertTrue(loc.satelliteCount >= 0)
    }

    @Test
    fun hashCode_consistentWithEquals() {
        val loc1 = createLocation()
        val loc2 = createLocation()
        assertEquals(loc1.hashCode(), loc2.hashCode())
    }
}
