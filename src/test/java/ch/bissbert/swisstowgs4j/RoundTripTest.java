package ch.bissbert.swisstowgs4j;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * LV95 -> WGS84 -> LV95 over the grid tools/Probe.java samples. With the x³
 * term in the longitude polynomial (issue #6) the residual reached 148 m.
 */
class RoundTripTest {

    /** Largest residual over the whole grid, including corners outside Switzerland. */
    static final double GRID_TOLERANCE_M = 5.0;

    @Test
    void roundTripOverSwissGrid() {
        double worst = 0;
        String where = "";
        for (double east = 2_480_000; east <= 2_840_000; east += 10_000) {
            for (double north = 1_070_000; north <= 1_300_000; north += 10_000) {
                LV95 back = new LV95(east, north).toWGS84().toLV95();
                double error = Math.hypot(back.getEast() - east, back.getNorth() - north);
                if (error > worst) {
                    worst = error;
                    where = east + "/" + north;
                }
            }
        }
        double max = worst;
        String at = where;
        assertTrue(max < GRID_TOLERANCE_M, () -> "worst residual " + max + " m at " + at);
    }

    @Test
    void longitudeTermUsesEastOffset() {
        // 300 km east of Bern, on the projection's north line: x = 0, so an
        // x³ term would vanish and y³ is the only cubic contribution.
        WGS84 point = new LV95(2_840_000.0, 1_200_000.0).toWGS84();
        LV95 back = point.toLV95();
        assertTrue(Math.abs(back.getEast() - 2_840_000) < 1.0, () -> "east residual " + (back.getEast() - 2_840_000));
    }
}
