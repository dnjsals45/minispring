package study.minispringframework.ioc;

import study.minispringframework.annotation.Component;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

// 싱글톤 패턴 사용(컨테이너에서 생성한 인스턴스를 전역적으로 단 1개만 사용 -> 메모리 효율 증가)
public class MiniIoCContainer {
    private static MiniIoCContainer miniIoCContainer;

    private Map<String, Object> beanMap = new HashMap<>();

    public static MiniIoCContainer getInstance() {
        if (miniIoCContainer == null) {
            miniIoCContainer = new MiniIoCContainer();
        }
        return miniIoCContainer;
    }

    public void setUpBean(String basePackage) {
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            String path = basePackage.replace(".", "/");
            URL resource = classLoader.getResource(path);
            File directory = new File(resource.getFile());
            scanDirectory(directory, basePackage);
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
            System.out.println("className = " + className);
            if (hasAnnotation(clazz, Component.class) && isInstantiable(clazz)) {
                Object instance = getClassWithParameter(clazz);
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

    private boolean isInstantiable(Class<?> clazz) {
        return !clazz.isInterface() && !clazz.isEnum() && !clazz.isAnnotation();
    }

    private boolean hasAnnotation(Class<?> clazz, Class<? extends Annotation> target) {
        if (clazz.isAnnotationPresent(target)) {
            return true;
        }

        Annotation[] annotations = clazz.getAnnotations();
        for (Annotation annotation : annotations) {
            if (annotation.annotationType().isAnnotationPresent(target)) {
                return true;
            }
        }
        return false;
    }

    private Object getClassWithParameter(Class<?> controller) throws InvocationTargetException, InstantiationException, IllegalAccessException {
        MiniIoCContainer container = MiniIoCContainer.getInstance();
        Constructor<?>[] constructors = controller.getConstructors();

        for (Constructor<?> constructor : constructors) {
            if (constructor.getParameterCount() == 0) {
                return constructor.newInstance();
            } else {
                Class<?>[] parameterTypes = constructor.getParameterTypes();
                Object[] parameters = new Object[parameterTypes.length];
                for (int i = 0; i < parameterTypes.length; i++) {
                    parameters[i] = container.getBean(parameterTypes[i].getName());
                }

                return constructor.newInstance(parameters);
            }
        }

        return null;
    }
}
