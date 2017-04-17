package server;

import database.*;
import messagedata.AnswerData;
import messagedata.DashesData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Controller
public class WebSocketController {

    public static final String SESSION_GAME_ATTR = "pgameid";
    public static final int SINGLE_GAME_TIME = 30;

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationController.class);

    private final AccountServiceDb accountService;
    private final DashesServiceDb dashesService;
    private final SingleGameServiceDb singleGameService;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final HashMap<Integer, SingleGame> currentSingleGames = new HashMap<>();

    @Autowired
    public WebSocketController(
        AccountServiceDb accountService,
        DashesServiceDb dashesService,
        SingleGameServiceDb singleGameService) {

        this.accountService = accountService;
        this.dashesService = dashesService;
        this.singleGameService = singleGameService;
    }

    private synchronized boolean changeGameState(WebSocketSession session, int gameId, boolean shutdown) {

        if (shutdown && currentSingleGames.containsKey(gameId)) {

            singleGameService.shutdownGame(gameId);
            currentSingleGames.remove(gameId);

            try {
                session.close();

            } catch ( IOException exception ) {

                LOGGER.error("Couldn't close websocket session {}.", session.getId());
            }

            return true;
        }
        return false;
    }

    private Runnable runDeletion(WebSocketSession session, int gameId) {

        return () -> changeGameState(session, gameId, true);
    }

    @SubscribeMapping("/sp-create/")
    public DashesData createSingleplayerGame(WebSocketSession session) {

        final String login = ( String ) session.getAttributes().get(ApplicationController.SESSION_LOGIN_ATTR);
        final Dashes dashes = dashesService.getRandomDash(login);

        final SingleGame game = singleGameService.createGame(login, dashes.getId());
        currentSingleGames.put(game.getId(), game);
        session.getAttributes().put(SESSION_GAME_ATTR, game.getId());

        scheduler.schedule(runDeletion(session, game.getId()), SINGLE_GAME_TIME, TimeUnit.SECONDS);

        return new DashesData(game.getId(), dashes);
    }

    @SendTo("/sp-game/{gameId}")
    @MessageMapping("/sp-game/{gameId}")
    public String answerSinglePlayerGame(@DestinationVariable int gameId, AnswerData answer, WebSocketSession session) {

        final String login = ( String ) session.getAttributes().get(ApplicationController.SESSION_LOGIN_ATTR);
        final SingleGame game = currentSingleGames.get(gameId);

        if ((game == null) || !game.getLogin().equals(login)) {

            return "{ \"correct\": false }";
        }

        final int dashesId = game.getDashesId();
        final boolean isCorrect = dashesService.checkWord(answer.getWord(), dashesId);

        if (changeGameState(session, gameId, isCorrect)) {

            dashesService.addUsedDashes(login, dashesId);
            accountService.updateAccountRating(login, 1);
            return "{ \"correct\": true }";
        }

        return "{ \"correct\": false }";
    }
}
