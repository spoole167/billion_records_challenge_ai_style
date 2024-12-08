/**
 * Java Version: 17
 */

package assistantpkg;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.TreeMap;

/**
 * The WeatherStationProcessor program processes a large text file containing weather data.
 * It calculates the minimum, mean, and maximum temperatures for each weather station
 * and prints the results in alphabetical order.
 *
 * <p>Usage:
 * <pre>
 *     java assistantpkg.WeatherStationProcessor inputFileName.txt
 * </pre>
 *
 * <p>File Format:
 * Each row in the input file should have the format:
 * <pre>
 *     StationName;Temperature
 * </pre>
 * Lines starting with '#' are considered comments and are ignored.
 *
 * <p>Example Output:
 * <pre>
 *     {Abha=5.0/18.0/27.4, Abidjan=15.7/26.0/34.1, ...}
 * </pre>
 *
 * <p>Performance Optimizations:
 * This program is optimized for speed by:
 * <ul>
 *     <li>Using efficient I/O with BufferedReader.</li>
 *     <li>Utilizing a HashMap for constant-time access to station data.</li>
 *     <li>Minimizing object creation within the processing loop.</li>
 *     <li>Employing a TreeMap at the end to sort the stations alphabetically.</li>
 *     <li>Leveraging modern Java features available in Java 17.</li>
 * </ul>
 *
 * <p>Note:
 * Given the large size of the input file (1,000,000,000 rows), ensure that the system has sufficient memory.
 *
 * @author
 */
public class WeatherStationProcessor {

    /**
     * Represents temperature statistics for a weather station.
     */
    private static class TemperatureStats {
        double min;
        double sum;
        long count;
        double max;

        TemperatureStats(double temperature) {
            this.min = temperature;
            this.max = temperature;
            this.sum = temperature;
            this.count = 1;
        }

        /**
         * Updates the statistics with a new temperature reading.
         *
         * @param temperature The new temperature to incorporate.
         */
        void update(double temperature) {
            if (temperature < min) {
                min = temperature;
            }
            if (temperature > max) {
                max = temperature;
            }
            sum += temperature;
            count++;
        }

        /**
         * Calculates the mean temperature.
         *
         * @return The mean temperature.
         */
        double getMean() {
            return sum / count;
        }

        @Override
        public String toString() {
            return String.format("%.1f/%.1f/%.1f", min, getMean(), max);
        }
    }

    /**
     * The main method processes the input file and computes temperature statistics per station.
     *
     * @param args Command-line arguments where args[0] is the input file name.
     */
    public static void main(String[] args) {
        // Validate command-line arguments
        if (args.length < 1) {
            System.err.println("Usage: java assistantpkg.WeatherStationProcessor <inputFileName>");
            System.exit(1);
        }

        String inputFileName = args[0];
        Map<String, TemperatureStats> stationData = new HashMap<>();

        // Read and process the input file
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFileName), 16 * 1024)) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Ignore comment lines
                if (line.startsWith("#")) {
                    continue;
                }

                // Split the line into station and temperature
                String[] parts = line.split(";");
                if (parts.length != 2) {
                    // Invalid line format; skip or handle as needed
                    continue;
                }

                String station = parts[0];
                double temperature;
                try {
                    temperature = Double.parseDouble(parts[1]);
                } catch (NumberFormatException e) {
                    // Invalid temperature format; skip or handle as needed
                    continue;
                }

                // Update the statistics for the station
                TemperatureStats stats = stationData.get(station);
                if (stats == null) {
                    stationData.put(station, new TemperatureStats(temperature));
                } else {
                    stats.update(temperature);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading the input file: " + e.getMessage());
            System.exit(1);
        }

        // Sort the stations alphabetically using TreeMap
        Map<String, TemperatureStats> sortedStationData = new TreeMap<>(stationData);

        // Build the output string
        StringBuilder output = new StringBuilder();
        output.append("{");
        boolean first = true;
        for (Map.Entry<String, TemperatureStats> entry : sortedStationData.entrySet()) {
            if (!first) {
                output.append(", ");
            }
            output.append(entry.getKey()).append("=").append(entry.getValue().toString());
            first = false;
        }
        output.append("}");

        // Print the result
        System.out.println(output.toString());
    }
}

