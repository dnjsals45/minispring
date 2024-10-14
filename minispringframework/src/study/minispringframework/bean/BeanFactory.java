package study.minispringframework.bean;

import study.minispringframework.annotation.Component;
import study.minispringframework.scanner.AnnotationScanner;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.*;

public class BeanFactory {
    private static BeanFactory beanFactory;
    private final Map<String, Object> beans = new HashMap<>();
    private final Map<Class<?>, Set<Class<?>>> dependencyGraph = new HashMap<>();
    private final Map<Class<?>, Integer> inDegree = new HashMap<>();

    public static BeanFactory getInstance() {
        if (beanFactory == null) {
            beanFactory = new BeanFactory();
        }
        return beanFactory;
    }

    public Object getBean(String beanName) {
        return beans.get(beanName);
    }

    public void setBeans(File directory, String basePackage) {
        try {
            List<Class<?>> classes = new ArrayList<>();
            scanClasses(directory, basePackage, classes);
            buildDependencyGraph(classes);
            List<Class<?>> sortedClasses = topologicalSort();
            createAndRegisterBeans(sortedClasses);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void scanClasses(File directory, String basePackage, List<Class<?>> cl) {
        File[] files = directory.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                scanClasses(file, basePackage + "." + file.getName(), cl);
            } else if (file.getName().endsWith(".class")) {
                String className = basePackage + "." + file.getName().substring(0, file.getName().length() - 6);
                try {
                    cl.add(Class.forName(className));
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void buildDependencyGraph(List<Class<?>> classes) {
        for (Class<?> clazz : classes) {
            dependencyGraph.put(clazz, new HashSet<>());
            inDegree.put(clazz, 0);
        }

        for (Class<?> clazz : classes) {
            for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
                for (Class<?> parameterType : constructor.getParameterTypes()) {
                    if (dependencyGraph.containsKey(parameterType)) {
                        dependencyGraph.get(parameterType).add(clazz);
                        inDegree.put(clazz, inDegree.get(parameterType) + 1);
                    }
                }
            }
        }
    }

    private List<Class<?>> topologicalSort() {
        List<Class<?>> result = new ArrayList<>();
        Queue<Class<?>> queue = new LinkedList<>();

        for (Map.Entry<Class<?>, Integer> entry : inDegree.entrySet()) {
            if (entry.getValue() == 0) {
                queue.offer(entry.getKey());
            }
        }

        while (!queue.isEmpty()) {
            Class<?> clazz = queue.poll();
            result.add(clazz);

            for (Class<?> dependent : dependencyGraph.get(clazz)) {
                inDegree.put(dependent, inDegree.get(dependent) - 1);
                if (inDegree.get(dependent) == 0) {
                    queue.offer(dependent);
                }
            }
        }

        if (result.size() != dependencyGraph.size()) {
            throw new RuntimeException("Circular dependency detected");
        }

        return result;
    }

    private void createAndRegisterBeans(List<Class<?>> classes) {
        for (Class<?> clazz : classes) {
            if (AnnotationScanner.getInstance().hasAnnotation(clazz, Component.class) && isInstantiable(clazz)) {
                Object bean = createBean(clazz);
                beans.put(getBeanName(clazz), bean);
            }
        }
    }

    private Object createBean(Class<?> clazz) {
        try {
            Constructor<?>[] constructors = clazz.getDeclaredConstructors();
            Arrays.sort(constructors, Comparator.comparingInt(Constructor::getParameterCount));

            for (Constructor<?> constructor : constructors) {
                if (canCreateBeanWithConstructor(constructor)) {
                    Class<?>[] paramTypes = constructor.getParameterTypes();
                    Object[] params = new Object[paramTypes.length];
                    for (int i = 0; i < paramTypes.length; i++) {
                        params[i] = beans.get(getBeanName(paramTypes[i]));
                    }
                    return constructor.newInstance(params);
                }
            }
            throw new RuntimeException("No suitable constructor found for " + clazz.getName());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error creating bean for " + clazz.getName(), e);
        }
    }

    private boolean canCreateBeanWithConstructor(Constructor<?> constructor) {
        Class<?>[] paramTypes = constructor.getParameterTypes();
        for (Class<?> paramType : paramTypes) {
            if (!beans.containsKey(getBeanName(paramType))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Interface, Enum, Annotation 파일은 instance 생성 불가
     */
    private boolean isInstantiable(Class<?> clazz) {
        return !clazz.isInterface() && !clazz.isEnum() && !clazz.isAnnotation();
    }

    private String getBeanName(Class<?> clazz) {
        String beanName = clazz.getSimpleName();
        return Character.toLowerCase(beanName.charAt(0)) + beanName.substring(1);
    }
}
