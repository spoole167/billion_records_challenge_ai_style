package useroptimized;

/**
 * Java program to calculate min, mean, and max temperature per weather station
 * from a large text file. Optimized for speed using modern Java features.
 * 
 * Java version: 20+
 * 
 * Usage: java useroptimized.WeatherStationProcessor <input_file>
 */

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Stream;

public class WeatherStationProcessor {

    private static final int THREAD_COUNT = Runtime.getRuntime().availableProcessors();

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java useroptimized.WeatherStationProcessor <input_file>");
            System.exit(1);
        }

        String inputFile = args[0];
        Map<String, StationStatistics> stationData = new ConcurrentHashMap<>();

        try (BufferedReader reader = Files.newBufferedReader(Path.of(inputFile));
             Stream<String> lines = reader.lines()) {

            ForkJoinPool customThreadPool = new ForkJoinPool(THREAD_COUNT);
            customThreadPool.submit(() -> lines.parallel().forEach(line -> processLine(line, stationData))).join();

        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            System.exit(2);
        }

        stationData.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    String station = entry.getKey();
                    StationStatistics stats = entry.getValue();
                    double mean = stats.sum.doubleValue() / stats.count.doubleValue();
                    System.out.printf("%s=%.1f/%.1f/%.1f%n", station, stats.min, mean, stats.max);
                });
    }

    /**
     * Processes a single line of the input file.
     * 
     * @param line       The line to process.
     * @param stationData The shared data structure for storing results.
     */
    private static void processLine(String line, Map<String, StationStatistics> stationData) {
        if (line.startsWith("#") || line.isBlank()) {
            return; // Ignore comments and blank lines
        }

        String[] parts = line.split(";");
        if (parts.length != 2) {
            return; // Skip malformed lines
        }

        String station = parts[0].trim();
        double temperature;
        try {
            temperature = Double.parseDouble(parts[1].trim());
        } catch (NumberFormatException e) {
            return; // Skip lines with invalid temperature data
        }

        stationData.compute(station, (key, stats) -> {
            if (stats == null) {
                stats = new StationStatistics();
            }
            stats.update(temperature);
            return stats;
        });
    }

    /**
     * A thread-safe class to hold statistics for a weather station.
     */
    private static class StationStatistics {
        private final DoubleAdder sum = new DoubleAdder();
        private final LongAdder count = new LongAdder();
        private volatile double min = Double.MAX_VALUE;
        private volatile double max = Double.MIN_VALUE;

        public synchronized void update(double temperature) {
            sum.add(temperature);
            count.increment();
            min = Math.min(min, temperature);
            max = Math.max(max, temperature);
        }
    }
}
