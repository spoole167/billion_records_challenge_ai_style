package dev.gruff.billion_records;


import org.junit.jupiter.api.Test;

import java.io.*;
import java.lang.classfile.ClassFile;
import java.lang.classfile.ClassModel;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import java.util.concurrent.atomic.AtomicBoolean;

import static dev.gruff.billion_records.Data.BRC_MEASUREMENTS_TXT;
import static dev.gruff.billion_records.Data.SAMPLE;

/**
 * This class is responsible for executing all sample tests for projects located in a specified directory.
 * It filters directories to find valid projects, identifies the main class, and runs tests using a predefined sample file.
 */
public class RunaAllTest {

    private static record TestData(String project, int count, long millis, double fmillis, TestResult result) { }

    public static Map<String,File> candidates=buildCandidates();
    public static Map<String,File> buildCandidates() {

        Map<String,File> candidates=new TreeMap<>();

        File rootDir=new File("../");
        File[] files=rootDir.listFiles();
        for(File f:files){
            if(!f.isDirectory())continue;
            if(f.getName().startsWith("."))continue;
            if(f.getName().equals("Tester"))continue;
            File pom=new File(f,"pom.xml");
            if(pom.exists()){
                try {
                    File main = getMain(f);
                    candidates.put(f.getName(),main);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            }
        }
        return candidates;



    }

    @Test
    void ArunSample1Count() throws IOException {

        runTestGroup(SAMPLE,"sample", 1);

    }
    @Test
    void BrunSample1KCount() throws IOException {

        runTestGroup(SAMPLE,"sample", 1000);

    }

    @Test
    void CrunSample100KCount() throws IOException {

        runTestGroup(SAMPLE,"sample", 100000);

    }

    @Test
    void DrunBRC1Count() throws IOException {

        runTestGroup(BRC_MEASUREMENTS_TXT,"brc", 1);

    }
    private void runTestGroup(String testFile, String desc,int count) throws IOException {

        File dir=new File("target");
        dir=new File(dir,"results");
        dir=new File(dir,desc);

        File output=new File(dir,""+count);
        output.mkdirs();

        FileWriter fw=new FileWriter(new File(output,"timings.csv"));
        PrintWriter pw=new PrintWriter(fw);
        pw.println("#Test Results x3");
        pw.println("# Data :"+testFile);
        pw.println("# Count :"+count);
        pw.println("project,count,elapsed,unit,completed");

        Map<String, List<TestData>> results=new TreeMap<>();
        results.clear();
        for(int i=1;i<4; i++) {
            System.out.println("---");
            System.out.println("--- PASS "+i+"  of "+testFile+" * "+count);
            System.out.println("---");

            for (String name :candidates.keySet()) {
                File main =candidates.get(name);
                TestData td=runTest(output, pw, testFile, name, main, count);
                if(td!=null && td.result()==TestResult.PASS) {
                    List<TestData> l=results.computeIfAbsent(name, k->new ArrayList<>());
                    l.add(td);
                }
            }
        }
        pw.close();

       fw=new FileWriter(new File(output,"summary.csv"));
       pw=new PrintWriter(fw);
        pw.println("#Test Results");
        pw.println("# Data :"+testFile);
        pw.println("# Count :"+count);
        pw.println("project,count,average");

        PrintWriter finalPw = pw;
        results.keySet().forEach(k->{
            List<TestData> l=results.get(k);
            double avg=l.stream().mapToDouble(TestData::fmillis).sum()/l.size();
            finalPw.printf("%s,%d,%f\n",k,count,avg);
        });

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


    private TestData runTest(File dir, PrintWriter reporter, String testFile, String project, File main, int count) {

        String path=main.getAbsolutePath();
        int classesPos=path.indexOf("classes");
        String clp=path.substring(0,classesPos+8);
        String mainName=path.substring(classesPos+8,path.length()-6).replace("/",".");



        LocalClassInstantiator lc=null;
        File output=new File(dir,project+"_results.txt");

        try
        {


            long startTime = System.nanoTime();
           TestResult r= runProcess(output,project,clp,mainName,testFile,count);
            long durationNanos   = System.nanoTime()-startTime;
            long millis = durationNanos / 1_000_000;
            double fmillis=(double)(durationNanos)/((double)count*1e6);
            double fmills_count=fmillis/(double)count;

            reporter.printf("%s,%d,%d,%f,%s\n",project,count,millis,fmillis,r.name());
            System.out.printf("    %s,%d,%d,%f,%s\n",project,count,millis,fmillis,r.name());
            return new TestData(project,count,millis,fmillis,r);
        } catch (Exception e) {
            e.printStackTrace();
        }


        return null;

    }

    private TestResult runProcess(File outputFile, String project, String cp, String main, String testfile, int count)  {
        File target=new File("target");
        File testclasses=new File(target,"test-classes");
        String classpath=testclasses.getAbsolutePath()+java.io.File.pathSeparator+cp;

        ProcessBuilder processBuilder = new ProcessBuilder();

        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            processBuilder.command("java.exe", "-cp",classpath,"--enable-preview","dev.gruff.billion_records.TestRunner",cp,main,testfile,""+count);
        } else {
            processBuilder.command("java", "-cp", classpath,"--enable-preview","dev.gruff.billion_records.TestRunner",cp,main,testfile,""+count);
        }

        // Setting the output file for redirection
            processBuilder.redirectOutput(outputFile);
            processBuilder.redirectError(outputFile);
        try {

            Process process = processBuilder.start();
            // Wait for the process to finish
        int exitCode = 0;

            exitCode = process.waitFor();
            exitCode = process.exitValue();
            return TestResult.result(exitCode);

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }



    }

    private static File getMain(File proj) throws IOException {

        File target=new File(proj,"target");
        File classes=new File(target,"classes");

        Optional<Path> mainClass=Files.find(classes.toPath(), 10, (path, basicFileAttributes) -> isMainClass(path))
                .findFirst();

        return mainClass.map(Path::toFile).orElse(null);
    }

    private static boolean isMainClass(Path path) {
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
