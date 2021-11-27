package ch.bissbert.swisstowgs4j;

/**
 * Coordinate in the LV03 system
 *
 * @author Bissbert
 * @version 1.0
 */
public class LV03 implements Coordinate {
    private final double north;
    private final double east;
    private Double height;

    public LV03(double north, double east, Double height) {
        this.north = north;
        this.east = east;
        this.height = height;
    }

    public LV03(double north, double east) {
        this.north = north;
        this.east = east;
    }

    public double getNorth() {
        return north;
    }

    public double getEast() {
        return east;
    }

    public Double getHeight() {
        return height;
    }

    @Override
    public LV03 toLV03() {
        return this;
    }

    @Override
    public LV95 toLV95() {
        Double[] lv95data = Transformer.lv03ToLV95(east, north, height);
        if (lv95data[2] == null) {
            return new LV95(lv95data[0], lv95data[1]);
        }
        return new LV95(lv95data[0], lv95data[1], lv95data[2]);
    }

    @Override
    public WGS84 toWGS84() {
        Double[] lv95data = Transformer.lv03ToLV95(east, north, height);
        Double[] wgs84data = Transformer.lv95ToWGS84(lv95data[0], lv95data[1], lv95data[2]);
        if (wgs84data[2] == null){
            return new WGS84(wgs84data[0], wgs84data[1]);
        }
        return new WGS84(wgs84data[0], wgs84data[1], wgs84data[2]);
    }
}
