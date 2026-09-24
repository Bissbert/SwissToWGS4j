[← back to the overview](../README.md)

# Bugs found

| # | Entry | Status |
|---|---|---|
| 1 | The inverse polynomial used the wrong units and order | Fixed in [`e532cde`](https://github.com/Bissbert/SwissToWGS4j/commit/e532cde) |
| 2 | `WGS84.toLV95()` read past the returned array | Fixed in [`b7b4bd9`](https://github.com/Bissbert/SwissToWGS4j/commit/b7b4bd9) |
| 3 | The inverse method's Javadoc left the angular unit unspecified | Fixed in [`078e378`](https://github.com/Bissbert/SwissToWGS4j/commit/078e378) |
| 4 | `LV95.toLV03()` swapped north and east | Fixed in [`c929f7e`](https://github.com/Bissbert/SwissToWGS4j/commit/c929f7e) |
| 5 | The forward polynomial used `x³` where the longitude term needs `y³` | Fixed in [`c929f7e`](https://github.com/Bissbert/SwissToWGS4j/commit/c929f7e) |

Entries 4 and 5 turned up when the probe was re-run in a Linux container after
the first three fixes (see [How this was measured](measurement.md)). Both have
regression tests in the JUnit suite under
[`src/test/java`](../src/test/java/ch/bissbert/swisstowgs4j), which fail when
either fix is reverted.

## 1 — The inverse polynomial used the wrong units and order

**Status:** fixed in [`e532cde`](https://github.com/Bissbert/SwissToWGS4j/commit/e532cde).

**File:** `src/main/java/ch/bissbert/swisstowgs4j/Transformer.java`
(`wgs84ToLV95`)

**What happened:** the parameters are named `longitude` and `latitude` and
`WGS84` stores decimal degrees, but the arithmetic subtracted arcsecond-scale
constants without converting, and fed longitude into the latitude term. Called
with decimal degrees, the method returned `E=1541561.8610 N=-4525615.7729`
for a point in Bern.

**What changed:** `x` is now built from latitude and `y` from longitude,
both multiplied by `3600` first. The public `(longitude, latitude, height)`
order is unchanged:

```text
static wgs84ToLV95, decimal degrees: E=2599999.9488 N=1199999.9296 h=null
```

## 2 — `WGS84.toLV95()` read past the returned array

**Status:** fixed in [`b7b4bd9`](https://github.com/Bissbert/SwissToWGS4j/commit/b7b4bd9).

**File:** `src/main/java/ch/bissbert/swisstowgs4j/WGS84.java` (`toLV95`)

**What happened:** `Transformer.wgs84ToLV95()` returns three elements, but
`toLV95()` checked element `3`, so every call threw
`ArrayIndexOutOfBoundsException: Index 3 out of bounds for length 3`.
`WGS84.toLV03()` failed the same way because it delegates to `toLV95()`.

**What changed:** the height check reads element `2`. Both calls now return,
and a missing height stays `null`:

```text
WGS84.toLV95, no height -> LV95[E=2599999.9488 N=1199999.9296 h=null]
```

## 3 — The inverse method's Javadoc left the angular unit unspecified

**Status:** fixed in [`078e378`](https://github.com/Bissbert/SwissToWGS4j/commit/078e378).

**What happened:** the parameters were documented only as "in WGS84".

**What changed:** both now say "in WGS84 decimal degrees".

## 4 — `LV95.toLV03()` swapped north and east

**Status:** fixed in [`c929f7e`](https://github.com/Bissbert/SwissToWGS4j/commit/c929f7e) ([#5](https://github.com/Bissbert/SwissToWGS4j/issues/5)).

**File:** `src/main/java/ch/bissbert/swisstowgs4j/LV95.java:48-54`

**What happened:** `Transformer.lv95ToLV03()` returns `[east, north, height]`.
`toLV03()` passed those into `new LV03(lv03data[0], lv03data[1])`, but the
`LV03` constructor takes `(north, east)`, so the result had its axes swapped.
`WGS84.toLV03()` goes through `toLV95().toLV03()` and inherited the swap.
`LV03.toLV95()` was correct. Output before the fix, `api:` block of
`python3 tools/measure.py`:

```text
LV95.toLV03 -> LV03[north=600000.0000 east=200000.0000 h=540.0]
LV03.toLV95 -> LV95[E=2600000.0000 N=1200000.0000 h=540.0]
WGS84.toLV03 -> LV03[north=599999.9488 east=199999.9296 h=490.44575825839996]
```

Bern is at LV03 east 600,000, north 200,000. `LV03(200000, 600000)` converted
to the right LV95 point, while `LV95(2600000, 1200000).toLV03()` reported
north 600,000.

**What changed:** the result is built as `new LV03(lv03data[1], lv03data[0], ...)`.
`AxisOrderTest` checks both directions, the LV03 → LV95 → LV03 round trip and
`WGS84.toLV03()` against the swisstopo REFRAME position of Bern. The Linux run
now shows:

```text
LV95.toLV03 -> LV03[north=200000.0000 east=600000.0000 h=540.0]
WGS84.toLV03 -> LV03[north=199999.9296 east=599999.9488 h=490.44575825839996]
```

## 5 — The forward polynomial used `x³` where the longitude term needs `y³`

**Status:** fixed in [`c929f7e`](https://github.com/Bissbert/SwissToWGS4j/commit/c929f7e) ([#6](https://github.com/Bissbert/SwissToWGS4j/issues/6)).

**File:** `src/main/java/ch/bissbert/swisstowgs4j/Transformer.java:20-24`
(`lv95ToWGS84`)

**What happened:** the longitude polynomial ended with `- 0.0436 * x³`. In
swisstopo's approximate LV95 → WGS84 formula this term is `- 0.0436 · y'³`,
where `y'` is the east offset. Near Bern both are close to zero, so the quick
start looked right. Far from Bern the error grew, and the LV95 → WGS84 → LV95
round trip over the sample grid was off by up to 148 m:

```text
roundtrip.max_abs_east_m=148.254882
roundtrip.max_abs_north_m=7.482338
roundtrip.max_horizontal_m=148.443577
```

**What changed:** the term is `Math.pow(y, 3)`. The Linux run now shows:

```text
roundtrip.max_abs_east_m=3.752119
roundtrip.max_abs_north_m=3.016353
roundtrip.max_horizontal_m=4.721508
```

The worst point is the grid corner E 2,480,000 / N 1,300,000, which lies
outside Switzerland. `ReferencePointsTest` compares both directions with
swisstopo's REFRAME service: the forward formula is within 2.0 m at the five
points in Switzerland and within 4.3 m at the four grid corners abroad; the
inverse is within 1.0 m. With `x³` restored, nine tests fail, including
Zurich at 4.2 m and Chur at 34.3 m from the reference.
