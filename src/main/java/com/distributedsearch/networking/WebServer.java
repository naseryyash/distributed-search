package http.server;

import com.distributedsearch.networking.OnRequestCallback;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.Executors;

import static http.server.WebServerConstants.*;

public class WebServer {

    private HttpServer server;

    private final int port;

    private final OnRequestCallback onRequestCallback;

    public WebServer(
            int port,
            OnRequestCallback onRequestCallback
    ) {
        this.port = port;
        this.onRequestCallback = onRequestCallback;
    }

    public void startServer() {
        try {
            server =
                    HttpServer.create(
                            new InetSocketAddress(this.port), 0);

            HttpContext statusContext = server.createContext(STATUS_ENDPOINT);
            HttpContext taskContext = server.createContext(TASK_ENDPOINT);

            statusContext.setHandler(this::handleStatusCheckRequest);
            taskContext.setHandler(this::handleTaskRequest);

            server.setExecutor(Executors.newFixedThreadPool(MAX_THREADPOOL_SIZE));
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleTaskRequest(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase(HTTP_POST)) {
            exchange.close();
            return;
        }

        Headers headers = exchange.getRequestHeaders();
        if (headers.containsKey(X_TEST_HEADER) &&
            headers.get(X_TEST_HEADER).get(0).equalsIgnoreCase(TRUE_STRING)) {
            String dummyResponse = "123\n";
            sendResponse(dummyResponse.getBytes(), exchange);
            return;
        }

        boolean isDebugMode = headers.containsKey(X_DEBUG_HEADER) &&
                headers.get(X_DEBUG_HEADER).get(0).equalsIgnoreCase(TRUE_STRING);

        long startTime = System.nanoTime();

        byte[] requestBytes = exchange.getRequestBody().readAllBytes();
        byte[] responseBytes = calculateResponse(requestBytes);

        long finishTime = System.nanoTime();

        if (isDebugMode) {
            String debugMessage = String.format("Operation took %d ns",
                    finishTime - startTime);
            exchange.getResponseHeaders()
                    .put(X_DEBUG_INFO_HEADER, List.of(debugMessage));
        }

        sendResponse(responseBytes, exchange);
    }

    private byte[] calculateResponse(byte[] requestBytes) {
        String bodyString = new String(requestBytes);
        String[] stringNumbers = bodyString.split(COMMA);

        BigInteger result = BigInteger.ONE;

        for (String number: stringNumbers) {
            result = result.multiply(new BigInteger(number));
        }

        return String
                .format("Result of the multiplication is %s\n", result)
                .getBytes();
    }

    private void handleStatusCheckRequest(HttpExchange exchange)
            throws IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase(HTTP_GET)) {
            exchange.close();
            return;
        }

        String responseMessage = "Server is alive!";
        sendResponse(responseMessage.getBytes(), exchange);
    }

    private void sendResponse(byte[] responseBytes, HttpExchange exchange)
            throws IOException {
        exchange.sendResponseHeaders(200, responseBytes.length);
        OutputStream outputStream = exchange.getResponseBody();
        outputStream.write(responseBytes);
        outputStream.flush();
        outputStream.close();
    }

}
