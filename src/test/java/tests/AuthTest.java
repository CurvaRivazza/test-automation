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
@Feature("User authorization")
@DisplayName("Testing the login process")
public class AuthTest extends WireMockBase {
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
    @DisplayName("Successful authorization with a valid token")
    @Story("The user can login with correct data")
    @Description("""
            Checks that the system correctly processes a valid token:
                1. The token must contain 32 characters
                2. The token must contain only capital letters A-Z and numbers 0-9
                3. The external authorization service should respond successfully
            """)
    void shouldReturnOkWhenLoginWithValidToken() {
        String token = TestDataGenerator.generateValidToken();
        Allure.step("1. Preparing test data", () -> {
            Allure.addAttachment("Correct token", "text/plain", "The token used for the test is: " + token + "\n" +
                    "Token format: 32 characters, only uppercase letters A-Z and digits 0-9");
        });

        Allure.step("2. Sending an authorization request", () -> {
            Response response = apiClient.executeRequest(token, ApiClient.ACTION_LOGIN);

            Allure.addAttachment("Request Details", "text/plain",
                    "Authorization request sent with token\n" +
                            "Token: " + token + "\n" +
                            "Expected outcome: Successful user login");

            Allure.step("3. Checking the system response", () -> {
                assertAll("Verifying successful authorization",
                        () -> assertEquals("OK", response.getResult(), "The result should be \"OK\"\nError message: " + response.getMessage()),
                        () -> assertNull(response.getMessage(), "If authorization is successful, the \"message\" field should be absent"));

                Allure.addAttachment("Response Details", "text/plain",
                        "Successful authorization\n" +
                                "System returned result: OK\n" +
                                "User has been successfully logged in\n" +
                                "No error messages - as expected for successful login");
            });
        });

        Allure.step("4. Checking the call to an external service", () -> {
            verifyExternalAuthCalled(token);
        });
    }

    @Test
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Error during authorization with incorrect token length")
    @Story("User cannot login with incorrect token length")
    @Description("Checks that the system correctly processes a token of incorrect length (correct length is 32 characters)")
    void shouldReturnErrorWhenTokenHasInvalidLength() {
        String token = TestDataGenerator.generateInvalidLengthToken();
        Allure.step("1. Preparing test data", () -> {
            Allure.addAttachment("Incorrect length token", "text/plain", "The token used for the test is: " + token);
        });

        Allure.step("2. Sending an authorization request", () -> {
            Response response = apiClient.executeRequest(token, ApiClient.ACTION_LOGIN);

            Allure.addAttachment("Request Details", "text/plain",
                    "Authorization request sent with invalid length token\n" +
                            "Token: " + token + "\n" +
                            "Token length: " + token.length() + " characters (should be 32)\n" +
                            "Expected outcome: Authorization failure with error message");

            Allure.step("3. Checking the system response", () -> {
                assertAll("Checking that authorization is unsuccessful",
                        () -> assertEquals("ERROR", response.getResult(), "The result should be \"ERROR\""),
                        () -> assertNotNull(response.getMessage(), "There should be a message indicating the cause of the error"));

                Allure.addAttachment("Response Details", "text/plain",
                        "Authorization failed - as expected\n" +
                                "System returned result: ERROR\n" +
                                "Error message provided: " + response.getMessage() + "\n" +
                                "System correctly rejected token with incorrect length");
            });
        });
    }

    @Test
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Error during authorization with a token containing invalid characters")
    @Story("User cannot login with a token containing invalid characters")
    @Description("Checks that the system correctly processes a token containing invalid characters (any characters except the letters A-Z and numbers 0-9, as well as any lowercase characters)")
    void shouldReturnErrorWhenTokenHasInvalidCharacters() {
        String token = TestDataGenerator.generateInvalidCharsToken();

        Allure.step("1. Preparing test data", () -> {
            Allure.addAttachment("Incorrect length token", "text/plain", "The token used for the test is: " + token);
        });

        Allure.step("2. Sending an authorization request", () -> {
            Response response = apiClient.executeRequest(token, ApiClient.ACTION_LOGIN);

            Allure.addAttachment("Request Details", "text/plain",
                    "Authorization request sent with token containing invalid characters\n" +
                            "Token: " + token + "\n" +
                            "Contains invalid characters (special symbols or lowercase letters)\n" +
                            "Expected outcome: Authorization failure with error message");

            Allure.step("3. Checking the system response", () -> {
                assertAll("Checking that authorization is unsuccessful",
                        () -> assertEquals("ERROR", response.getResult(), "The result should be \"ERROR\""),
                        () -> assertNotNull(response.getMessage(), "There should be a message indicating the cause of the error"));

                Allure.addAttachment("Response Details", "text/plain",
                        "Authorization failed - as expected\n" +
                                "System returned result: ERROR\n" +
                                "Error message provided: " + response.getMessage() + "\n" +
                                "System correctly rejected token with invalid characters\n" +
                                "Security check passed: Invalid format detection works correctly");
            });
        });
    }

