package weatherproc;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Weather data processor optimized for processing billion-record files.
 * Requires Java 21 or higher.
 *
 * Run with: java --enable-preview -XX:+UseParallelGC -Xms4g -Xmx4g WeatherProcessor input.txt
 */
public class WeatherProcessor {
    private static class StationStats {
        private double min = Double.MAX_VALUE;
        private double max = Double.MIN_VALUE;
        private double sum = 0.0;
        private final AtomicInteger count = new AtomicInteger(0);

        synchronized void update(double temp) {
            min = Math.min(min, temp);
            max = Math.max(max, temp);
            sum += temp;
            count.incrementAndGet();
        }

        double getAverage() {
            return sum / count.get();
        }
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: WeatherProcessor <input-file>");
            System.exit(1);
        }

        ConcurrentHashMap<String, StationStats> stations = new ConcurrentHashMap<>();

        try {
            processFile(args[0], stations);
            printResults(stations);
        } catch (IOException e) {
            System.err.println("Error processing file: " + e.getMessage());
            System.exit(1);
        }
    }

    private static void processFile(String filename, ConcurrentHashMap<String, StationStats> stations)
            throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new FileReader(filename), 1024 * 1024)) { // 1MB buffer

            reader.lines()
                    .parallel()
                    .filter(line -> !line.startsWith("#"))
                    .forEach(line -> {
                        String[] parts = line.split(";");
                        if (parts.length == 2) {
                            String station = parts[0];
                            double temp = Double.parseDouble(parts[1]);

                            stations.computeIfAbsent(station, k -> new StationStats())
                                    .update(temp);
                        }
                    });
        }
    }

    private static void printResults(ConcurrentHashMap<String, StationStats> stations) {
        String result = stations.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> {
                    StationStats stats = entry.getValue();
                    return String.format("%s=%.1f/%.1f/%.1f",
                            entry.getKey(),
                            stats.min,
                            stats.getAverage(),
                            stats.max);
                })
                .collect(Collectors.joining(", ", "{", "}"));

        System.out.println(result);
    }
}
