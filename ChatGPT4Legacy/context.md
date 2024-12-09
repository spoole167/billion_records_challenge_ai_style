
# Java Program: Efficient Processing of Large Temperature Data Files

Below is a Java program designed to efficiently process a very large text file containing temperature data, with each row formatted as `{Station;Temperature}`. The program calculates the minimum, mean, and maximum temperature for each weather station and prints the results sorted alphabetically by the station name. It uses modern Java features to optimize for speed, including multi-threading and concurrent collections.

## Key Points

1. **Concurrency**:
    - Uses a `ConcurrentHashMap` to store temperature statistics.
    - Leverages an `ExecutorService` to process lines in parallel, enhancing performance on large files.

2. **Error Handling**:
    - Gracefully handles formatting issues and non-numeric temperature entries without crashing.

3. **Output**:
    - Results are sorted alphabetically by station name.
    - Output is formatted as specified, providing clear and accurate statistics.

4. **Modern Java Features**:
    - Leverages recent Java concurrency features for efficiency.
    - Tested with Java 19 but compatible with other modern Java versions.

## Notes

- Adjust the Java version in the `@version` tag if using a different Java SDK.
- This program assumes a modern environment optimized for concurrency and large file handling.

## Compilation and Execution

To compile and run the program:

```bash
javac --enable-preview --release 19 TemperatureProcessor.java
java --enable-preview TemperatureProcessor input.txt
```

## Additional Considerations

- Test with different Java versions to ensure compatibility.
- Customize thread pool size in `ExecutorService` for optimal performance on your system.

This program balances performance with maintainability, offering a robust solution for processing large-scale temperature data files.
