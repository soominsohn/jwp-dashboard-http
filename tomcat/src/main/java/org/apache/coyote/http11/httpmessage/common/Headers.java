package org.apache.coyote.http11.httpmessage.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class Headers {

    final Map<String, String> headers;

    public Headers() {
        this.headers = new LinkedHashMap<>();
    }

    public Headers(Map<String, String> headers) {
        this.headers = headers;
    }

    public Headers(BufferedReader bufferedReader) throws IOException {
        headers = new HashMap<>();
        while (true) {
            String buffer = bufferedReader.readLine();
            if (buffer.equals("\r\n") || "".equals(buffer)) {
                break;
            }
            String key = buffer.split(": ")[0];
            String value = buffer.split(": ")[1];
            headers.put(key, value);
        }
    }

    public Headers add(final String fieldName, final String fieldValue) {
        Map<String, String> newHeaders = new LinkedHashMap<>(headers);
        newHeaders.put(fieldName, fieldValue);

        return new Headers(newHeaders);
    }

    public int getContentLength() {
        return Integer.parseInt(headers.getOrDefault("Content-Length", "0"));
    }

    public boolean exist(final String headerName) {
        return headers.containsKey("headerName");
    }

    public String getValue(final String headerName) {
        return headers.get(headerName);
    }

    public String parseToString() {
        return headers.entrySet()
                .stream()
                .map(it -> it.getKey() + ": " + it.getValue() + " ")
                .collect(Collectors.joining("\r\n"));
    }
}
