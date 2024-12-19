package dev.gruff.billion_records;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Method;

public class LocalClassInstantiator {

    File root;
   // Object instance;
    Method method;
    ClassLoader classLoader;

    public LocalClassInstantiator(File root) throws Exception {
        this.root=root;
        classLoader = new ClassLoader() {
            @Override
            protected Class<?> findClass(String name) throws ClassNotFoundException {
                byte[] classBytes = null;
                try {
                    classBytes = load(name);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                return defineClass(name, classBytes, 0, classBytes.length);
            }
        };
    }


    private byte[] load(String className) throws Exception {

        String fname=className.replace(".","/")+".class";
        File classFile=new File(root,fname);
        // Load the class bytes
        int classLength = (int) classFile.length();
        byte[] classBytes;
        try (FileInputStream fis = new FileInputStream(classFile)) {
            classBytes = new byte[classLength];
            int read = fis.read(classBytes);
            if (read != classLength) {
                throw new RuntimeException("failed to complete reading class file");
            }
            return classBytes;
        }

    }

    public Method getMain(String className) throws Exception {




        // Load the class
        Class<?> loadedClass = classLoader.loadClass(className);

        // Call a method (assuming the method name and signature)
        return loadedClass.getMethod("main", String[].class);
    }




}
