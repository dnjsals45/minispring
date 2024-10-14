package study.minispringframework.scanner;

import java.lang.annotation.Annotation;

public class AnnotationScanner {
    private static AnnotationScanner instance = new AnnotationScanner();

    public static AnnotationScanner getInstance() {
        return instance;
    }

    public boolean hasAnnotation(Class<?> clazz, Class<? extends Annotation> target) {
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

    public Class<? extends Annotation> findAnnotation(Class<?> clazz, Class<? extends Annotation> target) {
        if (clazz.isAnnotationPresent(target)) {
            return target;
        }

        Annotation[] annotations = clazz.getAnnotations();
        for (Annotation annotation : annotations) {
            if (annotation.annotationType().isAnnotationPresent(target)) {
                return annotation.annotationType();
            }
        }
        return null;
    }
}
