package server;

public enum ErrorCode {

    INSUFFICIENT("insufficient"),
    LOG_OUT("log_out"),
    EXISTS("exists"),
    FORBIDDEN("forbidden"),
    LOG_IN("log_in"),
    NOT_FOUND("not_found"),
    INVALID_FIELD("invalid_field"),
    INTERNAL("internal");

    private String text;

    ErrorCode(final String text) {

        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }
}
