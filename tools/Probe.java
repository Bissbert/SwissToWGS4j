import ch.bissbert.swisstowgs4j.LV03;
import ch.bissbert.swisstowgs4j.LV95;
import ch.bissbert.swisstowgs4j.Transformer;
import ch.bissbert.swisstowgs4j.WGS84;

import java.util.Locale;

/**
 * Measurement probe for the current SwissToWGS4j API.
 *
 * <p>This class calls the published API and the published static transformer
 * methods. It does not modify or mock code in {@code src/}. The Python wrapper
 * in {@code tools/measure.py} compiles this probe from the current sources.</p>
 */
public final class Probe {

    /** Swiss national extent used for the round-trip sample, in LV95 metres. */
    static final double E_MIN = 2_480_000;
    static final double E_MAX = 2_840_000;
    static final double N_MIN = 1_070_000;
    static final double N_MAX = 1_300_000;

    private Probe() {
    }

    public static void main(String[] args) {
        String mode = args.length == 0 ? "quickstart" : args[0];
        switch (mode) {
            case "quickstart":
                quickstart();
                break;
            case "bugs":
                bugs();
                break;
            case "roundtrip":
                roundtrip(Double.parseDouble(args[1]));
                break;
            case "shift":
                shift(Double.parseDouble(args[1]));
                break;
            case "api":
                api();
                break;
            default:
                throw new IllegalArgumentException("Unknown mode: " + mode);
        }
    }

    static void quickstart() {
        LV95 lv95 = new LV95(2_600_000.0, 1_200_000.0);
        WGS84 wgs84 = lv95.toWGS84();
        System.out.printf(Locale.ROOT,
                "LV95 -> WGS84: lon=%.9f lat=%.9f height=%s%n",
                wgs84.getLongitude(), wgs84.getLatitude(), wgs84.getHeight());

        try {
            new WGS84(7.438632, 46.951082).toLV95();
            System.out.println("WGS84 -> LV95: returned normally");
        } catch (Throwable exception) {
            System.out.println("WGS84 -> LV95: !! " + exception);
        }
    }

    static void bugs() {
        double longitude = 7.438632;
        double latitude = 46.951082;
        Double[] decimal = Transformer.wgs84ToLV95(longitude, latitude, null);
        Double[] seconds = Transformer.wgs84ToLV95(
                latitude * 3600.0, longitude * 3600.0, null);

        System.out.printf(Locale.ROOT,
                "direct decimal degrees: E=%.4f N=%.4f%n",
                decimal[0], decimal[1]);
        System.out.printf(Locale.ROOT,
                "direct lat/lon arcseconds: E=%.4f N=%.4f%n",
                seconds[0], seconds[1]);
        try {
            new WGS84(longitude, latitude).toLV95();
            System.out.println("object conversion: returned normally");
        } catch (Throwable exception) {
            System.out.println("object conversion: !! " + exception);
        }
    }

    static void roundtrip(double step) {
        int points = 0;
        double maxEast = 0.0;
        double maxNorth = 0.0;
        double maxHorizontal = 0.0;

        for (double east = E_MIN; east <= E_MAX; east += step) {
            for (double north = N_MIN; north <= N_MAX; north += step) {
                Double[] wgs84 = Transformer.lv95ToWGS84(east, north, null);
                Double[] back = Transformer.wgs84ToLV95(
                        wgs84[1] * 3600.0, wgs84[0] * 3600.0, null);
                double eastError = Math.abs(back[0] - east);
                double northError = Math.abs(back[1] - north);
                double horizontalError = Math.hypot(eastError, northError);
                maxEast = Math.max(maxEast, eastError);
                maxNorth = Math.max(maxNorth, northError);
                maxHorizontal = Math.max(maxHorizontal, horizontalError);
                points++;
            }
        }

        System.out.printf(Locale.ROOT, "roundtrip.step_m=%.0f%n", step);
        System.out.println("roundtrip.points=" + points);
        System.out.printf(Locale.ROOT, "roundtrip.max_abs_east_m=%.6f%n", maxEast);
        System.out.printf(Locale.ROOT, "roundtrip.max_abs_north_m=%.6f%n", maxNorth);
        System.out.printf(Locale.ROOT, "roundtrip.max_horizontal_m=%.6f%n", maxHorizontal);
        System.out.println("roundtrip.path=static methods with lat/lon arcseconds");
    }

    static void shift(double step) {
        int points = 0;
        double maxEast = 0.0;
        double maxNorth = 0.0;
        boolean heightExact = true;

        for (double east = 480_000; east <= 840_000; east += step) {
            for (double north = 70_000; north <= 300_000; north += step) {
                Double height = 500.0;
                Double[] lv95 = Transformer.lv03ToLV95(east, north, height);
                Double[] back = Transformer.lv95ToLV03(lv95[0], lv95[1], lv95[2]);
                maxEast = Math.max(maxEast, Math.abs(back[0] - east));
                maxNorth = Math.max(maxNorth, Math.abs(back[1] - north));
                heightExact &= height.equals(back[2]);
                points++;
            }
        }

        System.out.printf(Locale.ROOT, "shift.step_m=%.0f%n", step);
        System.out.println("shift.points=" + points);
        System.out.printf(Locale.ROOT, "shift.max_abs_east_m=%.6f%n", maxEast);
        System.out.printf(Locale.ROOT, "shift.max_abs_north_m=%.6f%n", maxNorth);
        System.out.println("shift.height_exact=" + heightExact);
    }

    static void api() {
        LV95 bern = new LV95(2_600_000.0, 1_200_000.0, 540.0);
        line("LV95.toWGS84", () -> format(bern.toWGS84()));
        line("LV95.toLV03", () -> format(bern.toLV03()));

        LV03 lv03 = new LV03(200_000.0, 600_000.0, 540.0);
        line("LV03.toLV95", () -> format(lv03.toLV95()));
        line("LV03.toWGS84", () -> format(lv03.toWGS84()));

        WGS84 wgs84 = new WGS84(7.438632, 46.951082, 540.0);
        line("WGS84.toLV95", () -> format(wgs84.toLV95()));
        line("WGS84.toLV03", () -> format(wgs84.toLV03()));
    }

    interface Call {
        String get();
    }

    static void line(String label, Call call) {
        try {
            System.out.println(label + " -> " + call.get());
        } catch (Throwable exception) {
            System.out.println(label + " -> !! " + exception);
        }
    }

    static String format(Object coordinate) {
        if (coordinate instanceof LV95) {
            LV95 value = (LV95) coordinate;
            return String.format(Locale.ROOT, "LV95[E=%.4f N=%.4f h=%s]",
                    value.getEast(), value.getNorth(), value.getHeight());
        }
        if (coordinate instanceof LV03) {
            LV03 value = (LV03) coordinate;
            return String.format(Locale.ROOT, "LV03[north=%.4f east=%.4f h=%s]",
                    value.getNorth(), value.getEast(), value.getHeight());
        }
        WGS84 value = (WGS84) coordinate;
        return String.format(Locale.ROOT, "WGS84[lon=%.9f lat=%.9f h=%s]",
                value.getLongitude(), value.getLatitude(), value.getHeight());
    }
}
