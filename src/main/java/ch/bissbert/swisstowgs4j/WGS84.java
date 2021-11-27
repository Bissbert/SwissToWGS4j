package ch.bissbert.swisstowgs4j;

public class WGS84 {
    private final Double longitude;
    private final Double latitude;
    private Double height;

    public WGS84(Double longitude, Double latitude, Double height) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.height = height;
    }

    public WGS84(Double longitude, Double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getHeight() {
        return height;
    }

    public LV95 toLV95() {
        Double[] lv95Data = Transformer.wgs84ToLV95(longitude, latitude, height);
        if (lv95Data[3] == null) {
            return new LV95(lv95Data[0], lv95Data[1]);
        }
        return new LV95(lv95Data[0], lv95Data[1], lv95Data[2]);
    }
}
