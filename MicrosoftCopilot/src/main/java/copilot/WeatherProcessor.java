package copilot;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.DoubleAdder;

/**
 * Java version: 17
 * This program processes a text file to calculate the min, mean, and max temperature per weather station.
 * Input file name is the first command-line argument.
 */
public class WeatherProcessor {

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java WeatherProcessor <input-file>");
            System.exit(1);
        }
        
        String inputFileName = args[0];
        ConcurrentHashMap<String, TemperatureStats> weatherData = new ConcurrentHashMap<>();
        
        try (BufferedReader br = new BufferedReader(new FileReader(inputFileName))) {
            br.lines().parallel().forEach(line -> {
                if (!line.startsWith("#")) {
                    String[] parts = line.split(";");
                    String station = parts[0];
                    double temperature = Double.parseDouble(parts[1]);
                    
                    weatherData.computeIfAbsent(station, k -> new TemperatureStats()).updateStats(temperature);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        weatherData.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    String station = entry.getKey();
                    TemperatureStats stats = entry.getValue();
                    System.out.printf("%s=%.1f/%.1f/%.1f%n", station, stats.getMin(), stats.getMean(), stats.getMax());
                });
    }
}

class TemperatureStats {
    private double min = Double.MAX_VALUE;
    private double max = Double.MIN_VALUE;
    private final DoubleAdder sum = new DoubleAdder();
    private final AtomicInteger count = new AtomicInteger();

    public void updateStats(double temperature) {
        min = Math.min(min, temperature);
        max = Math.max(max, temperature);
        sum.add(temperature);
        count.incrementAndGet();
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    public double getMean() {
        return sum.sum() / count.get();
    }
}
