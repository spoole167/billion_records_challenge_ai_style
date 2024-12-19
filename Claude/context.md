### Key Performance Optimization Strategies

#### Concurrency Optimizations

- **Uses `parallel()` streams** for processing.
- Configured thread pool matching available processors.
- `ConcurrentHashMap` for thread-safe aggregations.
- Minimal synchronization overhead.

---

#### Memory Efficiency

- Lightweight `StationStats` class.
- Single-pass processing.
- Avoids unnecessary object creation.
- In-place statistical computations.

---

#### Processing Strategy

- Reads file line-by-line with `BufferedReader`.
- Filters out comment lines.
- Immediate parsing and aggregation.
- Minimal intermediate data structures.

---

#### Error Handling

- Validates input file argument.
- Skips malformed lines.
- Provides processing time metrics.

---

### Performance Expectations

- Designed for 1B row files.
- `O(n)` time complexity.
- Low memory footprint.
- Scalable across multi-core systems.

---

### Compile & Run

```bash
javac -source 21 TemperatureProcessor.java
java TemperatureProcessor measurements.txt
```

---

Would you like me to elaborate on any specific aspect of the implementation?
