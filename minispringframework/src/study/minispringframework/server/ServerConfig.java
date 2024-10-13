package study.minispringframework.server;

import study.minispringframework.annotation.Component;

@Component
public class ServerConfig {
    private final int port;
    private final int threadPoolSize;

    public ServerConfig() {
        this.port = 8080;
        this.threadPoolSize = 10;
    }

    public ServerConfig(int port, int threadPoolSize) {
        this.port = port;
        this.threadPoolSize = threadPoolSize;
    }

    public int getPort() {
        return port;
    }

    public int getThreadPoolSize() {
        return threadPoolSize;
    }
}
