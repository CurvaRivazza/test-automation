package models;

public class Response {
    private String result;
    private String message;

    public Response(){}
    public Response(String result, String message) {
        this.result = result;
        this.message = message;
    }


    public String getResult() {
        return result;
    }

    public void setResult(String _result) {
        this.result = _result;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
