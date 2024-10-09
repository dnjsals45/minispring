package minispringframework.server;

import minispringframework.parser.HttpRequest;
import minispringframework.parser.HttpResponse;
import minispringframework.servlet.DisPatcherServlet;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

public class Server {
    private int port;
    private ExecutorService executorService;
    private DisPatcherServlet disPatcherServlet;

    public Server(int port, ExecutorService executorService) {
        this.port = port;
        this.executorService = executorService;
        this.disPatcherServlet = new DisPatcherServlet();
        initDisPatcherServlet();
    }

    private void initDisPatcherServlet() {
        disPatcherServlet.init();
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

            disPatcherServlet.service(request, response);

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
