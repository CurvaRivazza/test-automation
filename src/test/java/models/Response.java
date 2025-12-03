package models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Response {
    private String result;
    private String message;

    public Response() {
    }

    public Response(String result) {
        this.result = result;
    }

    public Response(String result, String message) {
        this.result = result;
        this.message = message;
    }

    @JsonProperty("result")
    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    @JsonProperty("message")
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSuccess() {
        return "OK".equals(result);
    }
}
