package minispringframework.servlet;

import minispringframework.annotation.RequestMapping;
import minispringframework.parser.HttpRequest;
import minispringframework.parser.HttpResponse;
import minispringframework.scanner.ClassPathScanner;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DisPatcherServlet {
    private Map<String, Object> controllerMap;
    private Map<String, Method> handlerMap;
    private ClassPathScanner scanner;

    public DisPatcherServlet() {
        this.controllerMap = new HashMap<>();
        this.handlerMap = new HashMap<>();
        this.scanner = new ClassPathScanner();
    }

    public void init() {
        scanController();
    }

    private void scanController() {
        // classPath 을 스캔하여 컨트롤러를 등록
        try {
            List<Class<?>> clazz = scanner.scanPackage("out.production.minispring.project");
            for (Class<?> controller : clazz) {
                if (controller.isAnnotationPresent(minispringframework.annotation.Controller.class)) {
                    Object instance = controller.getDeclaredConstructor().newInstance();
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
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private void registerController(String baseUrl, Object controller) {
        controllerMap.put(baseUrl, controller);
        for (Method method : controller.getClass().getMethods()) {
            if (method.isAnnotationPresent(RequestMapping.class)) {
                RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
                String url = baseUrl + requestMapping.value();
                controllerMap.put(url, method);
            }
        }
    }

    public void service(HttpRequest request, HttpResponse response) {
        String path = request.getPath();
        Method handler = handlerMap.get(path);

        if (handler != null) {
            try {
                Object controller = controllerMap.get(path.substring(0, path.lastIndexOf("/")));
                Object result = handler.invoke(controller, request, response);
                handleResult(response, result);
            } catch (Exception e) {
                e.printStackTrace();
                response.setStatus(500, "Internal Server Error");
                response.write("An error occurred: " + e.getMessage());
            }
        } else {
            response.setStatus(404, "Not Found");
            response.write("404 Not Found: " + path);
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
