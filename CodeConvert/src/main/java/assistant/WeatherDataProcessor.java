/**
 * Java program to process a large text file containing weather data.
 * Version: 17
 */
package assistant;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WeatherDataProcessor {

    private static final int THREAD_COUNT = Runtime.getRuntime().availableProcessors();

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java WeatherDataProcessor <input_file>");
            System.exit(1);
        }

        String inputFile = args[0];
        Map<String, double[]> stationData = new ConcurrentHashMap<>();

        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        try (BufferedReader br = new BufferedReader(new FileReader(inputFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("#")) continue;
                executor.submit(() -> processLine(line, stationData));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            executor.shutdown();
        }

        while (!executor.isTerminated()) {
            // Wait for all tasks to finish
        }

        printResults(stationData);
    }

    private static void processLine(String line, Map<String, double[]> stationData) {
        String[] parts = line.split(";");
        if (parts.length != 2) return;

        String station = parts[0];
        double temperature;
        try {
            temperature = Double.parseDouble(parts[1]);
        } catch (NumberFormatException e) {
            return;
        }

        stationData.merge(station, new double[]{temperature, temperature, temperature, 1},
                (existing, newValues) -> {
                    existing[0] = Math.min(existing[0], newValues[0]); // min
                    existing[1] += newValues[1]; // sum
                    existing[2] = Math.max(existing[2], newValues[2]); // max
                    existing[3] += newValues[3]; // count
                    return existing;
                });
    }

    private static void printResults(Map<String, double[]> stationData) {
        StringBuilder result = new StringBuilder("{");
        stationData.forEach((station, data) -> {
            double min = data[0];
            double mean = data[1] / data[3];
            double max = data[2];
            result.append(station).append("=").append(String.format("%.1f/%.1f/%.1f", min, mean, max)).append(", ");
        });
        if (result.length() > 1) {
            result.setLength(result.length() - 2); // Remove last comma and space
        }
        result.append("}");
        System.out.println(result);
    }
}
