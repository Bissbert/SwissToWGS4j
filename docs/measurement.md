[← back to the overview](../README.md)

# How this was measured

Every result in the documentation comes from one script that runs in a Linux
container:

```sh
sh tools/linux-run.sh > media/captures/linux-run.txt
```

[`tools/linux-run.sh`](../tools/linux-run.sh) starts
`maven:3.9-eclipse-temurin-11`, mounts the repository read-only, copies it,
runs the JUnit suite, builds and installs the library with Maven, and runs
[`tools/measure.py`](../tools/measure.py). That script compiles the sources
together with [`tools/Probe.java`](../tools/Probe.java) using
`javac -Xlint:all` and prints what the probe measures. The full output is
[`media/captures/linux-run.txt`](../media/captures/linux-run.txt).

```mermaid
flowchart LR
    L["tools/linux-run.sh"] --> U["mvn test"]
    L --> B["mvn package, mvn install"]
    L --> P["python3 tools/measure.py"]
    P --> J["javac -Xlint:all<br/>src + Probe.java"]
    J --> R["java Probe"]
    R --> D["README and docs"]

    style L fill:#1f6feb,stroke:#58a6ff,color:#fff
    style R fill:#238636,stroke:#3fb950,color:#fff
```

## Environment

| | |
|---|---|
| Kernel | Linux 6.5.11-linuxkit, aarch64 (Docker Desktop VM) |
| Image | `maven:3.9-eclipse-temurin-11` (`sha256:72b9e4bb…a5d3d7`) |
| JDK | OpenJDK 11.0.32 (Eclipse Temurin) |
| Maven | 3.9.16 |
| Python | 3.12.3 |
| Date | 2026-09-24 |

## Tests

`mvn -B -Dgpg.skip=true test`:

```text
Tests run: 14, Failures: 0, Errors: 0, Skipped: 0  ReferencePointsTest
Tests run: 6, Failures: 0, Errors: 0, Skipped: 0  AxisOrderTest
Tests run: 6, Failures: 0, Errors: 0, Skipped: 0  HeightAndIdentityTest
Tests run: 2, Failures: 0, Errors: 0, Skipped: 0  RoundTripTest
Tests run: 28, Failures: 0, Errors: 0, Skipped: 0
```

`ReferencePointsTest` uses reference values from swisstopo's REFRAME web
service (`https://geodesy.geo.admin.ch/reframe/lv95towgs84` and
`.../wgs84tolv95`), queried on 2026-09-24 and written into the test as
constants, so the suite runs offline. Measured distances to REFRAME:

| Direction | Points | Largest distance |
|---|---|---:|
| LV95 → WGS84 | Bern, Zurich, Chur, near Geneva, near Lugano | 2.0 m (near Geneva) |
| LV95 → WGS84 | four grid corners in France, Germany and Italy | 4.3 m |
| WGS84 → LV95 | Bern, Zurich, near Lugano, two grid corners | 1.0 m |

The test tolerances are these values rounded up (2.5 m, 5 m, 1.5 m). With
either fix from [Bugs found](BUGS-FOUND.md) reverted, the suite fails: 9
failures without the `y³` fix (entry 5), 4 without the axis fix (entry 4).

## Build

The repository declares Java source and target level `11` in `pom.xml`.

| Command | Result |
|---|---|
| `mvn -q -Dgpg.skip=true package` | exit 0; `SwissToWGS4j-1.0.jar`, `-sources.jar` and `-javadoc.jar` in `target/` |
| `mvn -q -Dgpg.skip=true install` | exit 0; the three jars in `~/.m2/repository/ch/bissbert/SwissToWGS4j/1.0/` |

The install step is what the README's dependency example needs, because the
`pom.xml` does not configure a remote artifact repository.

## Probe output

```text
compiler.warning_count=0
quickstart:
LV95 -> WGS84: lon=7.438637222 lat=46.951081111 height=null
WGS84 -> LV95: E=2599999.9488 N=1199999.9296 height=null
inverse:
static wgs84ToLV95, decimal degrees: E=2599999.9488 N=1199999.9296 h=null
WGS84.toLV95, no height -> LV95[E=2599999.9488 N=1199999.9296 h=null]
WGS84.toLV03, no height -> LV03[north=199999.9296 east=599999.9488 h=null]
roundtrip:
roundtrip.step_m=10000
roundtrip.points=888
roundtrip.max_abs_east_m=3.752119
roundtrip.max_abs_north_m=3.016353
roundtrip.max_horizontal_m=4.721508
roundtrip.path=static methods, decimal degrees
shift:
shift.step_m=10000
shift.points=888
shift.max_abs_east_m=0.000000
shift.max_abs_north_m=0.000000
shift.height_exact=true
api:
LV95.toWGS84 -> WGS84[lon=7.438637222 lat=46.951081111 h=589.55]
LV95.toLV03 -> LV03[north=200000.0000 east=600000.0000 h=540.0]
LV03.toLV95 -> LV95[E=2600000.0000 N=1200000.0000 h=540.0]
LV03.toWGS84 -> WGS84[lon=7.438637222 lat=46.951081111 h=589.55]
WGS84.toLV95 -> LV95[E=2599999.9488 N=1199999.9296 h=490.44575825839996]
WGS84.toLV03 -> LV03[north=199999.9296 east=599999.9488 h=490.44575825839996]
absolute_accuracy=see mvn test (swisstopo REFRAME reference points)
```

## What the output shows

- **Compiler:** no warnings under `-Xlint:all`.
- **Inverse direction:** the static method and the object path both return
  `E=2599999.9488 N=1199999.9296` for a point in Bern. A missing height stays
  `null`. These are the fixes for entries 1 and 2 in
  [Bugs found](BUGS-FOUND.md).
- **LV03/LV95 shift:** 888 grid points at a `10,000` m step, converted there and
  back with a height of `500.0`. The horizontal residual is `0.000000` m and the
  height comes back unchanged.
- **LV95/WGS84 round trip:** the same grid in LV95 metres, converted to WGS84
  and back with the static methods in decimal degrees. The largest residual is
  4.72 m, at the grid corner E 2,480,000 / N 1,300,000 outside Switzerland; it
  was 148.44 m before the `y³` fix (entry 5). The residual compares the two
  implemented formulas with each other; the REFRAME comparison above is the
  absolute check.
- **`api:` block:** `LV95.toLV03` and `WGS84.toLV03` report north and east in
  the right fields (entry 4).

## Not covered

- Heights against a reference. REFRAME's height conversion needs a geoid model
  that the approximate formulas do not use, so the tests check the height
  terms against swisstopo's approximate formula only.
- The Javadoc's accuracy claim (0.12" latitude, 0.8" longitude) was not
  treated as a measurement; the REFRAME distances above are.
