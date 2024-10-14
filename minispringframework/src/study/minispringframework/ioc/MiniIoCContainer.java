package study.minispringframework.ioc;

import study.minispringframework.bean.BeanFactory;
import study.minispringframework.scanner.AnnotationScanner;
import study.minispringframework.annotation.Component;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

// 싱글톤 패턴 사용(컨테이너에서 생성한 인스턴스를 전역적으로 단 1개만 사용 -> 메모리 효율 증가)
public class MiniIoCContainer {
    private static MiniIoCContainer miniIoCContainer;

    public static MiniIoCContainer getInstance() {
        if (miniIoCContainer == null) {
            miniIoCContainer = new MiniIoCContainer();
        }
        return miniIoCContainer;
    }

    /**
     * 모듈의 클래스 파일들 스캔하여 BeanFactory 를 통해 Bean 으로 등록
     */
    public void setUpBean(String basePackage) {
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            String path = basePackage.replace(".", "/");
            URL resource = classLoader.getResource(path);
            File directory = new File(resource.getFile());
            BeanFactory.getInstance().setBeans(directory, basePackage);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
