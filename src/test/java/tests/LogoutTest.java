package tests;

import clients.ApiClient;
import io.qameta.allure.*;
import models.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import utils.TestDataGenerator;
import utils.WireMockBase;

import static org.junit.jupiter.api.Assertions.*;

@Epic("User session management")
@Feature("Ending a session")
@DisplayName("Testing the logout process")
public class LogoutTest extends WireMockBase {
    private ApiClient apiClient;

    @BeforeEach
    void setup() {
        Allure.step("Initializing the test environment", () -> {
            apiClient = new ApiClient();
            setupExternalAuthSuccess();
        });
    }

    @Test
    @Severity(SeverityLevel.BLOCKER)
    @DisplayName("Successful logout after login")
    @Story("The user can gracefully end the session")
    @Description("Checks that the system is logged out correctly after authorization")
    void shouldReturnOkWhenLogoutAfterLogin() {
        String token = TestDataGenerator.generateValidToken();

        Allure.step("1. Preparing test data", () -> {
            Allure.addAttachment("Correct token", "text/plain", "The token used for the test is: " + token);
        });

        Allure.step("2. User authorization", () -> {
            Response loginResponse = apiClient.executeRequest(token, ApiClient.ACTION_LOGIN);

            Allure.addAttachment("Login Request", "text/plain",
                    "Initial authorization to create session\n" +
                            "Token: " + token + "\n" +
                            "Action: LOGIN\n" +
                            "Expected outcome: Create active user session\n" +
                            "Next step: Test logout functionality");

            assertTrue(loginResponse.isSuccess(), "Authorization should be successful\nError message: " + loginResponse.getMessage());

            Allure.addAttachment("Login Response", "text/plain",
                    "Login successful\n" +
                            "User authenticated\n" +
                            "Active session created\n" +
                            "Session ready for logout test\n" +
                            "Token validated: " + token);
        });

        Allure.step("3. User logout: " + token, () -> {
            Response logoutResponse = apiClient.executeRequest(token, ApiClient.ACTION_LOGOUT);

            Allure.addAttachment("Logout Request", "text/plain",
                    "Session termination request\n" +
                            "Token: " + token + "\n" +
                            "Action: LOGOUT\n" +
                            "Session status: Active (user is logged in)\n" +
                            "Expected outcome: Graceful session termination");

            assertAll("4. Verifying successful exit",
                    () -> assertEquals("OK", logoutResponse.getResult(),
                            "Logout should be successful\nError message: " + logoutResponse.getMessage()),
                    () -> assertNull(logoutResponse.getMessage(),
                            "On successful exit, the \"message\" field should be missing")
            );

            Allure.addAttachment("Logout Response", "text/plain",
                    "Logout successful\n" +
                            "System returned result: OK\n" +
                            "User session ended gracefully\n" +
                            "No error messages - as expected for successful logout\n" +
                            "Session status: Terminated\n" +
                            "User securely logged out of system");
        });
    }

    @Test
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Error when performing logout without login")
    @Story("System prevents logout without prior authorization")
    @Description("Checks that the user cannot log out and clear data without prior authorization")
    void shouldReturnErrorWhenLogoutWithoutLogin() {
        String token = TestDataGenerator.generateValidToken();

        Allure.step("1. Preparing test data", () -> {
            Allure.addAttachment("Correct token", "text/plain", "The token used for the test is: " + token);
        });

        Allure.step("2. Perform LOGOUT without prior authorization", () -> {
            Response response = apiClient.executeRequest(token, ApiClient.ACTION_LOGOUT);

            Allure.addAttachment("Logout Request", "text/plain",
                    "Logout attempt WITHOUT prior login\n" +
                            "Token: " + token + "\n" +
                            "Action: LOGOUT\n" +
                            "Session status: No active session (user never logged in)\n" +
                            "Expected outcome: Security rejection - cannot logout without login\n" +
                            "Reason: Logout requires an active session to terminate");

            Allure.step("Check error response", () -> {
                assertEquals("ERROR", response.getResult(),
                        "Should return ERROR when logout attempted without login");

                Allure.addAttachment("Logout Response", "text/plain",
                        "Logout failed - as expected (no active session)\n" +
                                "System returned result: ERROR\n" +
                                "Security check passed: Cannot logout without login\n" +
                                "Error message: " + (response.getMessage() != null ? response.getMessage() : "No active session") + "\n" +
                                "System prevents invalid logout attempts\n" +
                                "User cannot terminate non-existent session\n" +
                                "Session management security confirmed");
            });
        });
    }

