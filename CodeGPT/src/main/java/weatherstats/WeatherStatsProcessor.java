package weatherstats;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.*;

/**
 * Java version: 17+
 *
 * This program processes a large text file containing weather station temperature data,
 * calculates the minimum, mean, and maximum temperature for each station, and prints the
 * results alphabetically by station name.
 *
 * Input format: Each line contains `Station;Temperature` (e.g., `Hamburg;12.0`).
 * Lines starting with # are comments and are ignored.
 *
 * Usage: java weatherstats.WeatherStatsProcessor <input_file>
 */
public class WeatherStatsProcessor {

    public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
        if (args.length != 1) {
            System.err.println("Usage: java weatherstats.WeatherStatsProcessor <input_file>");
            System.exit(1);
        }

        String fileName = args[0];

        // Process file using parallel streams
        Map<String, TemperatureStats> stationStats =
                Files.lines(Path.of(fileName))
                        .parallel()
                        .filter(line -> !line.startsWith("#"))
                        .map(WeatherStatsProcessor::parseLine)
                        .filter(Objects::nonNull)
                        .collect(Collectors.groupingBy(
                                StationTemperature::getStation,
                                ConcurrentHashMap::new,
                                Collector.of(
                                        TemperatureStats::new,
                                        TemperatureStats::accept,
                                        TemperatureStats::combine)));

        // Prepare and sort results
        String result = stationStats.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> String.format("%s=%.1f/%.1f/%.1f",
                        entry.getKey(),
                        entry.getValue().getMin(),
                        entry.getValue().getMean(),
                        entry.getValue().getMax()))
                .collect(Collectors.joining(", "));

        // Print results
        System.out.println(result);
    }

    /**
     * Parse a line of input into a StationTemperature object.
     *
     * @param line the input line
     * @return a StationTemperature object, or null if the line is invalid
     */
    private static StationTemperature parseLine(String line) {
        try {
            String[] parts = line.split(";");
            if (parts.length != 2) return null;
            String station = parts[0].trim();
            double temperature = Double.parseDouble(parts[1].trim());
            return new StationTemperature(station, temperature);
        } catch (Exception e) {
            return null; // Skip invalid lines
        }
    }

    /**
     * A simple record to hold station and temperature data.
     */
    private static class StationTemperature {
        private final String station;
        private final double temperature;

        public StationTemperature(String station, double temperature) {
            this.station = station;
            this.temperature = temperature;
        }

        public String getStation() {
            return station;
        }

        public double getTemperature() {
            return temperature;
        }
    }

    /**
     * A helper class to calculate and store temperature statistics.
     */
    private static class TemperatureStats {
        private double sum = 0;
        private double min = Double.MAX_VALUE;
        private double max = Double.MIN_VALUE;
        private long count = 0;

        public void accept(StationTemperature record) {
            double temp = record.getTemperature();
            sum += temp;
            min = Math.min(min, temp);
            max = Math.max(max, temp);
            count++;
        }

        public TemperatureStats combine(TemperatureStats other) {
            sum += other.sum;
            min = Math.min(min, other.min);
            max = Math.max(max, other.max);
            count += other.count;
            return this;
        }

        public double getMin() {
            return min;
        }

        public double getMean() {
            return sum / count;
        }

        public double getMax() {
            return max;
        }
    }
}
