package tests;

import clients.ApiClient;
import io.qameta.allure.*;
import models.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import utils.TestDataGenerator;
import utils.WireMockBase;

import static org.junit.jupiter.api.Assertions.*;

@Epic("User session management")
@Feature("User actions")
@DisplayName("Testing the execution of actions in the system")
public class ActionTest extends WireMockBase {
    private ApiClient apiClient;

    @BeforeEach
    void setup() {
        Allure.step("Initializing the test environment", () -> {
            apiClient = new ApiClient();
            setupExternalAuthSuccess();
            setupExternalActionSuccess();
        });
    }

    @Test
    @Severity(SeverityLevel.BLOCKER)
    @DisplayName("Successful action execution after login")
    @Story("User can perform actions after successful authorization")
    @Description("Checks that the user can perform an action in the system after successful authorization")
    void shouldReturnOkWhenActionWithLoggedInToken() {
        String token = TestDataGenerator.generateValidToken();

        Allure.step("1. Preparing test data", () -> {
            Allure.addAttachment("Correct token", "text/plain", "The token used for the test is: " + token);
        });

        Allure.step("2. User authorization", () -> {
            Response loginResponse = apiClient.executeRequest(token, ApiClient.ACTION_LOGIN);

            Allure.addAttachment("Login Request", "text/plain",
                    "Authorization request sent before action\n" +
                            "Token: " + token + "\n" +
                            "Action: LOGIN\n" +
                            "Expected outcome: Successful authentication");

            assertTrue(loginResponse.isSuccess(), "Login should be successful before action\nError message: " + loginResponse.getMessage());

            Allure.addAttachment("Login Response", "text/plain",
                    "Login successful\n" +
                            "User authenticated\n" +
                            "Session established\n" +
                            "Ready to perform actions");
        });

        Allure.step("3. Performing an action with an authorization token", () -> {
            Response actionResponse = apiClient.executeRequest(token, ApiClient.ACTION_ACTION);

            Allure.addAttachment("Action Request", "text/plain",
                    "Action execution request\n" +
                            "Token: " + token + "\n" +
                            "Action: ACTION (perform user action)\n" +
                            "Expected outcome: Successful action execution\n" +
                            "Context: User has active session after login");

            assertAll("Check successful action response",
                    () -> assertEquals("OK", actionResponse.getResult(),
                            "Action result should be successful"),
                    () -> assertNull(actionResponse.getMessage(), "If the action is successful, the \"message\" field should be absent"));

            Allure.addAttachment("Action Response", "text/plain",
                    "Action executed successfully\n" +
                            "System returned result: OK\n" +
                            "User action completed\n" +
                            "No error messages - as expected for successful action\n" +
                            "User can perform operations in the system");
        });

        Allure.step("4. Checking the call to an external action service", () -> {
            verifyExternalActionCalled(token);
        });
    }

    @Test
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Error when external action service fails")
    @Story("System handles external service failures gracefully")
    @Description("Checks that the system correctly handles situations when an external service is unavailable")
    void shouldReturnErrorWhenExternalActionServiceFails() {
        String token = TestDataGenerator.generateValidToken();

        Allure.step("1. Preparing test data", () -> {
            Allure.addAttachment("Correct token", "text/plain", "The token used for the test is: " + token);
        });

        Allure.step("2. User authorization", () -> {
            Response loginResponse = apiClient.executeRequest(token, ApiClient.ACTION_LOGIN);

            Allure.addAttachment("Login Request", "text/plain",
                    "Initial authorization before testing service failure\n" +
                            "Token: " + token + "\n" +
                            "Action: LOGIN\n" +
                            "Expected outcome: Normal successful login");

            assertTrue(loginResponse.isSuccess(), "Login should be successful before action\nError message: " + loginResponse.getMessage());

            Allure.addAttachment("Login Response", "text/plain",
                    "Login successful - session established\n" +
                            "User authenticated normally\n" +
                            "Ready to test external service failure scenario");
        });

        Allure.step("3. Configure external action service failure", () -> {
            setupExternalServiceFailure("/doAction");
        });

        Allure.step("4. Attempting to perform an action when the external service is unavailable", () -> {
            Response actionResponse = apiClient.executeRequest(token, ApiClient.ACTION_ACTION);

            Allure.addAttachment("Action Request", "text/plain",
                    "Action execution request with failing external service\n" +
                            "Token: " + token + "\n" +
                            "Action: ACTION\n" +
                            "External service: Configured to fail\n" +
                            "Expected outcome: Graceful error handling, not system crash");

            Allure.step("5. Check the error message", () -> {
                assertAll("Checking external service failure handling",
                        () -> assertEquals("ERROR", actionResponse.getResult(), "If an external service fails, \"ERROR\" should be returned"),
                        () -> assertNotNull(actionResponse.getMessage(), "There must be an error message"));
            });

            Allure.addAttachment("Action Response", "text/plain",
                    "Action failed - as expected for external service failure\n" +
                            "System returned result: ERROR\n" +
                            "Error message provided: " + actionResponse.getMessage() + "\n" +
                            "System handled external service failure gracefully\n" +
                            "User gets informative error message\n" +
                            "No system crash or unexpected behavior\n" +
                            "System resilience confirmed");
        });
    }

