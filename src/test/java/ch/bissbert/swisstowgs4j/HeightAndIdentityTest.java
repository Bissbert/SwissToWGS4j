package ch.bissbert.swisstowgs4j;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Height handling and conversions to the same system. The height terms are
 * the ones in swisstopo's approximate formulas:
 * h = H + 49.55 - 12.60 y' - 22.64 x' and back H = h - 49.55 + 2.73 y + 6.94 x.
 */
class HeightAndIdentityTest {

    @Test
    void heightAtOriginAddsGeoidOffset() {
        assertEquals(589.55, new LV95(2_600_000.0, 1_200_000.0, 540.0).toWGS84().getHeight(), 1e-9);
    }

    @Test
    void heightAwayFromOrigin() {
        // y' = 0.1, x' = -0.1: 500 + 49.55 - 1.26 + 2.264
        assertEquals(550.554, new LV95(2_700_000.0, 1_100_000.0, 500.0).toWGS84().getHeight(), 1e-9);
    }

    @Test
    void heightRoundTripWithinDecimetre() {
        double height = new LV95(2_683_000.0, 1_248_000.0, 400.0).toWGS84().toLV95().getHeight();
        assertEquals(400.0, height, 0.1);
    }

    @Test
    void missingHeightStaysNull() {
        assertNull(new LV95(2_600_000.0, 1_200_000.0).toWGS84().getHeight());
        assertNull(new WGS84(7.4386, 46.9511).toLV95().getHeight());
        assertNull(new WGS84(7.4386, 46.9511).toLV03().getHeight());
        assertNull(new LV03(200_000, 600_000).toWGS84().getHeight());
    }

    @Test
    void sameSystemReturnsItself() {
        LV95 lv95 = new LV95(2_600_000.0, 1_200_000.0);
        LV03 lv03 = new LV03(200_000, 600_000);
        WGS84 wgs84 = new WGS84(7.4386, 46.9511);
        assertSame(lv95, lv95.toLV95());
        assertSame(lv03, lv03.toLV03());
        assertSame(wgs84, wgs84.toWGS84());
    }

    @Test
    void staticShiftIsConstant() {
        Double[] lv03 = Transformer.lv95ToLV03(2_700_000, 1_100_000, 12.5);
        assertEquals(700_000, lv03[0], 0);
        assertEquals(100_000, lv03[1], 0);
        assertEquals(12.5, lv03[2]);
        Double[] lv95 = Transformer.lv03ToLV95(700_000, 100_000, null);
        assertEquals(2_700_000, lv95[0], 0);
        assertEquals(1_100_000, lv95[1], 0);
        assertNull(lv95[2]);
    }
}
