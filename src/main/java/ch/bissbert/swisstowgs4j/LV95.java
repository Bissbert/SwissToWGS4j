package ch.bissbert.swisstowgs4j;

public class LV95 implements Coordinate {

    private final Double east;
    private final Double north;
    private Double height;

    public LV95(Double east, Double north) {
        this.east = east;
        this.north = north;
    }

    public LV95(Double east, Double north, Double height) {
        this.east = east;
        this.north = north;
        this.height = height;
    }

    public Double getEast() {
        return east;
    }

    public Double getNorth() {
        return north;
    }

    public Double getHeight() {
        return height;
    }

    @Override
    public WGS84 toWGS84() {
        Double[] wgs84data = Transformer.lv95ToWGS84(east, north, height);
        if (wgs84data[2] == null) {
            return new WGS84(wgs84data[0], wgs84data[1]);
        }
        return new WGS84(wgs84data[0], wgs84data[1], wgs84data[2]);
    }

    @Override
    public LV03 toLV03() {
        Double[] lv03data = Transformer.lv95ToLV03(east, north, height);
        if (lv03data[2] == null) {
            return new LV03(lv03data[0], lv03data[1]);
        }
        return new LV03(lv03data[0], lv03data[1], lv03data[2]);
    }

    @Override
    public LV95 toLV95() {
        return this;
    }
}