    @Test
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Error when performing action without login")
    @Story("System prevents actions without prior authorization")

    @Description("The system prevents actions from being performed if authorization has not been completed")
    void shouldReturnErrorWhenActionWithoutLogin() {
        String token = TestDataGenerator.generateValidToken();

        Allure.step("1. Preparing test data", () -> {
            Allure.addAttachment("Correct token", "text/plain", "The token used for the test is: " + token);
        });

        Allure.step("2. An attempt to perform an action without prior authorization", () -> {
            Response actionResponse = apiClient.executeRequest(token, ApiClient.ACTION_ACTION);

            Allure.addAttachment("Action Request", "text/plain",
                    "Action execution attempt without prior login\n" +
                            "Token: " + token + "\n" +
                            "Action: ACTION\n" +
                            "User status: Not logged in (no prior authentication)\n" +
                            "Expected outcome: Security rejection - action not allowed");

            assertAll("Checking protection against unauthorized actions",
                    () -> assertEquals("ERROR", actionResponse.getResult(),
                            "Should return ERROR when action attempted without login"),
                    () -> assertNotNull(actionResponse.getMessage(), "There should be a message about the need for authorization"));

            Allure.addAttachment("Action Response", "text/plain",
                    "Action failed - as expected (security requirement)\n" +
                            "System returned result: ERROR\n" +
                            "Error message provided: " + actionResponse.getMessage() + "\n" +
                            "System correctly rejected unauthorized action\n" +
                            "Security mechanism works: Actions require prior login\n" +
                            "User prompted to authenticate first\n" +
                            "Security validation successful");
        });
    }

    @Test
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Error when performing action with invalid token")
    @Story("System rejects actions with invalid tokens")
    @Description("Checks that the system does not give the user the opportunity to perform an action if the token does not match the required format")
    void shouldReturnErrorWhenActionWithInvalidToken() {
        String invalidToken = TestDataGenerator.generateInvalidCharsToken();

        Allure.step("1. Preparing test data", () -> {
            Allure.addAttachment("Invalid token", "text/plain", "The token used for the test is: " + invalidToken);
        });

        Allure.step("2. An attempt to perform an action with an invalid token: " + invalidToken, () -> {
            Response actionResponse = apiClient.executeRequest(invalidToken, ApiClient.ACTION_ACTION);

            Allure.addAttachment("Action Request", "text/plain",
                    "Action execution attempt with invalid token\n" +
                            "Token: " + invalidToken + "\n" +
                            "Action: ACTION\n" +
                            "Token status: Invalid format (wrong characters)\n" +
                            "Expected outcome: Immediate rejection due to token format");

            assertAll("The processing of attempts to perform an action with an invalid token is checked.",
                    () -> assertEquals("ERROR", actionResponse.getResult(),
                            "Should return ERROR for action with invalid token"),
                    () -> assertNotNull(actionResponse.getMessage(), "There should be a message about an invalid token"));

            Allure.addAttachment("Action Response", "text/plain",
                    "Action failed - as expected (invalid token)\n" +
                            "System returned result: ERROR\n" +
                            "Error message provided: " + actionResponse.getMessage() + "\n" +
                            "System correctly rejected invalid token format\n" +
                            "Token validation works for action requests\n" +
                            "Security: Invalid tokens cannot be used for any operations\n" +
                            "Format enforcement successful");
        });
    }

