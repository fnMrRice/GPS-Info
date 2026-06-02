package cn.fnrice.gpsinfo

import cn.fnrice.gpsinfo.data.SatelliteInfo
import org.junit.Assert.*
import org.junit.Test

class SatelliteInfoTest {

    private fun createGpsSatellite(svid: Int = 1, cn0: Float = 45f, usedInFix: Boolean = true) = SatelliteInfo(
        svid = svid,
        constellationType = 1, // CONSTELLATION_GPS
        cn0DbHz = cn0,
        elevationDegrees = 45f,
        azimuthDegrees = 180f,
        usedInFix = usedInFix,
    )

    @Test
    fun dataClass_equality() {
        val sat1 = createGpsSatellite()
        val sat2 = createGpsSatellite()
        assertEquals(sat1, sat2)
    }

    @Test
    fun dataClass_inequality_differentSvid() {
        val sat1 = createGpsSatellite(svid = 1)
        val sat2 = createGpsSatellite(svid = 2)
        assertNotEquals(sat1, sat2)
    }

    @Test
    fun dataClass_inequality_differentCn0() {
        val sat1 = createGpsSatellite(cn0 = 45f)
        val sat2 = createGpsSatellite(cn0 = 30f)
        assertNotEquals(sat1, sat2)
    }

    @Test
    fun copy_preservesOtherFields() {
        val original = createGpsSatellite()
        val copied = original.copy(cn0DbHz = 55f)
        assertEquals(55f, copied.cn0DbHz)
        assertEquals(original.svid, copied.svid)
        assertEquals(original.constellationType, copied.constellationType)
        assertEquals(original.elevationDegrees, copied.elevationDegrees)
        assertEquals(original.azimuthDegrees, copied.azimuthDegrees)
        assertEquals(original.usedInFix, copied.usedInFix)
    }

    @Test
    fun defaultValues_carrierFrequency() {
        val sat = createGpsSatellite()
        assertFalse(sat.hasCarrierFrequency)
        assertEquals(0f, sat.carrierFrequencyHz)
    }

    @Test
    fun defaultValues_basebandCn0() {
        val sat = createGpsSatellite()
        assertFalse(sat.hasBasebandCn0DbHz)
        assertEquals(0f, sat.basebandCn0DbHz)
    }

    @Test
    fun defaultValues_almanacEphemeris() {
        val sat = createGpsSatellite()
        assertFalse(sat.hasAlmanacData)
        assertFalse(sat.hasEphemerisData)
    }

    @Test
    fun withCarrierFrequency() {
        val sat = createGpsSatellite().copy(
            hasCarrierFrequency = true,
            carrierFrequencyHz = 1575.42f
        )
        assertTrue(sat.hasCarrierFrequency)
        assertEquals(1575.42f, sat.carrierFrequencyHz, 0.01f)
    }

    @Test
    fun withBasebandCn0() {
        val sat = createGpsSatellite().copy(
            hasBasebandCn0DbHz = true,
            basebandCn0DbHz = 42.5f
        )
        assertTrue(sat.hasBasebandCn0DbHz)
        assertEquals(42.5f, sat.basebandCn0DbHz, 0.01f)
    }

    @Test
    fun withAlmanacAndEphemeris() {
        val sat = createGpsSatellite().copy(
            hasAlmanacData = true,
            hasEphemerisData = true
        )
        assertTrue(sat.hasAlmanacData)
        assertTrue(sat.hasEphemerisData)
    }

    @Test
    fun hashCode_consistentWithEquals() {
        val sat1 = createGpsSatellite()
        val sat2 = createGpsSatellite()
        assertEquals(sat1.hashCode(), sat2.hashCode())
    }

    @Test
    fun toString_containsFieldNames() {
        val sat = createGpsSatellite()
        val str = sat.toString()
        assertTrue(str.contains("svid"))
        assertTrue(str.contains("constellationType"))
        assertTrue(str.contains("cn0DbHz"))
    }
}
