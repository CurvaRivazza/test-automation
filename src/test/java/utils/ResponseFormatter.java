package utils;

import models.Response;

public class ResponseFormatter {
    public static String formatResponse(Response response) {
        return String.format("{\n  \"result\": \"%s\",\n  \"message\": \"%s\"\n}",
                response.getResult(),
                response.getMessage() != null ? response.getMessage() : "null");
    }
}
