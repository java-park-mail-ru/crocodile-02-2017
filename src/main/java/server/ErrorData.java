package server;

@SuppressWarnings("unused")
public class ErrorData {

    private final String code;
    private final String message;

    ErrorData(ErrorCode code, String message) {

        this.code = code.toString();
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
