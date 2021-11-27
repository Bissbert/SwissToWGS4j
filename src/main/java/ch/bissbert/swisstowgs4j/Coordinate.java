package ch.bissbert.swisstowgs4j;

/**
 * interface to ensure coordinates can be converted to other systems
 *
 * @author Bissbert
 * @version 1.0
 */
public interface Coordinate {
    /**
     * transforms the current coordinate into the {@link LV03} system using {@link Transformer}
     *
     * @return the transformed coordinate
     */
    LV03 toLV03();

    /**
     * transforms the current coordinate into the {@link LV95} system using {@link Transformer}
     *
     * @return the transformed coordinate
     */
    LV95 toLV95();

    /**
     * transforms the current coordinate into the {@link WGS84} system using {@link Transformer}
     *
     * @return the transformed coordinate
     */
    WGS84 toWGS84();
}
