package minispringframework.parser;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

public class HttpResponse {
    private int statusCode;
    private String statusMessage;
    private Map<String, String> headers;
    private StringBuilder body;
    private OutputStream outputStream;

    public HttpResponse(OutputStream outputStream) {
        this.statusCode = 200;
        this.statusMessage = "OK";
        this.headers = new HashMap<>();
        this.body = new StringBuilder();
        this.outputStream = outputStream;

        headers.put("Content-Type", "text/html; charset=UTF-8");
    }

    public void setStatus(int statusCode, String statusMessage) {
        this.statusCode = statusCode;
        this.statusMessage = statusMessage;
    }

    public void setHeader(String name, String value) {
        headers.put(name, value);
    }

    public void write(String content) {
        body.append(content);
    }

    public void send() {
        try (PrintWriter writer = new PrintWriter(outputStream, true)) {
            writer.println("HTTP/1.1 " + statusCode + " " + statusMessage);

            headers.put("Content-Length", String.valueOf(body.length()));
            for (Map.Entry<String, String> header : headers.entrySet()) {
                writer.println(header.getKey() + ": " + header.getValue());
            }

            writer.println();
            writer.println(body.toString());
            writer.flush();
        }
    }
}