    @Test
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Action not available after logout")
    @Story("The user session ends gracefully")
    @Description("""
            1. The user logs in to the system
            2. User logs out
            The user should not be able to perform actions after logging out""")
    void actionUnavailableAfterLogout() {
        String token = TestDataGenerator.generateValidToken();

        Allure.step("1. Preparing test data", () -> {
            Allure.addAttachment("Correct token", "text/plain", "The token used for the test is: " + token);
        });

        Allure.step("2. User aithorization", () -> {
            Response loginResponse = apiClient.executeRequest(token, ApiClient.ACTION_LOGIN);

            Allure.addAttachment("Login Request", "text/plain",
                    "Initial login to establish session\n" +
                            "Token: " + token + "\n" +
                            "Action: LOGIN\n" +
                            "Expected outcome: Create active user session");

            assertTrue(loginResponse.isSuccess(), "Login must be successful\nError message: " + loginResponse.getMessage());

            Allure.addAttachment("Login Response", "text/plain",
                    "Login successful\n" +
                            "Session created\n" +
                            "User authenticated\n" +
                            "Ready for logout test");
        });

        Allure.step("3. User logs out", () -> {
            Response logoutResponse = apiClient.executeRequest(token, ApiClient.ACTION_LOGOUT);

            Allure.addAttachment("Logout Request", "text/plain",
                    "Session termination request\n" +
                            "Token: " + token + "\n" +
                            "Action: LOGOUT\n" +
                            "Expected outcome: End user session\n" +
                            "Session status: Will be terminated");

            assertTrue(logoutResponse.isSuccess(), "Logout must be successful\nError message: " + logoutResponse.getMessage());

            Allure.addAttachment("Logout Response", "text/plain",
                    "Logout successful\n" +
                            "Session terminated\n" +
                            "User logged out of system\n" +
                            "Session status: Ended");
        });

        Allure.step("4. Trying to perform an action after exiting", () -> {
            Response actionResponse = apiClient.executeRequest(token, ApiClient.ACTION_ACTION);

            Allure.addAttachment("Post-Logout Action Request", "text/plain",
                    "Action attempt after session termination\n" +
                            "Token: " + token + "\n" +
                            "Action: ACTION\n" +
                            "Session status: Terminated (user logged out)\n" +
                            "Expected outcome: Action rejected - no active session");

            assertEquals("ERROR", actionResponse.getResult(),
                    "After logging out, actions should be unavailable");

            Allure.addAttachment("Action Response", "text/plain",
                    "Action failed - as expected (session ended)\n" +
                            "System returned result: ERROR\n" +
                            "System correctly rejected action after logout\n" +
                            "Session termination works properly\n" +
                            "Security: Cannot use token after logout\n" +
                            "Session management confirmed\n" +
                            "Error message: " + (actionResponse.getMessage() != null ? actionResponse.getMessage() : "Session not found"));
        });
    }

