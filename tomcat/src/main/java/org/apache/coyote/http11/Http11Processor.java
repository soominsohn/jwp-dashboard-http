package org.apache.coyote.http11;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import nextstep.jwp.db.InMemoryUserRepository;
import nextstep.jwp.exception.UncheckedServletException;
import nextstep.jwp.model.User;
import org.apache.coyote.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Http11Processor implements Runnable, Processor {

    private static final Logger log = LoggerFactory.getLogger(Http11Processor.class);

    private final Socket connection;

    public Http11Processor(final Socket connection) {
        this.connection = connection;
    }

    @Override
    public void run() {
        process(connection);
    }

    @Override
    public void process(final Socket connection) {
        try (final var inputStream = connection.getInputStream();
             final var outputStream = connection.getOutputStream()) {
            final var bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            final var requestLine = bufferedReader.readLine();

            if (requestLine == null) {
                throw new IllegalStateException("잘못된 요청입니다.");
            }

            final var httpRequestMethod = requestLine.split(" ")[0];

            String response = null;

            if (httpRequestMethod.equals("GET")) {
                response = getRequest(requestLine);
            }

            outputStream.write(response.getBytes());
            outputStream.flush();
        } catch (IOException | UncheckedServletException e) {
            log.error(e.getMessage(), e);
        }
    }

    private String getRequest(String requestLine) throws IOException {

        final var uri = requestLine.split(" ")[1];

        if (uri.contains(".")) {
            return getResponseWithFileName(uri);
        }

        if (uri.contains("?")) {
            return getResponseWithQueryString(uri);
        }

        return getResponse(uri);
    }

    private String getResponse(String uri) throws IOException {
        if (uri.equals("/login")) {
            return getResponseWithFileName("/login.html");
        }
        return "";
    }

    private String getResponseWithFileName(String fileName) throws IOException {
        final var fileType = fileName.split("\\.")[1];

        String contentType = "text/html";

        if (fileType.equals("html")) {
            contentType = "text/html";
        }

        if (fileType.equals("css")) {
            contentType = "text/css";
        }

        if (fileType.equals("js")) {
            contentType = "text/javascript";
        }

        final var resource = getClass().getClassLoader().getResource("static" + fileName);

        final var path = new File(resource.getPath()).toPath();

        final var responseBody = new String(Files.readAllBytes(path));

        return String.join("\r\n",
                "HTTP/1.1 200 OK ",
                "Content-Type: " + contentType + ";charset=utf-8 ",
                "Content-Length: " + responseBody.getBytes().length + " ",
                "",
                responseBody);

    }

    private String getResponseWithQueryString(String uri) throws IOException {
        if (uri.contains("/login")) {

            Map<String, String> queryStrings = extractQueryStrings(uri);

            Optional<User> user = InMemoryUserRepository.findByAccount(queryStrings.get("account"));

            if (user.isPresent() && user.get().checkPassword(queryStrings.get("password"))) {
                return String.join("\r\n",
                        "HTTP/1.1 302 ",
                        "Location: http://localhost:8080/index.html ",
                        "Content-Type: text/html;charset=utf-8 ");
            }
            return String.join("\r\n",
                    "HTTP/1.1 302 ",
                    "Location: http://localhost:8080/401.html ",
                    "Content-Type: text/html;charset=utf-8 ");
        }
        return "";
    }

    private Map<String, String> extractQueryStrings(String uri) {
        Map<String, String> queryStrings = new HashMap<>();
        int index = uri.indexOf("?");

        for (String queryString : uri.substring(index + 1).split("&")) {
            String name = queryString.split("=")[0];
            String value = queryString.split("=")[1];
            queryStrings.put(name, value);
        }
        return queryStrings;
    }
}