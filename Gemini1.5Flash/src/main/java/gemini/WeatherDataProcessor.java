package gemini;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Processes a large text file containing weather station data and calculates
 * min, mean, and max temperatures per station.
 * 
 * @author gemini
 */
public class WeatherDataProcessor {

    /**
     * Main entry point.
     * 
     * @param args The first argument should be the input file path.
     * @throws IOException If an I/O error occurs.
     */
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Usage: java gemini.WeatherDataProcessor <input_file>");
            System.exit(1);
        }

        String inputFile = args[0];

        // Use a ConcurrentHashMap to handle concurrent writes from multiple threads
        Map<String, StationData> stationData = new ConcurrentHashMap<>();

        // Create a thread pool to process the file in parallel
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.startsWith("#")) {
                    String[] parts = line.split(";");
                    String station = parts[0];
                    double temperature = Double.parseDouble(parts[1]);

                    // Use a lambda expression to submit tasks to the executor
                    executor.submit(() -> {
                        stationData.computeIfAbsent(station, s -> new StationData()).update(temperature);
                    });
                }
            }
        }

        // Shutdown the executor and wait for all tasks to finish
        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Sort the station data alphabetically and print the results
        List<Map.Entry<String, StationData>> sortedData = new ArrayList<>(stationData.entrySet());
        Collections.sort(sortedData, (a, b) -> a.getKey().compareTo(b.getKey()));

        System.out.println(
                sortedData.stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining(", ", "{", "}"))

        );

    }

    /**
     * Represents the temperature data for a weather station.
     */
    private static class StationData {
        private double minTemp = Double.MAX_VALUE;
        private double maxTemp = Double.MIN_VALUE;
        private double sumTemp = 0;
        private long count = 0;

        public void update(double temperature) {
            minTemp = Math.min(minTemp, temperature);
            maxTemp = Math.max(maxTemp, temperature);
            sumTemp += temperature;
            count++;
        }

        @Override
        public String toString() {
            double meanTemp = count > 0 ? sumTemp / count : 0;
            return String.format("%.1f/%.1f/%.1f", minTemp, meanTemp, maxTemp);
        }
    }
}
