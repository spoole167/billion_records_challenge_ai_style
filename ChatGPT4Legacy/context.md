Below is a Java program designed to efficiently process a very large text file containing temperature data, with each row formatted as {Station;Temperature}. The program calculates the minimum, mean, and maximum temperature for each weather station and prints the results sorted alphabetically by the station name. It uses modern Java features to optimize for speed, including multi-threading and concurrent collections.

Key Points:
Concurrency: The program uses a ConcurrentHashMap to store temperature statistics and an ExecutorService to process lines in parallel, enhancing performance on large files.
Error Handling: It gracefully handles formatting issues and non-numeric temperature entries without crashing.
Output: Results are sorted alphabetically by station name and formatted as specified.
Java Features: This example leverages modern Java concurrency features but remains simple to maintain clarity. It's compatible with recent Java versions, tested with Java 19.
Make sure to adjust the Java version in the @version tag if you use a different Java SDK. This program assumes a modern environment optimized for concurrency and large file handling.
