package org.example.tests;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URI;

public abstract class MockHttp extends HttpExchange {

    private final ByteArrayOutputStream response = new ByteArrayOutputStream();
    private final InputStream requestBody;
    private final Headers requestHeaders = new Headers();
    private final Headers responseHeaders = new Headers();
    private int responseCode;
    private final String method;

    public MockHttp(String method, String body) {
        this.method = method;
        this.requestBody = new ByteArrayInputStream(body.getBytes());
        this.requestHeaders.add("Content-Type", "application/json");
    }

    @Override
    public String getRequestMethod() {
        return method;
    }

    @Override
    public InputStream getRequestBody() {
        return requestBody;
    }

    @Override
    public OutputStream getResponseBody() {
        return response;
    }

    @Override
    public Headers getRequestHeaders() {
        return requestHeaders;
    }

    @Override
    public Headers getResponseHeaders() {
        return responseHeaders;
    }

    @Override
    public void sendResponseHeaders(int code, long length) {
        this.responseCode = code;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public String getResponseString() {
        return response.toString();
    }

    // REQUIRED ABSTRACT METHODS BELOW

    @Override
    public URI getRequestURI() {
        return URI.create("/");
    }

    @Override
    public String getProtocol() {
        return "HTTP/1.1";
    }

    @Override
    public InetSocketAddress getRemoteAddress() {
        return new InetSocketAddress(0);
    }

    @Override
    public InetSocketAddress getLocalAddress() {
        return new InetSocketAddress(0);
    }

    @Override
    public HttpContext getHttpContext() {
        return null;
    }

    @Override
    public void close() {}

    @Override
    public void setStreams(InputStream i, OutputStream o) {}
}
