package dev.gruff.billion_records;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Timer;
import java.util.TimerTask;

public class TestRunner {

    public static void main(String[] args) {


        String classPath = args[0];
        String mainClass = args[1];
        String testFile = args[2];
        int count = Integer.parseInt(args[3]);

        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            if (e instanceof OutOfMemoryError) {
                System.exit(TestResult.OOM.code());
            } else {
                System.exit(TestResult.UNKNOWN.code());
            }
        });

        try {
            LocalClassInstantiator lc = new LocalClassInstantiator(new File(classPath));
            Method m = lc.getMain(mainClass);
            execute(m, testFile, count);
            System.exit(TestResult.PASS.code());
        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            System.exit(TestResult.ERROR.code());
        } catch(Throwable t) {
            if(t instanceof OutOfMemoryError) {
                System.exit(TestResult.OOM.code());
            }
        }
        System.exit(TestResult.UNKNOWN.code());

    }

        private  static void execute(Method method,String testFile, int count) throws InvocationTargetException, IllegalAccessException {

        watchDog(180);


            for(int i=0;i<count;++i) {
                method.invoke(null, (Object) new String[]{testFile});
            }


    }

    public static void watchDog(int seconds) {
        // Create a timer
        Timer timer = new Timer();

        // Schedule a task to run after a delay (e.g., 5000 milliseconds = 5 seconds)
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                // Code to execute when the timer completes
                System.out.println("Watch Dog Triggered!");
                System.out.println("Watch Dog Triggered!");
                System.out.println("Watch Dog Triggered!");
                System.out.println("Watch Dog Triggered!");

                timer.cancel();
                System.exit(TestResult.TIMEOUT.code());
            }
        }, seconds * 1000L); // Delay in milliseconds


    }


}

