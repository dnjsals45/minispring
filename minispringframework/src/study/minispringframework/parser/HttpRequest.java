package study.minispringframework.parser;

import study.minispringframework.annotation.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Component
public class HttpRequest {
    private String method;
    private String path;
    private String version;
    private Map<String, String> headers;
    private String body;

    private HttpRequest() {
        this.headers = new HashMap<>();
    }

    public static HttpRequest parse(InputStream inputStream) throws IOException {
        HttpRequest request = new HttpRequest();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        String requestLine = reader.readLine();
        String[] requestParts = requestLine.split(" ");
        request.method = requestParts[0];
        request.path = requestParts[1];
        request.version = requestParts[2];

        String headerLine;
        while (!(headerLine = reader.readLine()).isEmpty()) {
            String[] headerParts = headerLine.split(": ");
            request.headers.put(headerParts[0], headerParts[1]);
        }

        if ("POST".equalsIgnoreCase(request.method) || "PUT".equalsIgnoreCase(request.method) || "DELETE".equalsIgnoreCase(request.method)) {
            int contentLength = Integer.parseInt(request.headers.get("Content-Length"));
            if (contentLength > 0) {
                char[] body = new char[contentLength];
                reader.read(body, 0, contentLength);
                request.body = new String(body);
            }
        }

        return request;
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public String getVersion() {
        return version;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getBody() {
        return body;
    }

    @Override
    public String toString() {
        return "MiniHttpRequest = " +
                "method='" + method + '\'' +
                ", path='" + path + '\'' +
                ", version='" + version + '\'' +
                ", headers=" + headers +
                ", body='" + body + '\'' +
                '}';
    }
}
