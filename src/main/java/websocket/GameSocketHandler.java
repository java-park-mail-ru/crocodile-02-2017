package websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import database.AccountService;
import database.AccountServiceDb;
import database.DashesService;
import database.DashesServiceDb;
import entities.Dashes;
import entities.SingleplayerGame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import socketmessages.*;

import javax.naming.AuthenticationException;
import java.io.IOException;

@SuppressWarnings("OverlyBroadCatchBlock")
public class GameSocketHandler extends TextWebSocketHandler {



    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final Logger LOGGER = LoggerFactory.getLogger(GameSocketHandler.class);

    private final GameManagerService gameManagerService;
    private final DashesService dashesService;
    private final AccountService accountService;
    private final WebSocketMessageHandler webSocketMessageHandler;

    public GameSocketHandler(
        GameManagerService gameManagerService,
        DashesServiceDb dashesService,
        AccountServiceDb accountService,
        WebSocketMessageHandler webSocketMessageHandler) {

        this.gameManagerService = gameManagerService;
        this.dashesService = dashesService;
        this.accountService = accountService;
        this.webSocketMessageHandler = webSocketMessageHandler;

        webSocketMessageHandler.setHandler(
            MessageType.START_SINGLEPLAYER_GAME,
            (WebSocketSession s, TextMessage m) -> handleStartSinglePlayerGame(s));

        webSocketMessageHandler.setHandler(
            MessageType.CHECK_ANSWER,
            this::handleCheckAnswer);
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws AuthenticationException, JsonProcessingException {

        session.getAttributes().put("login", "bop1");
        final String login = SessionOperator.getLogin(session);

        if (login == null || accountService.findAccount(login) == null) {

            LOGGER.debug("Unlogged user tried to start the game.");
            throw new AuthenticationException("only logged users are allowed to play the game");
        }

        LOGGER.info("Got websocket connection from user {}.", login);
        gameManagerService.clearUserGame(login);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage textMessage) throws Exception {

        try {
            final WebSocketMessage message = readMessage(textMessage, EmptyContent.class);

            LOGGER.info("Got websocket message type {} from user {}.",
                message.getTypeString(), SessionOperator.getLogin(session));

            webSocketMessageHandler.handle(session, textMessage, message.getTypeEnum());

        } catch (Exception exception) {

            LOGGER.error(exception.getMessage());
            throw exception;
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {

        LOGGER.info("Websocket connection with user {} closed with reason {}.",
            SessionOperator.getLogin(session),
            status.getReason());
        super.afterConnectionClosed(session, status);
    }

    @SuppressWarnings("OverlyBroadCatchBlock")
    private void handleStartSinglePlayerGame(WebSocketSession session) throws Exception {

        final String login = SessionOperator.getLogin(session);
        assert login != null;
        final Dashes dashes = dashesService.getRandomDash(login);
        LOGGER.info("Got dashes #{}, {} for {}", dashes.getId(), dashes.getWord(), login);

        final SingleplayerGame game = gameManagerService.createSingleplayerGame(session, login, dashes.getId());
        final float timePassed = gameManagerService.startTimer(game.getId(), GameType.SINGLEPLAYER);

        final WebSocketMessage data = new WebSocketMessage<>(
            MessageType.STATE.toString(),
            new SingleplayerGameStateContent(dashes, timePassed, GameManagerService.SINGLEPLAYER_TIME_LIMIT));
        SessionOperator.sendMessage(session, data);
    }

    @SuppressWarnings("OverlyBroadCatchBlock")
    private void handleCheckAnswer(WebSocketSession session, TextMessage textMessage) throws Exception {

        final String login = SessionOperator.getLogin(session);
        assert login != null;

        final AnswerContent answerContent = (AnswerContent) readMessage(textMessage, AnswerContent.class).getContent();
        LOGGER.info("Got answer {} from user {}", answerContent.getWord(), login);

        gameManagerService.checkAnswer(login, answerContent.getWord());
    }

    private void handleGetSate(WebSocketSession session) throws Exception {

        final String login = SessionOperator.getLogin(session);
        assert login != null;

        gameManagerService.sendGameState(login);
    }

    private WebSocketMessage<?> readMessage(TextMessage textMessage, Class contentClass) throws IOException {

        final JavaType type = OBJECT_MAPPER.getTypeFactory().constructParametricType(WebSocketMessage.class, contentClass);

        return OBJECT_MAPPER.
            readValue(textMessage.getPayload(), type);
    }
}
