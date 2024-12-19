package dev.gruff.billion_records;

import java.io.File;
import java.lang.reflect.Method;

public class TestRunner {

    public static void main(String[] args) {

        String classPath=args[0];
        String mainClass=args[1];
        String testFile=args[2];
        int count=Integer.parseInt(args[3]);

        try {
            LocalClassInstantiator lc = new LocalClassInstantiator(new File(classPath));
            Method m = lc.getMain(mainClass);
            execute(m,testFile,count);

        } catch(Exception e) {
            e.printStackTrace();
            System.exit(-200);
        }
        System.exit(0);
    }

    private  static void execute(Method method,String testFile, int count) {
        try {
            for(int i=0;i<count;++i) {
                method.invoke(null, (Object) new String[]{testFile});
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-100);
        }
    }


}
