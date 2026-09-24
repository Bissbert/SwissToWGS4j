package ch.bissbert.swisstowgs4j;

/** Converts angular differences into metres on the ground. */
final class Geo {
    /** Metres per degree of latitude (mean Earth radius 6,371 km). */
    static final double METRES_PER_DEGREE = 6_371_000 * Math.PI / 180;

    private Geo() {
    }

    /** Horizontal distance in metres between two WGS84 points close to each other. */
    static double distance(double lon1, double lat1, double lon2, double lat2) {
        double north = (lat2 - lat1) * METRES_PER_DEGREE;
        double east = (lon2 - lon1) * METRES_PER_DEGREE * Math.cos(Math.toRadians((lat1 + lat2) / 2));
        return Math.hypot(east, north);
    }
}
