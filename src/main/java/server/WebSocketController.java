package server;

import database.Dashes;
import database.DashesServiceDb;
import messagedata.AnswerData;
import messagedata.SingleplayerGameData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

@SuppressWarnings("unused")
@Controller
public class WebSocketController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationController.class);

    private final GameManagerService gameManagerService;
    private final DashesServiceDb dashesService;

    @Autowired
    public WebSocketController(
        GameManagerService gameManagerService,
        DashesServiceDb dashesService) {

        this.gameManagerService = gameManagerService;
        this.dashesService = dashesService;
    }

    @SubscribeMapping("/sp-create/")
    public SingleplayerGameData createSingleplayerGame(SimpMessageHeaderAccessor session) {

        session.getSessionAttributes().put(ApplicationController.SESSION_LOGIN_ATTR, "bop1");

        final String login = (String) session.getSessionAttributes().get(ApplicationController.SESSION_LOGIN_ATTR);
        final Dashes dashes = dashesService.getRandomDash(login);
        LOGGER.info("Got dashes {}, {}", dashes.getId(), dashes.getWord());

        final int gameId = gameManagerService.scheduleSingleplayerGameStart(login, dashes.getId());

        return new SingleplayerGameData(gameId, dashes);
    }

    @SubscribeMapping("/sp-game/{gameId}")
    public void startSingleplayerGame(SimpMessageHeaderAccessor session) {

        System.out.println("Started game!");
        final String login = (String) session.getSessionAttributes().get(ApplicationController.SESSION_LOGIN_ATTR);
        gameManagerService.startSingleplayerGame(login);
    }

    @SendTo("/ws/sp-game/{gameId}")
    @MessageMapping("/sp-answer/{gameId}")
    public String answerSingleplayerGame(@DestinationVariable int gameId, AnswerData answer) {

        final boolean answerCorrect = gameManagerService.checkSingleplayerAnswer(gameId, answer.getWord());

        System.out.println(answerCorrect);
        System.out.println("/ws/sp-game/" + gameId);

        return answerCorrect ? "{ \"correct\": true }" : "{ \"correct\": false }";
    }
}
