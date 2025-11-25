package utils;

import clients.ApiClient;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public abstract class WireMockBase {
    protected static WireMockServer wireMockServer;
    protected ApiClient apiClient = new ApiClient();

    @BeforeAll
    static void startWireMock() {
        wireMockServer = new WireMockServer(8888);
        wireMockServer.start();
    }

    @AfterAll
    static void stopWireMock() {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }

    @BeforeEach
    void setup() {
        wireMockServer.resetAll();
        wireMockServer.stubFor(post(urlEqualTo("/auth")).willReturn(aResponse().withStatus(200)));
        wireMockServer.stubFor(post(urlEqualTo("/doAction")).willReturn(aResponse().withStatus(200)));
    }
}
