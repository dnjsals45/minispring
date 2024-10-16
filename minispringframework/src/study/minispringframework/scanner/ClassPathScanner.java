package study.minispringframework.scanner;

import study.minispringframework.annotation.Component;
import study.minispringframework.annotation.Controller;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

@Component
public class ClassPathScanner {

    public List<Class<?>> scanPackage(String basePackage) throws IOException, ClassNotFoundException {
        List<Class<?>> classes = new ArrayList<>();
        String path = basePackage.replace(".", "/");
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Enumeration<URL> resources = classLoader.getResources(path);

        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            File file = new File(resource.getFile());
            scanDirectory(file, basePackage, classes);
        }

        return classes;
    }

    private void scanDirectory(File directory, String basePackage, List<Class<?>> classes) throws ClassNotFoundException {
        if (directory.exists()) {
            for (File file : directory.listFiles()) {
                if (file.isDirectory()) {
                    scanDirectory(file, basePackage + "." + file.getName(), classes);
                } else if (file.getName().endsWith(".class")) {
                    String className = basePackage + "." + file.getName().substring(0, file.getName().length() - 6);
                    Class<?> clazz = Class.forName(className);
                    // Controller 어노테이션이 존재할 때만 class를 등록한다.
                    if (clazz.isAnnotationPresent(Controller.class)) {
                        classes.add(clazz);
                    }
                }
            }
        }
    }
}
