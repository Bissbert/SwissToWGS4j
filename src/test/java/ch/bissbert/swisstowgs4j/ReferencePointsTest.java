package ch.bissbert.swisstowgs4j;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Compares the approximate formulas with swisstopo's rigorous transformation.
 *
 * <p>The reference values come from swisstopo's REFRAME web service
 * (https://geodesy.geo.admin.ch/reframe/lv95towgs84 and .../wgs84tolv95,
 * queried 2026-09-24). swisstopo publishes the polynomials in
 * "Approximate formulas for the transformation between Swiss projection
 * coordinates and WGS84"; the tolerances below are the measured
 * differences to REFRAME, rounded up.</p>
 */
class ReferencePointsTest {

    /** Points in Switzerland: the forward polynomial is within 2.0 m of REFRAME there. */
    static final double INSIDE_M = 2.5;
    /** Corners of the sample grid, outside Switzerland, where the polynomial drifts to 4.3 m. */
    static final double CORNER_M = 5.0;
    /** The inverse polynomial is within 1.0 m of REFRAME at every point below. */
    static final double INVERSE_M = 1.5;

    @ParameterizedTest(name = "LV95 E={0} N={1}")
    @CsvSource({
            // east,  north,   REFRAME longitude,  REFRAME latitude
            "2600000, 1200000, 7.438632495274896,  46.951082876677035", // Bern, projection origin
            "2683000, 1248000, 8.537690383974429,  47.37760732422481",  // Zurich
            "2755000, 1190000, 9.47087313530997,   46.84307706747478",  // Chur
            "2510000, 1120000, 6.272117408757257,  46.225409677023066", // near Geneva
            "2700000, 1100000, 8.730497075536219,  46.04413033869967",  // near Lugano
    })
    void lv95ToWgs84MatchesReframeInSwitzerland(double east, double north, double lon, double lat) {
        assertForward(east, north, lon, lat, INSIDE_M);
    }

    @ParameterizedTest(name = "LV95 E={0} N={1}")
    @CsvSource({
            "2480000, 1075000, 5.894854106621189,  45.81600565540571",  // south-west corner, France
            "2840000, 1300000, 10.643213102935634, 47.80640578137515",  // north-east corner, Germany
            "2500000, 1290000, 6.105023551142973,  47.752903822986426", // north-west, France
            "2830000, 1080000, 10.39901421311893,  45.83258095768311",  // south-east, Italy
    })
    void lv95ToWgs84MatchesReframeAtGridCorners(double east, double north, double lon, double lat) {
        assertForward(east, north, lon, lat, CORNER_M);
    }

    private static void assertForward(double east, double north, double lon, double lat, double tolerance) {
        WGS84 result = new LV95(east, north).toWGS84();
        double error = Geo.distance(result.getLongitude(), result.getLatitude(), lon, lat);
        assertTrue(error < tolerance, () -> "off by " + error + " m");
    }

    @ParameterizedTest(name = "WGS84 lon={0} lat={1}")
    @CsvSource({
            // longitude,         latitude,           REFRAME east,      REFRAME north
            "7.438632495274896,  46.951082876677035, 2600000.000562095, 1200000.0011454732",
            "8.537690383974429,  47.37760732422481,  2683000.00061745,  1248000.0011388387",
            "8.730497075536219,  46.04413033869967,  2700000.0006753285, 1100000.0010735355",
            "5.894854106621189,  45.81600565540571,  2480000.0004772563, 1075000.001111293",
            "10.643213102935634, 47.80640578137515,  2840000.0007102676, 1300000.0010962542",
    })
    void wgs84ToLv95MatchesReframe(double lon, double lat, double east, double north) {
        LV95 result = new WGS84(lon, lat).toLV95();
        double error = Math.hypot(result.getEast() - east, result.getNorth() - north);
        assertTrue(error < INVERSE_M, () -> "off by " + error + " m");
    }
}