    @Test
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Error when performing logout with invalid token")
    @Story("System rejects logout with invalid token")
    @Description("Checks that the system does not give the user the opportunity to perform a logout if the token does not match the required format")
    void shouldReturnErrorWhenLogoutWithInvalidToken() {
        String invalidToken = TestDataGenerator.generateInvalidCharsToken();

        Allure.step("1. Preparing test data", () -> {
            Allure.addAttachment("Invalid token", "text/plain", "The token used for the test is: " + invalidToken);
        });

        Allure.step("2. An attempt to perform an action with an invalid token: " + invalidToken, () -> {
            Response response = apiClient.executeRequest(invalidToken, ApiClient.ACTION_LOGOUT);

            Allure.addAttachment("Logout Request", "text/plain",
                    "Logout attempt with invalid token format\n" +
                            "Token: " + invalidToken + "\n" +
                            "Action: LOGOUT\n" +
                            "Token status: Invalid format (wrong characters)\n" +
                            "Expected outcome: Immediate format validation failure\n" +
                            "Reason: System validates token format before any session operations");

            assertAll("The processing of attempts to perform an action with an invalid token is checked.",
                    () -> assertEquals("ERROR", response.getResult(),
                            "Should return ERROR for logout with invalid token"),
                    () -> assertNotNull(response.getMessage(), "There should be a message about an invalid token"));

            Allure.addAttachment("Logout Response", "text/plain",
                    "Logout failed - as expected (invalid token format)\n" +
                            "System returned result: ERROR\n" +
                            "Error message provided: " + response.getMessage() + "\n" +
                            "System correctly rejected invalid token format\n" +
                            "Token validation works for logout requests\n" +
                            "Security: Invalid tokens cannot be used for any operations\n" +
                            "Format enforcement consistent across all actions\n" +
                            "Input validation confirmed");
        });
    }

    @Test
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Logging out again")
    @Story("Repeated logout requests are processed correctly")
    @Description("It is checked that the system does not allow you to log out again if it has already been done previously")
    void repeatedLogoutRequests() {
        String token = TestDataGenerator.generateValidToken();

        Allure.step("1. Preparing test data", () -> {
            Allure.addAttachment("Correct token", "text/plain", "The token used for the test is: " + token);
        });

        Allure.step("2. User authorization", () -> {
            Response loginResponse = apiClient.executeRequest(token, ApiClient.ACTION_LOGIN);

            Allure.addAttachment("Login Request", "text/plain",
                    "Initial login for repeated logout test\n" +
                            "Token: " + token + "\n" +
                            "Action: LOGIN\n" +
                            "Expected outcome: Create session for logout testing");

            assertTrue(loginResponse.isSuccess(), "Login should be successful\nError message: " + loginResponse.getMessage());

            Allure.addAttachment("Login Response", "text/plain",
                    "Login successful\n" +
                            "Session created\n" +
                            "Ready to test repeated logout behavior\n" +
                            "Token active: " + token);
        });

        Allure.step("3. Performing first logout", () -> {
            Response logout1 = apiClient.executeRequest(token, ApiClient.ACTION_LOGOUT);

            Allure.addAttachment("First Logout Request", "text/plain",
                    "First logout attempt (normal flow)\n" +
                            "Token: " + token + "\n" +
                            "Action: LOGOUT\n" +
                            "Session status: Active\n" +
                            "Expected outcome: Successful session termination");

            assertTrue(logout1.isSuccess(), "The first exit must be successful\nError message: " + logout1.getMessage());

            Allure.addAttachment("First Logout Response", "text/plain",
                    "First logout successful\n" +
                            "System returned result: OK\n" +
                            "Session terminated\n" +
                            "Token should now be invalid\n" +
                            "Ready to test second logout attempt");
        });

        Allure.step("4. Logging out again", () -> {
            Response logout2 = apiClient.executeRequest(token, ApiClient.ACTION_LOGOUT);

            Allure.addAttachment("Second Logout Request", "text/plain",
                    "Second logout attempt (after session already ended)\n" +
                            "Token: " + token + "\n" +
                            "Action: LOGOUT\n" +
                            "Session status: Already terminated (from first logout)\n" +
                            "Expected outcome: Error - cannot logout twice\n" +
                            "Reason: Session no longer exists to terminate");

            assertAll("Verifying that an error is returned when logging out again",
                    () -> assertEquals("ERROR", logout2.getResult(),
                            "Should return an error when logging out again"),
                    () -> assertNotNull(logout2.getMessage(), "There should be an error message"));

            Allure.addAttachment("Second Logout Response", "text/plain",
                    "Second logout failed - as expected\n" +
                            "System returned result: ERROR\n" +
                            "Error message provided: " + logout2.getMessage() + "\n" +
                            "System correctly prevents repeated logout\n" +
                            "Security: Cannot logout non-existent session\n" +
                            "Session state management confirmed\n" +
                            "Token status after logout: Invalid/expired\n" +
                            "System maintains consistent session state");

        });
    }
}
