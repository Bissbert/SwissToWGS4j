package ch.bissbert.swisstowgs4j;

public class Transformer {
    /**
     * converts LV95 coordinates to WGS84
     * <p>
     * This method creates an estimate of the LV95 coordinates to WGS84 which is better than 0.12" in latitude and better than 0.8" in longitude
     *
     * @param east   east in LV95
     * @param north  north in LV95
     * @param height height in LV95
     * @return an array containing the estimate in the lv95 system
     * <p>
     * [0] = longitude, [1] = latitude, [2] = height(null if nothing passed)
     */
    public static Double[] lv95ToWGS84(double east, double north, Double height) {
        double y = (east - 2600000) / 1000000;
        double x = (north - 1200000) / 1000000;

        double longitudeM = 2.6779094
                + (4.728982 * y)
                + (0.791484 * y * x)
                + (0.1306 * y * Math.pow(x, 2))
                - (0.0436 * Math.pow(x, 3));

        double latitudeM = 16.9023892
                + (3.238272 * x)
                - (0.270978 * Math.pow(y, 2))
                - (0.002528 * Math.pow(x, 2))
                - (0.0447 * Math.pow(y, 2) * x)
                - (0.0140 * Math.pow(x, 3));

        Double[] pair = new Double[3];
        pair[0] = longitudeM * 100 / 36;//longitude
        pair[1] = latitudeM * 100 / 36;//latitude

        if (height != null) {
            pair[2] = height + 49.55
                    - 12.60 * y
                    - 22.64 * x;
        }

        return pair;
    }

    /**
     * creates the LV95 coordinates from the WGS84 data
     *
     * @param longitude longitude in WGS84
     * @param latitude  latitude in WGS84
     * @param height    height in WGS84
     * @return an array containing the estimate in LV95
     * <p>
     * [0] = east, [1] = north, [2] = height(null if nothing passed)
     */
    public static Double[] wgs84ToLV95(double longitude, double latitude, Double height) {

        Double[] data = new Double[3];

        double x = (longitude - 169028.66) / 10000;
        double y = (latitude - 26782.5) / 10000;

        //east
        data[0] = 2600072.37d
                + (211455.93d * y)
                - (10938.51d * y * x)
                - (0.36d * y * Math.pow(x, 2))
                - (44.54d * Math.pow(y, 3));

        //north
        data[1] = 1200147.07d
                + (308807.95d * x)
                + (3745.25d * Math.pow(y, 2))
                + (76.63d * Math.pow(x, 2))
                - (194.56d * Math.pow(y, 2) * x)
                + (119.79d * Math.pow(x, 3));

        if (height != null) {
            data[2] = height - 49.55
                    + (2.73 * y)
                    + (6.94 * x);
        }

        return data;
    }

    public static Double[] lv95ToLV03(double east, double north, Double height) {
        double newEast = east - 2000000.000;
        double newNorth = north - 1000000.000;
        return new Double[]{newEast, newNorth, height};
    }

    public static Double[] lv03ToLV95(double east, double north, Double height) {
        double newEast = east + 2_000_000;
        double newNorth = north + 1_000_000;
        return new Double[]{newEast, newNorth, height};
    }
}
