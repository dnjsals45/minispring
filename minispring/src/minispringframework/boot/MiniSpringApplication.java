package minispringframework.boot;

import minispringframework.server.Server;

import java.util.concurrent.Executors;

public class MiniSpringApplication {
    public static void run() {
        Server server = new Server(8080, Executors.newFixedThreadPool(10));
        server.start();
    }
}
