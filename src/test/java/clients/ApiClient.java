package clients;

import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

public class ApiClient {
    private static final String BASE_URL = "http://localhost:8080";
    private static final String X_API_KEY = "qazWSXedc";
    private static final String ENDPOINT = "/endpoint";
    public static final String ACTION_LOGIN = "LOGIN";
    public static final String ACTION_ACTION = "ACTION";
    public static final String ACTION_LOGOUT = "LOGOUT";

    public models.Response executeRequest(String token, String action) {
        try {
            Response restAssuredResponse = given()
                    .baseUri(BASE_URL)
                    .header("X-Api-Key", X_API_KEY)
                    .contentType("application/x-www-form-urlencoded")
                    .accept("application/json")
                    .formParam("token", token)
                    .formParam("action", action)
                    .when()
                    .post(ENDPOINT);

            return restAssuredResponse.as(models.Response.class);
        } catch (Exception ex){
            return new models.Response("ERROR", "Request failed: " + ex.getMessage());
        }
    }
}
