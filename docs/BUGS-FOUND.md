[← back to the overview](../README.md)

# Bugs found

| # | Entry | Status |
|---|---|---|
| 1 | The inverse polynomial used the wrong units and order | Fixed in [`e532cde`](https://github.com/Bissbert/SwissToWGS4j/commit/e532cde) |
| 2 | `WGS84.toLV95()` read past the returned array | Fixed in [`b7b4bd9`](https://github.com/Bissbert/SwissToWGS4j/commit/b7b4bd9) |
| 3 | The inverse method's Javadoc left the angular unit unspecified | Fixed in [`078e378`](https://github.com/Bissbert/SwissToWGS4j/commit/078e378) |
| 4 | `LV95.toLV03()` swaps north and east | Open |
| 5 | The forward polynomial uses `x³` where the longitude term needs `y³` | Open |

Entries 4 and 5 turned up when the probe was re-run in a Linux container after
the first three fixes (see [How this was measured](measurement.md)). The
outputs below come from that run.

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

## 4 — `LV95.toLV03()` swaps north and east

**Status:** open.

**File:** `src/main/java/ch/bissbert/swisstowgs4j/LV95.java:48-54`

**What happens:** `Transformer.lv95ToLV03()` returns `[east, north, height]`.
`toLV03()` passes those into `new LV03(lv03data[0], lv03data[1])`, but the
`LV03` constructor takes `(north, east)`. The result has its axes swapped.
`WGS84.toLV03()` goes through `toLV95().toLV03()` and inherits the swap.
`LV03.toLV95()` is correct.

**Reproduce:** `python3 tools/measure.py`, `api:` block:

```text
LV95.toLV03 -> LV03[north=600000.0000 east=200000.0000 h=540.0]
LV03.toLV95 -> LV95[E=2600000.0000 N=1200000.0000 h=540.0]
WGS84.toLV03 -> LV03[north=599999.9488 east=199999.9296 h=490.44575825839996]
```

Bern is at LV03 east 600,000, north 200,000. `LV03(200000, 600000)` converts
to the right LV95 point, while `LV95(2600000, 1200000).toLV03()` reports
north 600,000.

**Possible fix:** construct the result as
`new LV03(lv03data[1], lv03data[0], ...)`.

## 5 — The forward polynomial uses `x³` where the longitude term needs `y³`

**Status:** open.

**File:** `src/main/java/ch/bissbert/swisstowgs4j/Transformer.java:20-24`
(`lv95ToWGS84`)

**What happens:** the longitude polynomial ends with `- 0.0436 * x³`. In
swisstopo's approximate LV95 → WGS84 formula this term is `- 0.0436 · y'³`,
where `y'` is the east offset. Near Bern both are close to zero, so the quick
start looks right. Far from Bern the error grows, and the LV95 → WGS84 → LV95
round trip over the sample grid is off by up to 148 m:

```text
roundtrip.max_abs_east_m=148.254882
roundtrip.max_abs_north_m=7.482338
roundtrip.max_horizontal_m=148.443577
```

A standalone copy of both formulas with `y³` in place of `x³`, and nothing
else changed, gives a maximum round-trip residual of 4.72 m on the same grid, at the grid corner
E 2,480,000 / N 1,300,000, which lies outside Switzerland.

**Possible fix:** replace `Math.pow(x, 3)` with `Math.pow(y, 3)` in the
longitude term, then check against published reference points.
