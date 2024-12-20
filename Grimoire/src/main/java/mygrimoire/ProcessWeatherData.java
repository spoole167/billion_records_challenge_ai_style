package mygrimoire;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.DoubleSummaryStatistics;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Java program to process a text file with 1,000,000,000 rows and compute
 * the min, mean, and max temperature per weather station.
 *
 * Requires Java 17 or higher.
 *
 * Usage: java mygrimoire.ProcessWeatherData <inputFilePath>
 */
public class ProcessWeatherData {

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: java mygrimoire.ProcessWeatherData <inputFilePath>");
            System.exit(1);
        }

        String inputFilePath = args[0];
        Path filePath = Paths.get(inputFilePath);

        if (!Files.exists(filePath) || !Files.isReadable(filePath)) {
            System.err.println("Error: File does not exist or is not readable.");
            System.exit(1);
        }

        try (Stream<String> lines = Files.lines(filePath)) {
            Map<String, DoubleSummaryStatistics> statistics = lines
                    .parallel()
                    .filter(line -> !line.startsWith("#")) // Ignore comments
                    .map(line -> line.split(";"))
                    .filter(parts -> parts.length == 2)
                    .collect(Collectors.groupingBy(
                            parts -> parts[0].trim(),
                            Collectors.summarizingDouble(parts -> Double.parseDouble(parts[1].trim()))
                    ));

            TreeMap<String, String> sortedResults = statistics.entrySet().stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            entry -> formatStatistics(entry.getValue()),
                            (a, b) -> a,
                            TreeMap::new
                    ));

            System.out.println(sortedResults);

        } catch (IOException e) {
            System.err.println("Error processing file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Formats the statistics into the required output format.
     *
     * @param stats DoubleSummaryStatistics object.
     * @return A formatted string in the format min/mean/max.
     */
    private static String formatStatistics(DoubleSummaryStatistics stats) {
        return String.format("%.1f/%.1f/%.1f", stats.getMin(), stats.getAverage(), stats.getMax());
    }
}
