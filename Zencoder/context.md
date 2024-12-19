### Explanation

- **Package Name**: Replace `yourname` with a single word based on your name.
- **Imports**: We use standard Java imports only, as no external dependencies are allowed.
- **BufferedReader**: Efficiently reads the file line by line.
- **ConcurrentHashMap**: Used to store temperature statistics for each station, allowing concurrent updates.
- **DoubleAdder and LongAdder**: Used for efficient concurrent accumulation of temperature sums and counts.
- **Parallel Streams**: Utilized to process lines concurrently, leveraging multiple CPU cores.
- **TemperatureStats Class**: Maintains min, max, and mean calculations for each station.
- **Output**: Results are printed in alphabetical order of station names.

This solution is optimized for speed and leverages modern Java features to handle large datasets efficiently.
