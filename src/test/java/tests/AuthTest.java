package tests;

import clients.ApiClient;
import io.qameta.allure.*;
import models.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import utils.ResponseFormatter;
import utils.TestDataGenerator;
import utils.WireMockBase;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Epic("User session management")
@Feature("Authorization")
public class AuthTest extends WireMockBase {
    @Test
    @DisplayName("Successful authorization with a valid token")
    @Story("The user can login with correct data")
    void shouldReturnOkWhenLoginWithValidToken() {
        String token = TestDataGenerator.generateValidToken();
        Allure.step("A valid token has been generated: " + token, () -> {
            Allure.addAttachment("Token", "text/plain", token);
        });
        Response response = Allure.step("Sending a LOGIN request with a valid token", () -> apiClient.sendRequest(token, ApiClient.ACTION_LOGIN)
        );

        Allure.step("Checking a successful response", () -> {
            Allure.addAttachment("Server response", "application/json", ResponseFormatter.formatResponse(response));
            assertEquals("OK", response.getResult(), "The result is expected to be 'OK' upon successful authorization");
        });
    }

    @Test
    @DisplayName("Error during authorization with incorrect token length")
    @Story("User cannot login with incorrect token length")
    void shouldReturnErrorWhenTokenHasInvalidLength() {
        String token = TestDataGenerator.generateInvalidLengthToken();
        Allure.step("A token of incorrect length was generated: " + token, () -> {
            Allure.addAttachment("Token", "text/plain", token);
        });
        Response response = Allure.step("Sending a LOGIN request with an incorrect token length", () -> apiClient.sendRequest(token, ApiClient.ACTION_LOGIN)
        );

        Allure.step("Checking to see if an error is returned", () -> {
            Allure.addAttachment("Server response", "application/json", ResponseFormatter.formatResponse(response));
            assertEquals("ERROR", response.getResult(), "The result is expected to be 'ERROR' if authorization fails");
        });
    }

    @Test
    @DisplayName("Error during authorization with a token containing invalid characters")
    @Story("User cannot login with a token containing invalid characters")
    void shouldReturnErrorWhenTokenHasInvalidCharacters() {
        String token = TestDataGenerator.generateInvalidCharsToken();
        Allure.step("A token with invalid characters was generated: " + token, () -> {
            Allure.addAttachment("Token", "text/plain", token);
        });
        Response response = Allure.step("Sending a LOGIN request with a token containing invalid characters", () -> apiClient.sendRequest(token, ApiClient.ACTION_LOGIN)
        );

        Allure.step("Checking to see if an error is returned", () -> {
            Allure.addAttachment("Server response", "application/json", ResponseFormatter.formatResponse(response));
            assertEquals("ERROR", response.getResult(), "The result is expected to be 'ERROR' if authorization fails");
        });
    }

    @Test
    @DisplayName("Error during authorization with an empty token")
    @Story("User cannot login with an empty token")
    void shouldReturnErrorWhenLoginWithEmptyToken() {
        Response response = Allure.step("Sending a LOGIN request with an empty token", () -> apiClient.sendRequest("", ApiClient.ACTION_LOGIN)
        );

        Allure.step("Checking to see if an error is returned", () -> {
            Allure.addAttachment("Server response", "application/json", ResponseFormatter.formatResponse(response));
            assertEquals("ERROR", response.getResult(), "The result is expected to be 'ERROR' if authorization fails");
        });
    }

    @Test
    @DisplayName("Error when the external authorization service is unavailable")
    @Story("System handles external service failures gracefully")
    void shouldReturnErrorWhenExternalAuthServiceFails() {
        String token = TestDataGenerator.generateValidToken();

        Allure.step("Simulate external service failure", () -> {
            wireMockServer.stubFor(post(urlEqualTo("/auth"))
                    .willReturn(aResponse().withStatus(500)));
        });

        Allure.step("Perform LOGIN with failing external service", () -> {
            Response response = apiClient.sendRequest(token, ApiClient.ACTION_LOGIN);

            Allure.step("Check error response", () -> {
                Allure.addAttachment("Error response", "application/json",
                        ResponseFormatter.formatResponse(response));
                assertEquals("ERROR", response.getResult(),
                        "Should return ERROR when external service fails");
            });

            Allure.addAttachment("Error Response", "application/json",
                    ResponseFormatter.formatResponse(response));
                        assertEquals("ERROR", response.getResult(),
                    "If an external service fails, ERROR should be returned");
        });
    }
}
