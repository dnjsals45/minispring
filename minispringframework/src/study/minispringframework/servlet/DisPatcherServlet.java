package study.minispringframework.servlet;

import study.minispringframework.annotation.Component;
import study.minispringframework.annotation.Controller;
import study.minispringframework.annotation.RequestMapping;
import study.minispringframework.ioc.MiniIoCContainer;
import study.minispringframework.parser.HttpRequest;
import study.minispringframework.parser.HttpResponse;
import study.minispringframework.scanner.ClassPathScanner;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class DisPatcherServlet {
    private Map<String, Object> controllerMap;
    private Map<String, Method> handlerMap;
    private ClassPathScanner scanner;

    public DisPatcherServlet() {
        this.controllerMap = new HashMap<>();
        this.handlerMap = new HashMap<>();
        this.scanner = new ClassPathScanner();
    }

    public void init(String basePackage) {
        scanController(basePackage);
    }

    private void scanController(String basePackage) {
        // classPath 을 스캔하여 컨트롤러를 등록
        try {
            List<Class<?>> clazz = scanner.scanPackage(basePackage);
            for (Class<?> controller : clazz) {
                if (controller.isAnnotationPresent(Controller.class)) {
                    Object instance = getClassWithParameter(controller);
                    RequestMapping requestMapping = controller.getAnnotation(RequestMapping.class);
                    String baseUrl = requestMapping.value();
                    registerController(baseUrl, instance);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
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

    private void registerController(String baseUrl, Object controller) {
        controllerMap.put(baseUrl, controller);
        for (Method method : controller.getClass().getMethods()) {
            if (method.isAnnotationPresent(RequestMapping.class)) {
                RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
                String url = baseUrl + requestMapping.value();
                handlerMap.put(url, method);
            }
        }
    }

    public void service(HttpRequest request, HttpResponse response) {
        String path = request.getPath();
        Method handler = handlerMap.get(path);

        if (handler != null) {
            try {
                Object controller = controllerMap.get(path.substring(0, path.lastIndexOf("/")));
                Object result = handler.invoke(controller);
                handleResult(response, result);
            } catch (Exception e) {
                e.printStackTrace();
                response.setStatus(500, "Internal Server Error");
                response.write("500 Internal Server Error");
            }
        } else {
            response.setStatus(404, "Not Found");
            response.write("404 Not Found");
        }
    }

    private void handleResult(HttpResponse response, Object result) {
        if (result instanceof String) {
            response.write((String) result);
        } else {
            response.write(result.toString());
        }
    }
}
