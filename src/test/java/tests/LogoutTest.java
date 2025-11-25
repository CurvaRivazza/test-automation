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

import static org.junit.jupiter.api.Assertions.assertEquals;

@Epic("User session management")
@Feature("Logout")
public class LogoutTest extends WireMockBase {
    @Test
    @DisplayName("Successful logout after login")
    @Story("User can perform logout after successful authorization")
    void shouldReturnOkWhenLogoutAfterLogin() {
        String token = TestDataGenerator.generateValidToken();

        Allure.step("Perform successful login with token: " + token, () -> {
            Response loginResponse = apiClient.sendRequest(token, ApiClient.ACTION_LOGIN);
            Allure.addAttachment("Login response", "application/json",
                    ResponseFormatter.formatResponse(loginResponse));
            assertEquals("OK", loginResponse.getResult(), "Login should be successful before action");
        });

        Allure.step("Perform LOGOUT with logged-in token: " + token, () -> {
            Response response = apiClient.sendRequest(token, ApiClient.ACTION_LOGOUT);

            Allure.step("Check successful logout response", () -> {
                Allure.addAttachment("Logout response", "application/json",
                        ResponseFormatter.formatResponse(response));
                assertEquals("OK", response.getResult(),
                        "Logout should be successful after login");
            });
        });

        Allure.step("Verify token is removed after logout", () -> {
            Response actionAfterLogout = apiClient.sendRequest(token, ApiClient.ACTION_ACTION);
            assertEquals("ERROR", actionAfterLogout.getResult(),
                    "Actions should be unavailable after logout");
        });
    }

    @Test
    @DisplayName("Error when performing logout without login")
    @Story("System prevents logout without prior authorization")
    void shouldReturnErrorWhenLogoutWithoutLogin() {
        String token = TestDataGenerator.generateValidToken();

        Allure.step("Perform LOGOUT without prior LOGIN", () -> {
            Response response = apiClient.sendRequest(token, ApiClient.ACTION_LOGOUT);

            Allure.step("Check error response", () -> {
                Allure.addAttachment("Error response", "application/json",
                        ResponseFormatter.formatResponse(response));
                assertEquals("ERROR", response.getResult(),
                        "Should return ERROR when logout attempted without login");
            });
        });
    }

    @Test
    @DisplayName("Error when performing logout with invalid token")
    @Story("System rejects logout with invalid token")
    void shouldReturnErrorWhenLogoutWithInvalidToken() {
        String invalidToken = TestDataGenerator.generateInvalidCharsToken();

        Allure.step("Perform LOGOUT with invalid token: " + invalidToken, () -> {
            Allure.addAttachment("Invalid Token", "text/plain", invalidToken);

            Response response = apiClient.sendRequest(invalidToken, ApiClient.ACTION_LOGOUT);

            Allure.step("Check error response", () -> {
                Allure.addAttachment("Error response", "application/json",
                        ResponseFormatter.formatResponse(response));
                assertEquals("ERROR", response.getResult(),
                        "Should return ERROR for logout with invalid token");
            });
        });
    }
}
