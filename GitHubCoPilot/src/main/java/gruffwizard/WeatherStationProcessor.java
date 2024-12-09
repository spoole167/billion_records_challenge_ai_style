// Java version: 17

package gruffwizard;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;

/**
 * This program processes a large text file containing weather station data,
 * calculates the minimum, mean, and maximum temperature per station, and
 * prints the results in alphabetical order.
 */
public class WeatherStationProcessor {

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java WeatherStationProcessor <input-file>");
            System.exit(1);
        }

        String inputFile = args[0];
        Map<String, StationStats> stationStatsMap = new ConcurrentHashMap<>();

        // Read and process the file using multiple threads
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile))) {
            reader.lines().parallel().forEach(line -> {
                if (!line.startsWith("#")) {
                    String[] parts = line.split(";");
                    if (parts.length == 2) {
                        String station = parts[0];
                        double temperature = Double.parseDouble(parts[1]);
                        stationStatsMap.computeIfAbsent(station, k -> new StationStats()).addTemperature(temperature);
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Collect and print the results
        String result = stationStatsMap.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> String.format("%s=%.1f/%.1f/%.1f", entry.getKey(),
                        entry.getValue().getMin(), entry.getValue().getMean(), entry.getValue().getMax()))
                .collect(Collectors.joining(", "));

        System.out.println(result);
    }

    /**
     * A class to hold statistics for a weather station.
     */
    static class StationStats {
        private final DoubleAdder sum = new DoubleAdder();
        private final LongAdder count = new LongAdder();
        private double min = Double.MAX_VALUE;
        private double max = Double.MIN_VALUE;

        /**
         * Adds a temperature reading to the statistics.
         *
         * @param temperature the temperature reading
         */
        public synchronized void addTemperature(double temperature) {
            sum.add(temperature);
            count.increment();
            min = Math.min(min, temperature);
            max = Math.max(max, temperature);
        }

        /**
         * Returns the minimum temperature.
         *
         * @return the minimum temperature
         */
        public double getMin() {
            return min;
        }

        /**
         * Returns the mean temperature.
         *
         * @return the mean temperature
         */
        public double getMean() {
            return sum.sum() / count.doubleValue();
        }

        /**
         * Returns the maximum temperature.
         *
         * @return the maximum temperature
         */
        public double getMax() {
            return max;
        }
    }
}