package tests;

import io.qameta.allure.*;
import models.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import utils.TestDataGenerator;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@Epic("Protocol Testing")
@Feature("HTTP Request Validation")
@DisplayName("Testing HTTP protocol compliance")
public class HttpProtocolTests {
    @Test
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("System returns ERROR for wrong Content-Type")
    @Story("Application validates request Content-Type header")
    @Description("""
            Verifies that the system returns ERROR result when client sends
            wrong Content-Type header (JSON instead of form-urlencoded).
            Business impact: Ensures API is used correctly according to specification.
            """)
    void shouldReturnErrorForWrongContentType() {
        String validToken = TestDataGenerator.generateValidToken();

        Allure.step("1. Prepare request with wrong Content-Type", () -> {
            Allure.addAttachment("Test Scenario", "text/plain",
                    "Sending POST request with Content-Type: application/json\n" +
                            "Required format: application/x-www-form-urlencoded\n" +
                            "Expected: Application should detect wrong format and return ERROR");
        });

        Allure.step("2. Send request and validate response", () -> {
            Response apiResponse = given()
                    .baseUri("http://localhost:8080")
                    .header("X-Api-Key", "qazWSXedc")
                    .contentType("application/json")
                    .accept("application/json")
                    .body("{\"token\":\"" + validToken + "\",\"action\":\"LOGIN\"}")
                    .when()
                    .post("/endpoint")
                    .as(Response.class);

            assertAll("Application should return ERROR for wrong Content-Type",
                    () -> assertEquals("ERROR", apiResponse.getResult(),
                            "Result field must be 'ERROR' when Content-Type is incorrect"),
                    () -> assertNotNull(apiResponse.getMessage(),
                            "A descriptive error message must be provided to help client fix the issue")
            );

            Allure.addAttachment("Test Result", "text/plain",
                    "CONTENT-TYPE VALIDATION PASSED\n" +
                            "System correctly validates Content-Type header\n" +
                            "Application returns ERROR with message for wrong format\n" +
                            "Error message: " + apiResponse.getMessage() +
                            "\nAPI specification is enforced at application level");
        });
    }

    @Test
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Wrong HTTP method returns appropriate error")
    @Story("Application handles incorrect HTTP methods")
    @Description("""
            Verifies how the system handles requests with wrong HTTP method
            (GET instead of POST). The error might come from application logic
            or web framework.
            """)
    void shouldHandleWrongHttpMethod() {
        String validToken = TestDataGenerator.generateValidToken();

        Allure.step("1. Send GET request to POST-only endpoint", () -> {
            Allure.addAttachment("Test Scenario", "text/plain",
                    "Sending GET request to endpoint that requires POST method\n" +
                            "Endpoint specification: POST /endpoint\n" +
                            "Test method: GET /endpoint\n");
        });

        Allure.step("Step 2: Analyze system response", () -> {
            Response response = given()
                    .baseUri("http://localhost:8080")
                    .header("X-Api-Key", "qazWSXedc")
                    .accept("application/json")
                    .queryParam("token", validToken)
                    .queryParam("action", "LOGIN")
                    .when()
                    .get("/endpoint")
                    .as(Response.class);

            assertEquals("ERROR", response.getResult(),
                    "GET request should return ERROR result");

            Allure.addAttachment("Test Result", "text/plain",
                    "Application logic handled the wrong method\n" +
                            "System correctly rejects GET requests to POST endpoint\n" +
                            "Response parsed as application JSON\n" +
                            "Error message: " + response.getMessage());

        });
    }


    @Test
    @Severity(SeverityLevel.MINOR)
    @DisplayName("Non-existent endpoint returns appropriate error")
    @Story("System handles requests to invalid URLs")
    @Description("""
            Verifies that requests to non-existent endpoints are properly rejected.
            """)
    void shouldHandleNonExistentEndpoint() {
        String validToken = TestDataGenerator.generateValidToken();

        Allure.step("1. Send request to wrong endpoint", () -> {
            Allure.addAttachment("Test Scenario", "text/plain",
                    "Sending request to: POST /nonexistent\n" +
                            "Correct endpoint: POST /endpoint\n");
        });

        Allure.step("2. Analyze system response", () -> {
            Response response = given()
                    .baseUri("http://localhost:8080")
                    .header("X-Api-Key", "qazWSXedc")
                    .contentType("application/x-www-form-urlencoded")
                    .accept("application/json")
                    .formParam("token", validToken)
                    .formParam("action", "LOGIN")
                    .when()
                    .post("/nonexistent")
                    .as(Response.class);

            assertEquals("ERROR", response.getResult(),
                    "Non-existent endpoint should return ERROR result");

            Allure.addAttachment("Test Result", "text/plain",
                    "ENDPOINT VALIDATION PASSED\n" +
                            "Web framework properly routes requests\n" +
                            "Error message: " + response.getMessage() +
                            "\nNo application logic executed for wrong URLs\n" +
                            "Security: Attackers cannot access random paths");
        });
    }
}
