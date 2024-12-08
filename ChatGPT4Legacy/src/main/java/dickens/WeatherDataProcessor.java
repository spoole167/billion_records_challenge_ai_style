package dickens;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * This program processes a text file containing temperature data by weather stations.
 * It calculates the minimum, mean, and maximum temperature for each station and prints
 * the results in alphabetical order.
 * 
 * To run: java WeatherDataProcessor "path_to_file.txt"
 * 
 * @author Your Name
 * @version Java 19
 */
public class WeatherDataProcessor {

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java WeatherDataProcessor <file_path>");
            return;
        }

        String filePath = args[0];
        Map<String, Stats> stationStats = new ConcurrentHashMap<>();
        
        // Define the number of threads based on available processors
        int threads = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(threads);

        try (BufferedReader reader = Files.newBufferedReader(Paths.get(filePath))) {
            List<Future<?>> futures = new ArrayList<>();

            String line;
            while ((line = reader.readLine()) != null) {
                String finalLine = line;
                futures.add(executor.submit(() -> processLine(finalLine, stationStats)));
            }

            // Wait for all tasks to complete
            for (Future<?> future : futures) {
                future.get();
            }
            
            // Shut down the executor
            executor.shutdown();

            // Sort and format the output
            String result = stationStats.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .map(entry -> String.format("%s=%.1f/%.1f/%.1f", entry.getKey(), entry.getValue().min,
                            entry.getValue().mean(), entry.getValue().max))
                    .collect(Collectors.joining(", ", "{", "}"));
            
            System.out.println(result);

        } catch (IOException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    /**
     * Processes a line of text, updating the stats map if the line contains valid data.
     * 
     * @param line         The line to process.
     * @param stationStats The concurrent map of weather station statistics.
     */
    private static void processLine(String line, Map<String, Stats> stationStats) {
        if (!line.startsWith("#") && line.contains(";")) {
            String[] parts = line.split(";");
            if (parts.length == 2) {
                String station = parts[0].trim();
                try {
                    double temp = Double.parseDouble(parts[1].trim());
                    stationStats.compute(station, (key, stats) -> {
                        if (stats == null) {
                            stats = new Stats();
                        }
                        stats.add(temp);
                        return stats;
                    });
                } catch (NumberFormatException ignored) {
                }
            }
        }
    }

    /**
     * Helper class to keep track of statistics for a weather station.
     */
    static class Stats {
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        double sum = 0;
        int count = 0;

        /**
         * Adds a new temperature reading to the statistics.
         * 
         * @param temp The temperature to add.
         */
        void add(double temp) {
            if (temp < min) {
                min = temp;
            }
            if (temp > max) {
                max = temp;
            }
            sum += temp;
            count++;
        }

        /**
         * Calculates the mean temperature.
         * 
         * @return The mean temperature.
         */
        double mean() {
            return sum / count;
        }
    }
}
