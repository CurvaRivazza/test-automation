package utils;

import clients.ApiClient;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import io.qameta.allure.Allure;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class WireMockBase {
    protected static WireMockServer wireMockServer;
    protected static WireMock wireMockClient;

    @BeforeAll
    static void startWireMock() {
        Allure.step("Setting up the test environment", () -> {
            Allure.addAttachment("Description", "text/plain",
                    "Starting a test server to simulate external services that the main application depends on");
            WireMockConfiguration config = WireMockConfiguration.options()
                    .port(8888)
                    .asynchronousResponseEnabled(true)
                    .stubRequestLoggingDisabled(true);

            wireMockServer = new WireMockServer(config);
            wireMockServer.start();

            wireMockClient = new WireMock("localhost", 8888);
            WireMock.configureFor("localhost", 8888);

            Allure.addAttachment("Result", "text/plain",
                    "Test server started successfully on port 8888\n" +
                            "The application can now communicate with simulated external services for testing purposes");
        });
    }

    @AfterAll
    static void stopWireMock() {
        Allure.step("Cleaning up test environment", () -> {
            if (wireMockServer != null) {
                wireMockServer.stop();
                Allure.addAttachment("Result", "text/plain",
                        "Test server stopped successfully");
            }
        });
    }

    public void setupExternalAuthSuccess() {
        Allure.step("Configure successful authentication response", () -> {
            Allure.addAttachment("Scenario", "text/plain",
                    "Simulating a situation where the external authentication service works correctly and returns a successful response");
            stubFor(post(urlEqualTo("/auth"))
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withHeader("Content-Type", "application/json")
                            .withBody("{\"status\":\"authenticated\"}")));

            Allure.addAttachment("Result", "text/plain",
                    "External authentication service configured to return: SUCCESS (200 OK)\n" +
                            "When the system sends authentication requests, it will receive successful responses");
        });
    }

    public void setupExternalActionSuccess() {
        Allure.step("Configure successful action execution response", () -> {
            Allure.addAttachment("Scenario", "text/plain",
                    "Simulating a situation where the external action service processes requests successfully");
            stubFor(post(urlEqualTo("/doAction"))
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withHeader("Content-Type", "application/json")
                            .withBody("{\"action\":\"completed\"}")));

            Allure.addAttachment("Result", "text/plain",
                    "External action service configured to return: SUCCESS (200 OK)\n" +
                            "When users perform actions, the external service will confirm successful completion");
        });
    }

    public void setupExternalServiceFailure(String endpoint) {
        Allure.step("Simulate external service failure", () -> {
            String serviceName = endpoint.equals("/auth") ? "Authentication Service" : "Action Service";

            Allure.addAttachment("Scenario", "text/plain",
                    "Simulating a situation where the " + serviceName + " is experiencing technical difficulties or is temporarily unavailable");
            stubFor(post(urlEqualTo(endpoint))
                    .willReturn(aResponse()
                            .withStatus(500)
                            .withHeader("Content-Type", "application/json")
                            .withBody("{\"error\":\"Internal Server Error\"}")));

            Allure.addAttachment("Result", "text/plain",
                    serviceName + " configured to return: FAILURE (500 Internal Server Error)\n" +
                            "This tests how the system handles situations when external services are down");
        });
    }

    public void setupExternalServiceTimeout(String endpoint) {
        Allure.step("Simulate slow external service response", () -> {
            String serviceName = endpoint.equals("/auth") ? "Authentication Service" : "Action Service";

            Allure.addAttachment("Scenario", "text/plain",
                    "Simulating a situation where the " + serviceName + " responds very slowly (5 seconds delay)");
            stubFor(post(urlEqualTo(endpoint))
                    .willReturn(aResponse()
                            .withFixedDelay(5000)
                            .withStatus(200)));

            Allure.addAttachment("Result", "text/plain",
                    serviceName + " configured with: 5-second response delay\n" +
                            "This tests how the system handles slow external service responses");
        });
    }

    public void verifyExternalAuthCalled(String token) {
        Allure.step("Verify authentication service interaction", () -> {
            Allure.addAttachment("Expected Behavior", "text/plain",
                    "The system should contact the external authentication service when processing login requests");
            try {
                verify(postRequestedFor(urlEqualTo("/auth"))
                        .withRequestBody(containing("token=" + token)));

                Allure.addAttachment("Verification Result", "text/plain",
                        "SUCCESS: Authentication service was called as expected\n" +
                                "Token was included in the request\n" +
                                "The system properly communicated with the external service");

            } catch (Exception e) {
                Allure.addAttachment("Verification Result", "text/plain",
                        "FAILURE: Authentication service was NOT called\n" +
                                "Actual: No call detected\n" +
                                "This indicates a problem in the authentication flow");

                throw new AssertionError("Authentication service was not called. Expected call to /auth with token");
            }
        });
    }

    public void verifyExternalActionCalled(String token) {
        Allure.step("Check call to external action service", () -> {
            verify(postRequestedFor(urlEqualTo("/doAction"))
                    .withRequestBody(containing("token=" + token)));
        });
    }
}
