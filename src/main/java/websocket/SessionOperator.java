package websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import server.ApplicationController;
import socketmessages.WebSocketMessage;

import java.io.IOException;

public final class SessionOperator {

    public static final String SESSION_LOGIN_ATTR = ApplicationController.SESSION_LOGIN_ATTR;

    private static final Logger LOGGER = LoggerFactory.getLogger(SessionOperator.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private SessionOperator() {
    }

    public static @Nullable String getLogin(WebSocketSession session) {

        return (String) session.getAttributes().get(SESSION_LOGIN_ATTR);
    }

    @SuppressWarnings("OverlyBroadCatchBlock")
    public static void sendMessage(WebSocketSession session, WebSocketMessage message) {

        try {
            session.sendMessage(new TextMessage(OBJECT_MAPPER.writeValueAsString(message)));

        } catch (IOException exception) {
            LOGGER.error("Can't send websocket message to {}.", getLogin(session));
        }
    }
}
