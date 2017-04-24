package socketmessages;

public enum MessageType {

    EMPTY(""),
    STATE("STATE"),
    GET_STATE("GET_STATE"),
    START_SINGLEPLAYER_GAME("START_SP_GAME"),
    CHECK_ANSWER("GET_ANSWER"),
    STOP_GAME("STOP_GAME"),
    TIMER_STATE("TIMER_STATE");

    private String type;

    MessageType(String type) {
        this.type = type;
    }

    public static MessageType fromString(String stringType) {

        for (MessageType messageType : MessageType.values()) {

            if (messageType.type.equalsIgnoreCase(stringType)) {

                return messageType;
            }
        }

        return EMPTY;
    }

    @Override
    public String toString() {
        return type;
    }
}
