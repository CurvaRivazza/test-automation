package tests;

import clients.ApiClient;
import io.qameta.allure.Allure;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import models.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import utils.ResponseFormatter;
import utils.TestDataGenerator;
import utils.WireMockBase;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Epic("User session management")
@Feature("Actions")
public class ActionTest extends WireMockBase {
    @Test
    @DisplayName("Successful action execution after login")
    @Story("User can perform actions after successful authorization")
    void shouldReturnOkWhenActionWithLoggedInToken() {
        String token = TestDataGenerator.generateValidToken();

        Allure.step("Perform successful login with token: " + token, () -> {
            Response loginResponse = apiClient.sendRequest(token, ApiClient.ACTION_LOGIN);
            Allure.addAttachment("Login response", "application/json",
                    ResponseFormatter.formatResponse(loginResponse));
            assertEquals("OK", loginResponse.getResult(), "Login should be successful before action");
        });

        Allure.step("Perform ACTION with logged-in token: " + token, () -> {
            Response actionResponse = apiClient.sendRequest(token, ApiClient.ACTION_ACTION);

            Allure.step("Check successful action response", () -> {
                Allure.addAttachment("Action response", "application/json",
                        ResponseFormatter.formatResponse(actionResponse));
                assertEquals("OK", actionResponse.getResult(),
                        "Action should be successful after login");
            });
        });
    }

    @Test
    @DisplayName("Error when external action service fails")
    @Story("System handles external service failures gracefully")
    void shouldReturnErrorWhenExternalActionServiceFails() {
        String token = TestDataGenerator.generateValidToken();

        Allure.step("Perform successful login with token: " + token, () -> {
            Response loginResponse = apiClient.sendRequest(token, ApiClient.ACTION_LOGIN);
            Allure.addAttachment("Login response", "application/json",
                    ResponseFormatter.formatResponse(loginResponse));
            assertEquals("OK", loginResponse.getResult());
        });

        Allure.step("Simulate external service failure", () -> {
            wireMockServer.stubFor(post(urlEqualTo("/doAction"))
                    .willReturn(aResponse().withStatus(500)));
        });

        Allure.step("Perform ACTION with failing external service", () -> {
            Response actionResponse = apiClient.sendRequest(token, ApiClient.ACTION_ACTION);

            Allure.step("Check error response", () -> {
                Allure.addAttachment("Error response", "application/json",
                        ResponseFormatter.formatResponse(actionResponse));
                assertEquals("ERROR", actionResponse.getResult(),
                        "Should return ERROR when external service fails");
            });
        });
    }

    @Test
    @DisplayName("Error when performing action without login")
    @Story("System prevents actions without prior authorization")
    void shouldReturnErrorWhenActionWithoutLogin() {
        String token = TestDataGenerator.generateValidToken();

        Allure.step("Perform ACTION without prior LOGIN", () -> {
            Response actionResponse = apiClient.sendRequest(token, ApiClient.ACTION_ACTION);

            Allure.step("Check error response", () -> {
                Allure.addAttachment("Error response", "application/json",
                        ResponseFormatter.formatResponse(actionResponse));
                assertEquals("ERROR", actionResponse.getResult(),
                        "Should return ERROR when action attempted without login");
            });
        });
    }

    @Test
    @DisplayName("Error when performing action with invalid token")
    @Story("System rejects actions with invalid tokens")
    void shouldReturnErrorWhenActionWithInvalidToken() {
        String invalidToken = TestDataGenerator.generateInvalidCharsToken();

        Allure.step("Perform ACTION with invalid token: " + invalidToken, () -> {
            Allure.addAttachment("Invalid Token", "text/plain", invalidToken);

            Response actionResponse = apiClient.sendRequest(invalidToken, ApiClient.ACTION_ACTION);

            Allure.step("Check error response", () -> {
                Allure.addAttachment("Error response", "application/json",
                        ResponseFormatter.formatResponse(actionResponse));
                assertEquals("ERROR", actionResponse.getResult(),
                        "Should return ERROR for action with invalid token");
            });
        });
    }
}
