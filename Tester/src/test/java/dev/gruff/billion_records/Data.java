package dev.gruff.billion_records;

import java.io.File;

public class Data {

    private static final String BRC_MEASUREMENTS_TXT = "../../1brc/measurements.txt";
    private static final String SAMPLE = "src/test/resources/sample.txt";


    public static File sample() {

        String smallData = System.getProperty("sample", SAMPLE);
        File sample = new File(smallData);
        if(!sample.exists()) throw new RuntimeException("sample file not found");
        return sample;

    }


    public static File BRC() {

        String brc = System.getProperty("1brc", BRC_MEASUREMENTS_TXT);
        File sample = new File(brc);
        if(!sample.exists()) throw new RuntimeException("1BRC data file not found");
        return sample;

    }

}
