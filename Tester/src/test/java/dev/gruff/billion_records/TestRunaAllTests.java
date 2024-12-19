package dev.gruff.billion_records;


import org.junit.jupiter.api.Test;

import java.io.*;
import java.lang.classfile.ClassFile;
import java.lang.classfile.ClassModel;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This class is responsible for executing all sample tests for projects located in a specified directory.
 * It filters directories to find valid projects, identifies the main class, and runs tests using a predefined sample file.
 */
public class TestRunaAllTests {

    public static final String BRC_MEASUREMENTS_TXT = "/Users/spoole/Documents/GitHub/1brc/measurements.txt";
    public static final String SAMPLE = "src/test/resources/sample.txt";

    static class TestRun {
        String name;
        File main;
        long time;
    }
    @Test
    void runSample1Count() throws IOException {

        runTestGroup(SAMPLE,"sample", 1);

    }
    @Test
    void runSample1KCount() throws IOException {

        runTestGroup(SAMPLE,"sample", 1000);

    }
    @Test
    void runSample1MCount() throws IOException {

        runTestGroup(SAMPLE,"sample", 1000000);

    }


    private void runTestGroup(String testFile, String desc,int count) throws IOException {

        File dir=new File("target");
        dir=new File(dir,"results");
        dir=new File(dir,desc);

        File output=new File(dir,""+count);
        output.mkdirs();

        Path projRoot=Paths.get("..").toAbsolutePath().normalize();

        File[] dirs= projRoot.toFile().listFiles();

        FileWriter fw=new FileWriter(new File(output,"summary.csv"));
        PrintWriter pw=new PrintWriter(fw);
        pw.println("#Test Results x3");
        pw.println("# Data :"+testFile);
        pw.println("# Count :"+count);
        pw.println("project,count,elapsed,unit,completed");

        TreeMap<String,TestRun> modules=new TreeMap<>();

        for(int i=1;i<4; i++) {
            System.out.println("---");
            System.out.println("--- PASS "+i+"  of "+testFile+" * "+count);
            System.out.println("---");
            for (File proj : dirs) {
                String name = proj.getName();
                if (name.equals("Tester")) continue;
                if (name.startsWith(".")) continue;
                if (!proj.isDirectory()) continue;

                File pom = new File(proj, "pom.xml");
                if (!pom.exists()) continue;
                File main = getMain(proj);
                runTest(output, pw, testFile, name, main, count);


            }
        }
        pw.close();
    }

    /**
     * Executes a test run for a specified project and main file.
     * Constructs the classpath and main class name from the file path and calls the runProcess method.
     * Prints the execution time and result status.
     *
     * @param dir The directory where the test is located.
     * @param testFile The test file to be executed.
     * @param project The name of the project to be tested.
     * @param main The main file to be executed.
     * @param count The number of times the test should be run.
     */


    private void runTest(File dir,PrintWriter reporter,String testFile,String project,File main,int count) {

        String path=main.getAbsolutePath();
        int classesPos=path.indexOf("classes");
        String clp=path.substring(0,classesPos+8);
        String mainName=path.substring(classesPos+8,path.length()-6).replace("/",".");



        LocalClassInstantiator lc=null;
        File output=new File(dir,project+"_results.txt");



        try
        {


            long startTime = System.nanoTime();
            boolean r= runProcess(output,project,clp,mainName,testFile,count);
            long durationNanos   = System.nanoTime()-startTime;
            long millis = durationNanos / 1_000_000;
            double fmillis=(double)(durationNanos)/((double)count*1e6);
            double fmills_count=fmillis/(double)count;

            reporter.printf("%s,%d,%d,%f,%b\n",project,count,millis,fmillis,r);
            System.out.printf("    %s,%d,%d,%f,%b\n",project,count,millis,fmillis,r);

        } catch (Exception e) {
            e.printStackTrace();
        }




    }

    private boolean runProcess(File outputFile,String project,String cp, String main,String testfile,int count)  {
        File target=new File("target");
        File testclasses=new File(target,"test-classes");
        String classpath=testclasses.getAbsolutePath()+java.io.File.pathSeparator+cp;

        ProcessBuilder processBuilder = new ProcessBuilder();

        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            processBuilder.command("java.exe", "-cp",classpath,"dev.gruff.billion_records.TestRunner",cp,main,testfile,""+count);
        } else {
            processBuilder.command("java", "-cp", classpath,"dev.gruff.billion_records.TestRunner",cp,main,testfile,""+count);
        }

        // Setting the output file for redirection
            processBuilder.redirectOutput(outputFile);
            processBuilder.redirectError(outputFile);
        try {

            Process process = processBuilder.start();
            // Wait for the process to finish
        int exitCode = 0;

            exitCode = process.waitFor();
            return exitCode == 0;

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }



    }

    private File getMain(File proj) throws IOException {

        File target=new File(proj,"target");
        File classes=new File(target,"classes");

        Optional<Path> mainClass=Files.find(classes.toPath(), 10, (path, basicFileAttributes) -> isMainClass(path))
                .findFirst();

        return mainClass.map(Path::toFile).orElse(null);
    }

    private boolean isMainClass(Path path) {
        if (!path.toString().endsWith(".class")) return false;
        if (path.toString().contains("$")) return false;

        ClassFile cf = ClassFile.of();
        try {
            ClassModel m = cf.parse(path);
            AtomicBoolean foundMain = new AtomicBoolean(false);
            m.methods().forEach(method -> {
                if (method.methodName().stringValue().equals("main")) {
                    foundMain.set(true);
                }
            });
            return foundMain.get();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    }