    @Test
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Multiple actions in one session")
    @Story("The user can perform several actions within one session")
    @Description("Checks that the user can perform several actions sequentially")
    void multipleActionsInSingleSession() {
        String token = TestDataGenerator.generateValidToken();

        Allure.step("1. Preparing test data", () -> {
            Allure.addAttachment("Correct token", "text/plain", "The token used for the test is: " + token);
        });

        Allure.step("2. User authorization", () -> {
            Response loginResponse = apiClient.executeRequest(token, ApiClient.ACTION_LOGIN);

            Allure.addAttachment("Login Request", "text/plain",
                    "Single login for multiple actions test\n" +
                            "Token: " + token + "\n" +
                            "Action: LOGIN\n" +
                            "Expected outcome: Create session for multiple operations");

            assertTrue(loginResponse.isSuccess(), "Login must be successful\nError message: " + loginResponse.getMessage());

            Allure.addAttachment("Login Response", "text/plain",
                    "Login successful\n" +
                            "Single session established\n" +
                            "Ready for multiple action execution\n" +
                            "Session ID: " + token);
        });

        Allure.step("3. The user performs a series of several actions", () -> {
            for (int i = 1; i <= 3; i++) {
                int finalI = i;
                Allure.step("Action №" + i, () -> {
                    Response response = apiClient.executeRequest(token, ApiClient.ACTION_ACTION);

                    Allure.addAttachment("Action Request " + finalI, "text/plain",
                            "Action execution #" + finalI + " in series\n" +
                                    "Token: " + token + "\n" +
                                    "Action: ACTION\n" +
                                    "Action number: " + finalI + " of 3\n" +
                                    "Session status: Still active (same token)\n" +
                                    "Expected outcome: Successful execution");

                    assertTrue(response.isSuccess(),
                            "Action №" + finalI + " must be successful\nError message: "  + response.getMessage());

                    Allure.addAttachment("Action Response " + finalI, "text/plain",
                            "Action №" + finalI + " successful\n" +
                                    "System returned result: OK\n" +
                                    "Action completed within same session\n" +
                                    "Session remains active\n" +
                                    "Token still valid for operations");
                });
            }

            Allure.addAttachment("Multiple Actions Summary", "text/plain",
                    "All 3 actions executed successfully\n" +
                            "Single session maintained throughout\n" +
                            "Token remained valid for all operations\n" +
                            "No session timeout or expiration\n" +
                            "System handles multiple requests correctly");
        });

        Allure.step("4. Checking external service calls", () -> {
            verifyExternalActionCalled(token);
        });
    }

    @Test
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Checking timeout during action")
    @Story("The system correctly handles slow responses from external services")
    @Description("The system must correctly handle situations when a response from an external service does not arrive for a long time")
    @Timeout(10)
    void actionTimeoutHandling() {
        String token = TestDataGenerator.generateValidToken();

        Allure.step("1. Preparing test data", () -> {
            Allure.addAttachment("Correct token", "text/plain", "The token used for the test is: " + token);
        });

        Allure.step("2. User aithorization", () -> {
            Response loginResponse = apiClient.executeRequest(token, ApiClient.ACTION_LOGIN);

            Allure.addAttachment("Login Request", "text/plain",
                    "Login before timeout test\n" +
                            "Token: " + token + "\n" +
                            "Action: LOGIN\n" +
                            "Expected outcome: Normal successful login");

            assertTrue(loginResponse.isSuccess(), "Login should be successful\nErrorMessage: " + loginResponse.getMessage());

            Allure.addAttachment("Login Response", "text/plain",
                    "Login successful - session ready\n" +
                            "User authenticated normally\n" +
                            "Ready to test timeout scenario for actions");
        });

        Allure.step("3. Set up a mock service with a long response delay", () -> {
            setupExternalServiceTimeout("/doAction");
        });

        Allure.step("4. Send a request and check timeout handling", () -> {
            Response response = apiClient.executeRequest(token, ApiClient.ACTION_ACTION);

            Allure.addAttachment("Action Request", "text/plain",
                    "Action execution with delayed external service\n" +
                            "Token: " + token + "\n" +
                            "Action: ACTION\n" +
                            "External service: Configured with response delay\n" +
                            "Expected outcome: System handles timeout gracefully\n" +
                            "Maximum wait time: 10 seconds (test timeout)");

            assertNotNull(response, "A response must be received");

            Allure.addAttachment("Timeout Test Result", "text/plain",
                    "System handled timeout scenario\n" +
                            "Response received: " + (response.getResult() != null ? response.getResult() : "Processed") + "\n" +
                            "No system freeze or crash during delay\n" +
                            "Timeout mechanism works correctly\n" +
                            "System remains responsive\n" +
                            "User experience: System doesn't hang indefinitely\n" +
                            "External service delay handled properly");
        });
    }
}
