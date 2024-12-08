// Java version: 17
package alex;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Processes a large text file containing weather station data to calculate minimum, mean, and maximum temperatures.
 */
public class WeatherProcessor {

    /**
     * Main method to process the weather data file.
     *
     * @param args The command-line arguments.  The first argument should be the path to the input file.
     * @throws IOException If an I/O error occurs.
     */
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Usage: java WeatherProcessor <input_file>");
            System.exit(1);
        }

        String filePath = args[0];
        Map<String, StationData> stationData = processWeatherData(filePath);
        System.out.println(formatResults(stationData));

    }


    /**
     * Processes the weather data file and calculates statistics for each weather station.
     *
     * @param filePath The path to the input file.
     * @return A map of station names to their corresponding StationData objects.
     * @throws IOException If an I/O error occurs.
     */
    private static Map<String, StationData> processWeatherData(String filePath) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            return reader.lines()
                    .parallel() // Use parallel streams for better performance
                    .filter(line -> !line.startsWith("#"))
                    .map(line -> line.split(";"))
                    .filter(parts -> parts.length == 2)
                    .collect(Collectors.groupingByConcurrent(parts -> parts[0],
                            Collectors.reducing(new StationData(),
                                    parts -> new StationData(Double.parseDouble(parts[1])),
                                    (s1, s2) -> s1.combine(s2))));

        }
    }


    /**
     * Formats the results into a string.
     * @param stationData  The map of station names to their statistics.
     * @return A formatted string of the results.
     */
    private static String formatResults(Map<String, StationData> stationData) {
        return new TreeMap<>(stationData).entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining(", "));
    }


    /**
     * Helper class to store the minimum, mean, and maximum temperatures for a weather station.
     */
    private static class StationData {
        private double minTemp;
        private double sumTemp;
        private double maxTemp;
        private AtomicInteger count = new AtomicInteger(0);

        public StationData() {
            minTemp = Double.MAX_VALUE;
            maxTemp = Double.MIN_VALUE;
            sumTemp = 0;
        }

        public StationData(double temp) {
            this();
            update(temp);
        }

        public void update(double temp) {
            minTemp = Math.min(minTemp, temp);
            maxTemp = Math.max(maxTemp, temp);
            sumTemp += temp;
            count.incrementAndGet();
        }


        public StationData combine(StationData other) {
            this.minTemp = Math.min(this.minTemp, other.minTemp);
            this.maxTemp = Math.max(this.maxTemp, other.maxTemp);
            this.sumTemp += other.sumTemp;
            this.count.addAndGet(other.count.get());
            return this;
        }

        @Override
        public String toString() {
            return String.format("%.1f/%.1f/%.1f", minTemp, sumTemp / count.get(), maxTemp);
        }
    }
}
