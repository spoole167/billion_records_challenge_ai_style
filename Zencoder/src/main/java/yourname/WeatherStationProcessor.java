// Java version: 17
package yourname;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;

/**
 * A program to process a large text file containing weather station data and calculate
 * the minimum, mean, and maximum temperatures for each station.
 */
public class WeatherStationProcessor {

    /**
     * Main method to execute the program.
     *
     * @param args Command-line arguments, where the first argument is the input file name.
     */
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Please provide the input file name as the first argument.");
            System.exit(1);
        }

        String fileName = args[0];
        Map<String, TemperatureStats> stationStats = new ConcurrentHashMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            reader.lines().parallel().forEach(line -> {
                if (!line.startsWith("#")) {
                    String[] parts = line.split(";");
                    if (parts.length == 2) {
                        String station = parts[0];
                        double temperature = Double.parseDouble(parts[1]);

                        stationStats.computeIfAbsent(station, k -> new TemperatureStats())
                                .update(temperature);
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        String result = stationStats.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining(", "));

        System.out.println(result);
    }

    /**
     * A helper class to maintain temperature statistics for a weather station.
     */
    private static class TemperatureStats {
        private double min = Double.MAX_VALUE;
        private double max = Double.MIN_VALUE;
        private DoubleAdder sum = new DoubleAdder();
        private LongAdder count = new LongAdder();

        /**
         * Update the statistics with a new temperature reading.
         *
         * @param temperature The new temperature reading.
         */
        public synchronized void update(double temperature) {
            min = Math.min(min, temperature);
            max = Math.max(max, temperature);
            sum.add(temperature);
            count.increment();
        }

        @Override
        public String toString() {
            double mean = sum.doubleValue() / count.doubleValue();
            return String.format("%.1f/%.1f/%.1f", min, mean, max);
        }
    }
}

