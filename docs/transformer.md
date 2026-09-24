[← back to the overview](../README.md)

# Transformer

`Transformer` contains four static operations. Two are exact coordinate-origin
offsets between LV03 and LV95. The other two are polynomial approximations
between LV95 and WGS84.

```mermaid
flowchart LR
    A["LV03<br/>east, north"] -->|"+2,000,000 E<br/>+1,000,000 N"| B["LV95"]
    B -->|"normalise x/y<br/>then polynomial"| C["WGS84<br/>longitude, latitude"]
    C -->|"degrees to arcseconds<br/>then polynomial"| B
    B -->|"−2,000,000 E<br/>−1,000,000 N"| A

    style A fill:#9e6a03,stroke:#d29922,color:#fff
    style B fill:#238636,stroke:#3fb950,color:#fff
    style C fill:#8250df,stroke:#bc8cff,color:#fff
```

## LV03 and LV95

The two Swiss grid systems are related here by fixed offsets:

```java
LV95 = LV03 + (2_000_000 east, 1_000_000 north)
LV03 = LV95 - (2_000_000 east, 1_000_000 north)
```

The `height` reference is returned unchanged by these two methods. The
measurement runner sampled `888` points in both directions with a `10,000` m
step; the largest observed horizontal residual was `0.000000` m and the
height comparison was exact. These are round-trip results for the fixed
offset, not a geodetic accuracy claim.

## LV95 to WGS84

The forward method normalises the LV95 axes as follows:

```text
y = (east  - 2600000) / 1000000
x = (north - 1200000) / 1000000
```

It evaluates one polynomial for longitude and one for latitude, then converts
the intermediate values with `* 100 / 36`. When height is non-null, the source
applies:

```text
wgs84Height = height + 49.55 - 12.60*y - 22.64*x
```

The verified quick-start sample is:

```text
LV95 -> WGS84: lon=7.438637222 lat=46.951081111 height=null
```

## WGS84 to LV95

The inverse method converts both angles to arcseconds, builds `x` from
latitude and `y` from longitude, and evaluates the inverse polynomials. The
call takes decimal degrees in the declared `(longitude, latitude, height)`
order:

```java
Double[] lv95 = Transformer.wgs84ToLV95(7.438632, 46.951082, null);
```

The result is:

```text
E=2599999.9488 N=1199999.9296
```

`new WGS84(7.438632, 46.951082).toLV95()` now returns the same pair.

## Accuracy boundary

The two polynomial directions are approximations. The repository contains no
known reference-point test against an authoritative WGS84/LV95 data source,
so absolute accuracy is not covered. The forward longitude polynomial also has
a wrong cubic term (open entry 5 in [Bugs found](BUGS-FOUND.md)), which is why
the round trip reaches 148 m away from Bern. The `10,000` m grid result in the
[measurement report](measurement.md) is only the residual after passing values
through the two implemented polynomial formulas.
