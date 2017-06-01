package socketmessages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;

public class WebSocketMessage <T extends EmptyContent> {

    public static final String TYPE_ATTR = "type";
    public static final String CONTENT_ATTR = "content";

    private final @NotNull String type;
    private final @NotNull T content;

    @JsonCreator
    public WebSocketMessage(
        @NotNull @JsonProperty(TYPE_ATTR) String type,
        @NotNull @JsonProperty(CONTENT_ATTR) T content) {

        this.type = type;
        this.content = content;
    }

    @JsonProperty(TYPE_ATTR)
    public String getTypeString() {
        return type;
    }

    @JsonIgnore
    public MessageType getTypeEnum() {
        return MessageType.fromString(type);
    }

    @JsonProperty(CONTENT_ATTR)
    public @NotNull T getContent() {
        return content;
    }
}
