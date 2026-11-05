package dev.recallforge.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import com.sun.net.httpserver.HttpServer;

/**
 * Long Optimize runs keep OpenAI silent until the whole response is ready,
 * so a slow-but-healthy answer must not be cut off by a client default.
 */
class OpenAiConfigTest {

    private static final long SLOW_RESPONSE_MILLIS = 12_000;

    private HttpServer server;

    @BeforeEach
    void startSlowServer() throws Exception {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/slow", exchange -> {
            try {
                Thread.sleep(SLOW_RESPONSE_MILLIS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            byte[] body = "done".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();
    }

    @AfterEach
    void stopServer() {
        server.stop(0);
    }

    @Test
    void slowResponse_isNotCutOffByClientTimeout() {
        RestClient client = RestClient.builder()
                .baseUrl("http://127.0.0.1:" + server.getAddress().getPort())
                .requestFactory(OpenAiConfig.requestFactory())
                .build();

        String body = client.get().uri("/slow").retrieve().body(String.class);

        assertThat(body).isEqualTo("done");
    }
}
