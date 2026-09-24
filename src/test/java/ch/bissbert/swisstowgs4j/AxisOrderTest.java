package ch.bissbert.swisstowgs4j;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * LV95 and LV03 differ by a constant offset of 2,000,000 m east and
 * 1,000,000 m north. The LV03 constructor takes (north, east); LV95 takes
 * (east, north). Regression tests for issue #5.
 */
class AxisOrderTest {

    @Test
    void lv95ToLv03KeepsAxes() {
        LV03 bern = new LV95(2_600_000.0, 1_200_000.0, 540.0).toLV03();
        assertEquals(600_000, bern.getEast(), 0);
        assertEquals(200_000, bern.getNorth(), 0);
        assertEquals(540.0, bern.getHeight());
    }

    @Test
    void lv95ToLv03WithoutHeight() {
        LV03 point = new LV95(2_683_000.0, 1_248_000.0).toLV03();
        assertEquals(683_000, point.getEast(), 0);
        assertEquals(248_000, point.getNorth(), 0);
        assertNull(point.getHeight());
    }

    @Test
    void lv03ToLv95KeepsAxes() {
        LV95 bern = new LV03(200_000, 600_000, 540.0).toLV95();
        assertEquals(2_600_000, bern.getEast(), 0);
        assertEquals(1_200_000, bern.getNorth(), 0);
        assertEquals(540.0, bern.getHeight());
    }

    @Test
    void lv03RoundTripIsExact() {
        LV03 start = new LV03(248_000, 683_000, 400.0);
        LV03 back = start.toLV95().toLV03();
        assertEquals(start.getEast(), back.getEast(), 0);
        assertEquals(start.getNorth(), back.getNorth(), 0);
        assertEquals(start.getHeight(), back.getHeight());
    }

    @Test
    void wgs84ToLv03KeepsAxes() {
        // REFRAME: 7.438632495274896 / 46.951082876677035 is LV03 east 600000, north 200000.
        LV03 bern = new WGS84(7.438632495274896, 46.951082876677035).toLV03();
        assertEquals(600_000, bern.getEast(), 2.0);
        assertEquals(200_000, bern.getNorth(), 2.0);
    }

    @Test
    void lv03ToWgs84MatchesLv95ToWgs84() {
        WGS84 viaLv03 = new LV03(248_000, 683_000).toWGS84();
        WGS84 direct = new LV95(2_683_000.0, 1_248_000.0).toWGS84();
        assertEquals(direct.getLongitude(), viaLv03.getLongitude(), 0);
        assertEquals(direct.getLatitude(), viaLv03.getLatitude(), 0);
    }
}
