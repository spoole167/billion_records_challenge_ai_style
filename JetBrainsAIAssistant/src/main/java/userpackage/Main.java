// Java 23
package userpackage;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * Program to process a large text file, compute min, mean, and max temperature
 * per weather station, and print results alphabetically.
 *
 * Usage: java userpackage.Main input_file_name
 */
public class Main {

    /**
     * Entry point of the program.
     *
     * @param args Command-line arguments (input file name is the first argument).
     */
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: java userpackage.Main <input_file_name>");
            System.exit(1);
        }

        String fileName = args[0];
        Map<String, TemperatureStats> weatherStationData = new ConcurrentHashMap<>();
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(availableProcessors);

        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            reader.lines()
                  .filter(line -> !line.startsWith("#") && !line.isBlank()) // Filter out comments and empty lines
                  .map(Main::parseLine)
                  .filter(Objects::nonNull)
                  .forEach(record -> executor.submit(() -> processRecord(record, weatherStationData)));

            executor.shutdown();
            while (!executor.isTerminated()) {
                // Wait for all tasks to complete
            }

        } catch (IOException e) {
            System.err.println("Error reading the file: " + e.getMessage());
            System.exit(2);
        }

        // Sort the results alphabetically and display the output
        String result = weatherStationData.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining(", "));
        System.out.println("{" + result + "}");
    }

    /**
     * Parse a line from the input file into a StationRecord.
     *
     * @param line A single line of input.
     * @return A StationRecord object containing station name and temperature, or null if parsing fails.
     */
    private static StationRecord parseLine(String line) {
        try {
            String[] parts = line.split(";");
            String station = parts[0].trim();
            double temperature = Double.parseDouble(parts[1].trim());
            return new StationRecord(station, temperature);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Process a single record and update the weather station data map.
     *
     * @param record             The StationRecord to process.
     * @param weatherStationData The map of weather stations and their temperature statistics.
     */
    private static void processRecord(StationRecord record, Map<String, TemperatureStats> weatherStationData) {
        weatherStationData.compute(record.station,
                (key, stats) -> {
                    if (stats == null) {
                        stats = new TemperatureStats();
                    }
                    stats.add(record.temperature);
                    return stats;
                });
    }

    /**
     * Represents a record with a weather station and temperature.
     */
    private static class StationRecord {
        final String station;
        final double temperature;

        StationRecord(String station, double temperature) {
            this.station = station;
            this.temperature = temperature;
        }
    }

    /**
     * Represents statistical data for a weather station.
     */
    private static class TemperatureStats {
        private double min = Double.MAX_VALUE;
        private double max = Double.MIN_VALUE;
        private double sum = 0.0;
        private long count = 0;

        public synchronized void add(double temperature) {
            min = Math.min(min, temperature);
            max = Math.max(max, temperature);
            sum += temperature;
            count++;
        }

        @Override
        public synchronized String toString() {
            double mean = count > 0 ? sum / count : 0.0;
            return String.format("%.1f/%.1f/%.1f", min, mean, max);
        }
    }
}