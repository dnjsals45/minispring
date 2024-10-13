package study.minispringframework.ioc;

import study.minispringframework.annotation.Component;

import java.io.File;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

// 싱글톤 패턴 사용(컨테이너에서 생성한 인스턴스를 전역적으로 단 1개만 사용 -> 메모리 효율 증가)
public class MiniIoCContainer {
    private static MiniIoCContainer miniIoCContainer;
    private final String INTERNAL_BEAN_PACKAGE = "study.minispringframework";

    private Map<String, Object> beanMap = new HashMap<>();

    public static MiniIoCContainer getInstance() {
        if (miniIoCContainer == null) {
            miniIoCContainer = new MiniIoCContainer();
        }
        return miniIoCContainer;
    }

    public void setUpBean(String basePackage) {
        try {
            registerInternalBeans(INTERNAL_BEAN_PACKAGE);
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            String path = basePackage.replace(".", "/");
            URL resource = classLoader.getResource(path);
            File directory = new File(resource.getFile());
            scanDirectory(directory, basePackage);

            for (Map.Entry<String, Object> entry : beanMap.entrySet()) {
                System.out.println(entry.getKey() + " : " + entry.getValue());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void registerInternalBeans(String internalBeanPackage) {
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            URL resource = classLoader.getResource(internalBeanPackage.replace(".", "/"));
            File directory = new File(resource.getFile());
            scanDirectory(directory, internalBeanPackage);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Object getBean(String beanName) {
        return beanMap.get(beanName);
    }

    private void scanDirectory(File directory, String basePackage) {
        File[] files = directory.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                scanDirectory(file, basePackage + "." + file.getName());
            } else if (file.getName().endsWith(".class")) {
                String className = basePackage + "." + file.getName().substring(0, file.getName().length() - 6);
                createBeanFromClass(className);
            }
        }
    }

    private void createBeanFromClass(String className) {
        try {
            Class<?> clazz = Class.forName(className);
            if (clazz.isAnnotationPresent(Component.class) && isInstable(clazz)) {
                Constructor<?> constructor = clazz.getDeclaredConstructor();
                Object instance = constructor.newInstance();
                String beanName = getBeanName(clazz);
                beanMap.put(beanName, instance);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String getBeanName(Class<?> clazz) {
        Component component = clazz.getAnnotation(Component.class);
        String beanName = component.value();
        if (beanName.isEmpty()) {
            beanName = clazz.getSimpleName();
            beanName = Character.toLowerCase(beanName.charAt(0)) + beanName.substring(1);
        }

        return beanName;
    }

    private boolean isInstable(Class<?> clazz) {
        return !clazz.isInterface() && !clazz.isEnum() && !clazz.isAnnotation();
    }
}
