package ch.bissbert.swisstowgs4j;

public interface Coordinate {
    LV03 toLV03();
    LV95 toLV95();
    WGS84 toWGS84();
}
