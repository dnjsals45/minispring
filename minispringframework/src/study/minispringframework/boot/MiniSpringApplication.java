package study.minispringframework.boot;

import study.minispringframework.ioc.MiniIoCContainer;
import study.minispringframework.server.Server;
import study.minispringframework.server.ServerConfig;
import study.minispringframework.servlet.DisPatcherServlet;

import java.util.concurrent.Executors;

public class MiniSpringApplication {
    public static void run(Class<?> source, String... args) {
        MiniIoCContainer container = MiniIoCContainer.getInstance();
        String basePackage = source.getPackage().getName();
        container.setUpBean(basePackage);
        Server server = new Server(8080, Executors.newFixedThreadPool(10), basePackage);
        server.start();
    }
}
