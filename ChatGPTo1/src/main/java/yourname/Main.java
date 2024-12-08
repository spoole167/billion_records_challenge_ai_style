/*
 * Java version: 17
 * 
 * This program reads a large weather data file (1,000,000,000 rows or more),
 * processes each record in the format: "Station;Temperature", computes the 
 * minimum, mean, and maximum temperature for each weather station, and 
 * prints the results alphabetically in a single line.
 * 
 * Comment lines starting with '#' are ignored.
 * 
 * Command-line usage:
 *   java -Xmx8g -XX:+UseG1GC -XX:+UnlockExperimentalVMOptions -XX:+UseVectorApi -XX:AOTLibrary=./aot
 *   -cp . yourname Main input.txt
 * 
 * The program attempts to optimize for performance:
 * - Uses modern Java constructs
 * - Encourages GC-friendly data structures
 * - Can be tuned for large heaps and modern GC
 * - Avoids external dependencies
 * 
 * Note:
 * - For extreme performance, one might consider using memory-mapped files,
 *   advanced parallelization strategies, custom thread pools, or vectorized
 *   parsing (if feasible). These are beyond the scope of this code snippet.
 * - The provided code is a reference solution and should be tested and profiled 
 *   before use on extremely large files.
 */

package yourname;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.concurrent.atomic.LongAdder;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

/**
 * A record structure to hold aggregated statistics for each station.
 * This record stores min, max, sum, and count of temperatures encountered.
 */
final class StationStats {
    private double min;
    private double max;
    private final DoubleAdder sum;
    private final LongAdder count;

    StationStats(double initialValue) {
        this.min = initialValue;
        this.max = initialValue;
        this.sum = new DoubleAdder();
        this.count = new LongAdder();
        this.sum.add(initialValue);
        this.count.increment();
    }

    void add(double value) {
        // Update min and max using local references for speed
        double currentMin = this.min;
        double currentMax = this.max;
        if (value < currentMin) {
            this.min = value;
        }
        if (value > currentMax) {
            this.max = value;
        }
        this.sum.add(value);
        this.count.increment();
    }

    double getMin() {
        return this.min;
    }

    double getMax() {
        return this.max;
    }

    double getMean() {
        return this.sum.sum() / this.count.sum();
    }
}

/**
 * Main application class.
 */
public class Main {
    /**
     * Main entry point of the application.
     * 
     * @param args command-line arguments; args[0] should be the input filename.
     */
    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("Usage: java yourname.Main <inputfile>");
            System.exit(1);
        }

        String inputFile = args[0];

        // Use a concurrent map for thread-safe updates.
        // The key is the station name (String), 
        // and the value is a StationStats object.
        Map<String, StationStats> stationData = new ConcurrentHashMap<>(1_000_000);

        // If we decide to parallelize line processing, we could:
        // 1. Use a parallel stream approach (with caution due to overhead)
        // 2. Divide the input file processing into chunks
        // 3. Keep it single-threaded but rely on concurrent map for future scalability
        // For simplicity here, we'll process lines sequentially.
        
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(inputFile), StandardCharsets.UTF_8), 8192)) {
            
            String line;
            while ((line = reader.readLine()) != null) {
                // Skip comment lines
                if (line.isEmpty() || line.charAt(0) == '#') {
                    continue;
                }

                int sepIdx = line.indexOf(';');
                if (sepIdx < 0) {
                    // If line format is incorrect, skip
                    continue;
                }

                String station = line.substring(0, sepIdx);
                String tempStr = line.substring(sepIdx + 1);
                
                // Parse temperature
                double temperature;
                try {
                    temperature = Double.parseDouble(tempStr);
                } catch (NumberFormatException e) {
                    // If not a valid number, skip
                    continue;
                }

                // Update the statistics in a thread-safe manner
                stationData.compute(station, (k, v) -> {
                    if (v == null) {
                        return new StationStats(temperature);
                    } else {
                        v.add(temperature);
                        return v;
                    }
                });
            }
        }

        // Sort the station names alphabetically
        List<String> stations = new ArrayList<>(stationData.keySet());
        Collections.sort(stations);

        // Build the output line
        StringBuilder sb = new StringBuilder();
        sb.append('{');
        boolean first = true;
        for (String st : stations) {
            if (!first) {
                sb.append(", ");
            }
            StationStats stats = stationData.get(st);
            sb.append(st).append('=')
              .append(stats.getMin()).append('/')
              .append(stats.getMean()).append('/')
              .append(stats.getMax());
            first = false;
        }
        sb.append('}');

        // Print the results
        System.out.println(sb.toString());
    }
}
