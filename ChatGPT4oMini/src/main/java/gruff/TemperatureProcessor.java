/**
 * Program to calculate min, mean, and max temperature per weather station from a large text file.
 * Java Version: 17 (or higher)
 * 
 * This program processes a text file containing weather data in the format:
 * Station;Temperature
 * It calculates the minimum, mean, and maximum temperature for each station and prints
 * the results alphabetically in the format:
 * {Abha=5.0/18.0/27.4, Abidjan=15.7/26.0/34.1, ...}
 * 
 * File Format:
 * Each line contains Station;Temperature
 * Lines starting with # are considered comments and should be ignored.
 * 
 * @author <Your Name>
 * @version 1.0
 */

package gruff;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.*;
import java.util.function.*;
import java.util.regex.*;
import java.nio.file.*;
import java.nio.charset.*;
import jdk.incubator.vector.*;

/**
 * Main class for processing temperature data
 */
public class TemperatureProcessor {
    
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java gruff.TemperatureProcessor <input-file>");
            System.exit(1);
        }
        
        String fileName = args[0];
        
        // Create a thread pool for parallel processing
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        
        // Result map to store temperature data per station
        ConcurrentMap<String, TemperatureStats> stationStats = new ConcurrentHashMap<>();
        
        // Regular expression for valid temperature lines
        Pattern pattern = Pattern.compile("^(?!#)([A-Za-z ]+);([-+]?[0-9]*\\.?[0-9]+)$");

        // Process the file line by line using parallel streams
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(fileName), StandardCharsets.UTF_8)) {
            reader.lines()
                .parallel()
                .filter(line -> !line.isEmpty() && !line.startsWith("#"))
                .map(pattern::matcher)
                .filter(Matcher::matches)
                .forEach(matcher -> {
                    String station = matcher.group(1).trim();
                    double temperature = Double.parseDouble(matcher.group(2));
                    stationStats.compute(station, (key, stats) -> {
                        if (stats == null) {
                            stats = new TemperatureStats();
                        }
                        stats.addTemperature(temperature);
                        return stats;
                    });
                });
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        // Print the results in the desired format
        stationStats.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .forEach(entry -> {
                String station = entry.getKey();
                TemperatureStats stats = entry.getValue();
                System.out.printf("%s=%.1f/%.1f/%.1f, ", station, stats.min(), stats.mean(), stats.max());
            });
        
        // Shutdown the executor service
        executor.shutdown();
    }
    
    /**
     * Class to store temperature statistics (min, mean, max) for each station
     */
    static class TemperatureStats {
        private double sum = 0.0;
        private double min = Double.MAX_VALUE;
        private double max = Double.MIN_VALUE;
        private long count = 0;

        /**
         * Adds a new temperature reading and updates stats
         * 
         * @param temperature the temperature to add
         */
        public void addTemperature(double temperature) {
            sum += temperature;
            if (temperature < min) {
                min = temperature;
            }
            if (temperature > max) {
                max = temperature;
            }
            count++;
        }

        /**
         * Gets the mean temperature
         * 
         * @return the mean temperature
         */
        public double mean() {
            return count == 0 ? 0.0 : sum / count;
        }

        /**
         * Gets the min temperature
         * 
         * @return the min temperature
         */
        public double min() {
            return min;
        }

        /**
         * Gets the max temperature
         * 
         * @return the max temperature
         */
        public double max() {
            return max;
        }
    }
}
