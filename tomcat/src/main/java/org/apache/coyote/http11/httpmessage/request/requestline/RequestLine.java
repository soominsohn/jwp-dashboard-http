package org.apache.coyote.http11.httpmessage.request.requestline;

import org.apache.coyote.http11.httpmessage.common.HttpVersion;

public class RequestLine {

    private final Method method;
    private final RequestUri requestUri;
    private final HttpVersion httpVersion;

    public RequestLine(final Method method, final RequestUri requestUri,
                       final HttpVersion httpVersion) {
        this.method = method;
        this.requestUri = requestUri;
        this.httpVersion = httpVersion;
    }

    public static RequestLine from(final String requestLine) {
        final String[] requestLineParameters = requestLine.split(" ");

        final Method method = Method.find(requestLineParameters[0]);
        final RequestUri requestUri = RequestUri.from(requestLineParameters[1]);
        final HttpVersion httpVersion = HttpVersion.HTTP1_1;

        return new RequestLine(method, requestUri, httpVersion);
    }

    public boolean isGetMethod() {
        return method.isGet();
    }

    public RequestUri getRequestUri() {
        return requestUri;
    }

    public boolean isMatchUri(final String requestUri) {
        return this.requestUri.isMatchUri(requestUri);
    }
}
