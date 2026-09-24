[← back to the overview](../README.md)

# Documentation

The library is small enough to explain by subsystem. The measurement runner is
in `tools/` because this repository had no existing measurement-tool tree.

| | Topic | In one line |
|---|---|---|
| 1 | [Coordinate model](coordinate-model.md) | Three coordinate value types share one<br/>conversion interface. |
| 2 | [Transformer](transformer.md) | LV03/LV95 use an offset; LV95/WGS84 use<br/>polynomial approximations. |
| 3 | [Measurement](measurement.md) | The Linux container run: tests, build,<br/>probe output, and what was not covered. |

Bugs are tracked as [GitHub issues](https://github.com/Bissbert/SwissToWGS4j/issues?q=label%3Abug).

```mermaid
flowchart LR
    R["README overview"] --> C["Coordinate model"]
    R --> T["Transformer"]
    R --> M["Measurement"]
    M --> P["tools/Probe.java<br/>tools/measure.py"]
    M --> U["JUnit suite<br/>REFRAME reference points"]

    style R fill:#1f6feb,stroke:#58a6ff,color:#fff
    style M fill:#238636,stroke:#3fb950,color:#fff
```
