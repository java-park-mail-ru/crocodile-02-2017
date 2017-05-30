package websocket;

import database.SingleplayerGamesService;
import entities.SingleplayerGame;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.socket.WebSocketSession;
import socketmessages.*;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;

public class SingleplayerScheduledGameManager extends ScheduledGameManager<SingleplayerGame> {

    private final SingleplayerGamesService singleplayerGamesService;
    private final GameRelationManager gameRelationManager;

    public final class SingleplayerScheduledGame extends ScheduledGame<SingleplayerGame> {

        private SingleplayerScheduledGame(ScheduledExecutorService scheduler, SingleplayerGame game) {
            super(scheduler, game);
        }

        @Override
        GameType getType() {
            return GameType.SINGLEPLAYER;
        }

        @Override
        WebSocketMessage<BaseGameContent> getGameStateMessage(@NotNull String login) {

            final MessageType messageType = MessageType.STATE;

            return new WebSocketMessage<>(
                messageType.toString(),
                new SingleplayerGameStateContent(
                    game.getDashes(),
                    this.getTimeLeft(),
                    GameManagerService.SINGLEPLAYER_TIME_LIMIT));
        }

        @NotNull
        @Override
        WebSocketMessage<BaseGameContent> getJoinGameMessage(@NotNull String login) {

            final MessageType messageType = MessageType.START_SINGLEPLAYER_GAME;

            return new WebSocketMessage<>(
                messageType.toString(),
                new SingleplayerGameStateContent(
                    game.getDashes(),
                    this.getTimeLeft(),
                    GameManagerService.SINGLEPLAYER_TIME_LIMIT));
        }

        private synchronized void endSingleplayerGame(GameResult gameResult) {

            final int gameId = this.getGame().getId();

            if (currentGames.containsKey(gameId)) {

                cancelAll();

                final WebSocketSession session = gameRelationManager.getGameSessions(this).get(0);
                SessionOperator.sendMessage(session, new WebSocketMessage<>(
                    MessageType.STOP_GAME.toString(),
                    new FinishGameContent(
                        gameResult, getWinScore(),
                        SessionOperator.getLogin(session),
                        getGame().getWord())));

                currentGames.remove(gameId);
                singleplayerGamesService.shutdownGame(gameId);
                gameRelationManager.removeRelation(SessionOperator.getLogin(session));
                LOGGER.info("Singleplayer game #{} ended with result {}.", gameId, gameResult.asInt());
            }
        }

        @Override
        synchronized void runWinTask(@NotNull String winnerLogin) {

            endSingleplayerGame(GameResult.GAME_WON);
        }

        @Override
        synchronized void runLoseTask() {

            endSingleplayerGame(GameResult.GAME_LOST);
        }

        @Override
        int getWinScore() {
            return GameManagerService.SINGLEPLAYER_GAME_SCORE;
        }

        @Override
        int getFinishTime() {
            return GameManagerService.SINGLEPLAYER_TIME_LIMIT;
        }
    }

    public SingleplayerScheduledGameManager(
        ScheduledExecutorService scheduler,
        SingleplayerGamesService service,
        GameRelationManager gameRelationManager) {

        super(scheduler, new ConcurrentHashMap<>());
        this.singleplayerGamesService = service;
        this.gameRelationManager = gameRelationManager;
    }

    @Override
    public @NotNull SingleplayerScheduledGame createScheduledGame(SingleplayerGame game) {

        final SingleplayerScheduledGame scheduledGame = new SingleplayerScheduledGame(scheduler, game);
        currentGames.put(game.getId(), scheduledGame);
        return scheduledGame;
    }
}
