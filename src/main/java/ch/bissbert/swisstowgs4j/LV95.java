package ch.bissbert.swisstowgs4j;

public class LV95 {

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

    public WGS84 toLV95(){
        Double[] wgs84data = Transformer.lv95ToWGS84(east, north, height);
        if (wgs84data[2] == null){
            return new WGS84(wgs84data[0], wgs84data[1]);
        }
        return new WGS84(wgs84data[0], wgs84data[1], wgs84data[2]);
    }
}
