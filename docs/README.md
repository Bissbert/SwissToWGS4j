[← back to the overview](../README.md)

# Documentation

The library is small enough to explain by subsystem. The measurement runner is
in `tools/` because this repository had no existing measurement-tool tree.

| | Topic | In one line |
|---|---|---|
| 1 | [Coordinate model](coordinate-model.md) | Three coordinate value types share one<br/>conversion interface. |
| 2 | [Transformer](transformer.md) | LV03/LV95 use an offset; LV95/WGS84 use<br/>polynomial approximations. |
| 3 | [Bugs found](BUGS-FOUND.md) | Three fixed inverse-direction bugs<br/>and two open ones. |
| — | [Measurement](measurement.md) | The Linux container run: build, probe<br/>output, and what was not covered. |

```mermaid
flowchart LR
    R["README overview"] --> C["Coordinate model"]
    R --> T["Transformer"]
    R --> M["Measurement"]
    R --> B["Bugs found"]
    M --> P["tools/Probe.java<br/>tools/measure.py"]

    style R fill:#1f6feb,stroke:#58a6ff,color:#fff
    style M fill:#238636,stroke:#3fb950,color:#fff
    style B fill:#da3633,stroke:#f85149,color:#fff
```