    @Test
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Error during authorization with an empty token")
    @Story("User cannot login with an empty token")
    @Description("Checks that the system correctly processes an empty token (not containing any characters)")
    void shouldReturnErrorWhenLoginWithEmptyToken() {
        Allure.step("1. Sending an authorization request with an empty token", () -> {
            Response response = apiClient.executeRequest("", ApiClient.ACTION_LOGIN);

            Allure.addAttachment("Request Details", "text/plain",
                    "Authorization request sent with empty token\n" +
                            "Token: empty string - no characters\n" +
                            "Expected outcome: Authorization failure with error message\n" +
                            "Reason: Token cannot be empty");

            Allure.step("2. Checking the system response", () -> {
                assertAll("Checking that authorization is unsuccessful",
                        () -> assertEquals("ERROR", response.getResult(), "The result should be \"ERROR\""),
                        () -> assertNotNull(response.getMessage(), "There should be a message indicating the cause of the error"));

                Allure.addAttachment("Response Details", "text/plain",
                        "Authorization failed - as expected\n" +
                                "System returned result: ERROR\n" +
                                "Error message provided: " + response.getMessage() + "\n" +
                                "System correctly rejected empty token\n" +
                                "Security check passed: Empty input validation works correctly");
            });
        });
    }

    @Test
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Error when the external authorization service is unavailable")
    @Story("System handles external service failures gracefully")
    @Description("Checks that the system correctly handles situations when an external service is unavailable")
    void shouldReturnErrorWhenExternalAuthServiceFails() {
        String token = TestDataGenerator.generateValidToken();

        Allure.step("1. Configure external authorization service failure", () -> {
            setupExternalServiceFailure("/auth");
        });

        Allure.step("2. Send an authorization request", () -> {
            Response response = apiClient.executeRequest(token, ApiClient.ACTION_LOGIN);

            Allure.addAttachment("Request Details", "text/plain",
                    "Authorization request sent with valid token\n" +
                            "Token: " + token + "\n" +
                            "External service: Configured to fail\n" +
                            "Expected outcome: System should handle external service failure gracefully");

            Allure.step("3. Check the error message", () -> {
                assertAll("Checking external service failure handling",
                        () -> assertEquals("ERROR", response.getResult(), "If an external service fails, \"ERROR\" should be returned"),
                        () -> assertNotNull(response.getMessage(), "There must be an error message"));

                Allure.addAttachment("Response Details", "text/plain",
                        "Authorization failed - as expected\n" +
                                "System returned result: ERROR\n" +
                                "Error message provided: " + response.getMessage() + "\n" +
                                "System correctly handled external service failure\n" +
                                "User gets informative error message when service is unavailable\n" +
                                "No system crash or unexpected behavior");
            });
        });
    }

    @Test
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Re-authorization with the same token")
    @Story("The system correctly processes repeated authorization requests")
    @Description("""
            1. Creating the correct token
            2. Authorization with this token
            3. Re-authorization with the same token
            The system should process re-authorization correctly
            """)
    void reauthorizationWithSameToken() {
        String token = TestDataGenerator.generateValidToken();
        Allure.step("1. Preparing test data", () -> {
            Allure.addAttachment("Correct token", "text/plain", "The token used for the test is: " + token);
        });

        Allure.step("2. First successful authorization", () -> {
            Response response1 = apiClient.executeRequest(token, ApiClient.ACTION_LOGIN);

            Allure.addAttachment("First Request Details", "text/plain",
                    "First authorization attempt with valid token\n" +
                            "Token: " + token + "\n" +
                            "Expected outcome: Successful first login");

            assertEquals("OK", response1.getResult(), "First login should be successful\nError message: " + response1.getMessage());

            Allure.addAttachment("First Response Details", "text/plain",
                    "First authorization successful\n" +
                            "System returned result: OK\n" +
                            "User logged in successfully\n" +
                            "Session established for token: " + token);
        });

        Allure.step("3. Re-authorization with the same token", () -> {
            Response response2 = apiClient.executeRequest(token, ApiClient.ACTION_LOGIN);

            Allure.addAttachment("Second Request Details", "text/plain",
                    "Second authorization attempt with the same token\n" +
                            "Token: " + token + "\n" +
                            "Expected outcome: Authorization failure (re-authorization not allowed)\n" +
                            "Reason: Token already used for active session");

            assertAll("Reauthorization check",
                    () -> assertEquals("ERROR", response2.getResult(),
                            "When trying to re-authorize, an error should be returned"),
                    () -> assertNotNull(response2.getMessage(), "An authorization error message should be returned")
            );

            Allure.addAttachment("Second Response Details", "text/plain",
                    "Re-authorization failed - as expected\n" +
                            "System returned result: ERROR\n" +
                            "Error message provided: " + response2.getMessage() + "\n" +
                            "System correctly prevented re-authorization with same token\n" +
                            "Security check passed: Token reuse protection works correctly");
        });
    }

    @Test
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Checking timeout during authorization")
    @Story("The system correctly handles slow responses from external services")
    @Description("The system must correctly handle situations when a response from an external service does not arrive for a long time")
    @Timeout(10)
    void authorizationTimeoutHandling() {
        String token = TestDataGenerator.generateValidToken();

        Allure.step("1. Set up a mock service with a long response delay", () -> {
            setupExternalServiceTimeout("/auth");
        });

        Allure.step("2. Send a request and check timeout handling", () -> {
            Response response = apiClient.executeRequest(token, ApiClient.ACTION_LOGIN);

            Allure.addAttachment("Request Details", "text/plain",
                    "Authorization request sent with valid token\n" +
                            "Token: " + token + "\n" +
                            "External service: Configured with delay\n" +
                            "Expected outcome: System should handle timeout gracefully\n" +
                            "Test timeout: 10 seconds");

            assertNotNull(response, "A response must be received");

            Allure.addAttachment("Response Details", "text/plain",
                    "System handled timeout scenario\n" +
                            "Response received within timeout limit\n" +
                            "No system freeze or crash\n" +
                            "System result: " + (response.getResult() != null ? response.getResult() : "Response processed") + "\n" +
                            "Timeout handling mechanism works correctly");
        });
    }
}
