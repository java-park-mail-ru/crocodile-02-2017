package server;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public class ErrorData {

    public static final String CODE_ATTR = "code";
    public static final String MESSAGE_ATTR = "message";

    private final @NotNull String code;
    private final @NotNull String message;

    ErrorData(@NotNull ErrorCode code, @NotNull String message) {

        this.code = code.toString();
        this.message = message;
    }

    @JsonProperty(value = CODE_ATTR)
    public @NotNull String getCode() {
        return code;
    }

    @JsonProperty(value = MESSAGE_ATTR)
    public @NotNull String getMessage() {
        return message;
    }
}
