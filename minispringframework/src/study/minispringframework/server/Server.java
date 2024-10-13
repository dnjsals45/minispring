package study.minispringframework.server;

import study.minispringframework.annotation.Component;
import study.minispringframework.parser.HttpRequest;
import study.minispringframework.parser.HttpResponse;
import study.minispringframework.servlet.DisPatcherServlet;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

@Component
public class Server {
    private int port;
    private ExecutorService executorService;

    public Server(int port, ExecutorService executorService) {
        this.port = port;
        this.executorService = executorService;
    }

    public void start() {
        try (ServerSocket socket = new ServerSocket(port)) {
            System.out.println("Server started on port " + port);

            while (true) {
                Socket clientSocket = socket.accept();
                executorService.submit(() -> handleRequest(clientSocket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleRequest(Socket clientSocket) {
        try {
            HttpRequest request = HttpRequest.parse(clientSocket.getInputStream());
            HttpResponse response = new HttpResponse(clientSocket.getOutputStream());

            response.send();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
