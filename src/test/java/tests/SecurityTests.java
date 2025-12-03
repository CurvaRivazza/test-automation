package tests;

import io.qameta.allure.*;
import models.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import utils.TestDataGenerator;
import utils.WireMockBase;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@Epic("Security Testing")
@Feature("API Key Validation")
@DisplayName("Testing authentication and authorization security")
public class SecurityTests {
    @Test
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("System rejects unauthorized access attempts when API key is missing")
    @Story("Application protects endpoints with API key authentication")
    @Description("""
        Verifies that the system properly blocks access and returns ERROR result when API key is missing
        This ensures only authorized clients can access the system.
        """)
    void shouldRejectUnauthorizedAccessWhenApiKeyIsEmpty() {
        String validToken = TestDataGenerator.generateValidToken();

        Allure.step("Test missing API key", () -> {
            Allure.addAttachment("Scenario", "text/plain",
                    "Client tries to access system without providing the required X-Api-Key header.");

            Response apiResponse = given()
                    .baseUri("http://localhost:8080")
                    .contentType("application/x-www-form-urlencoded")
                    .accept("application/json")
                    .formParam("token", validToken)
                    .formParam("action", "LOGIN")
                    .when()
                    .post("/endpoint")
                    .as(Response.class);

            assertAll("System must return ERROR for missing API key",
                    () -> assertEquals("ERROR", apiResponse.getResult(),
                            "Result field must be ERROR when API key is missing"),
                    () -> assertNotNull(apiResponse.getMessage(),
                            "A descriptive error message must be provided to the client")
            );

            Allure.addAttachment("Result", "text/plain",
                    "PASS: System correctly returned ERROR for missing API key.\n" +
                            "Unauthorized request rejected at application level.\n" +
                            "Error message: " + apiResponse.getMessage());
        });
    }

    @Test
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("System rejects unauthorized access attempts when API key is incorrect")
    @Story("Application protects endpoints with API key authentication")
    @Description("""
        Verifies that the system properly blocks access and returns ERROR result when API key is incorrect
        This ensures only authorized clients can access the system.
        """)
    void shouldRejectUnathorizedAccessWhenApiKeyIncorrect(){
        String validToken = TestDataGenerator.generateValidToken();

        Allure.step("Test incorrect API key", () -> {
            Allure.addAttachment("Scenario", "text/plain",
                    "Client provides a wrong API key (correct one is \"qazWSXedc\").");

            Response apiResponse = given()
                    .baseUri("http://localhost:8080")
                    .header("X-Api-Key", "WRONG_KEY")
                    .contentType("application/x-www-form-urlencoded")
                    .accept("application/json")
                    .formParam("token", validToken)
                    .formParam("action", "LOGIN")
                    .when()
                    .post("/endpoint")
                    .as(Response.class);

            assertAll("System must return ERROR for incorrect API key",
                    () -> assertEquals("ERROR", apiResponse.getResult(),
                            "Result field must be ERROR when API key is incorrect"),
                    () -> assertNotNull(apiResponse.getMessage(),
                            "A descriptive error message must be provided")
            );

            Allure.addAttachment("Result", "text/plain",
                    "PASS: System correctly returned ERROR for incorrect API key.\n" +
                            "Invalid credentials rejected.\n" +
                            "Error message: " + apiResponse.getMessage());
        });
    }
}
