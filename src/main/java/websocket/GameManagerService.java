package websocket;

import database.AccountService;
import database.AccountServiceDb;
import database.SingleplayerGamesServiceDb;
import entities.BasicGame;
import entities.SingleplayerGame;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;
import socketmessages.*;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.*;

@Service
public class GameManagerService {

    public static final int SINGLEPLAYER_TIME_LIMIT = 60;
    public static final int SINGLEPLAYER_GAME_SCORE = 1;

    private final AccountService accountService;
    private final SingleplayerGamesServiceDb singleplayerGamesService;

    private final Map<String, GameRelation> relatedGames = new ConcurrentHashMap<>();
    private final Map<Integer, ScheduledGame> currentSingleplayerGames = new ConcurrentHashMap<>();
    private final Map<Integer, ScheduledGame> currentMultiplayerGames = new ConcurrentHashMap<>();

    @Autowired
    public GameManagerService(
        AccountServiceDb accountService,
        SingleplayerGamesServiceDb singleplayerGamesService) {

        this.accountService = accountService;
        this.singleplayerGamesService = singleplayerGamesService;
    }

    private static final class GameRelation {

        private int id;
        private GameType type;
        private WebSocketSession session;

        GameRelation(int gameId, GameType gameType, WebSocketSession session) {

            this.id = gameId;
            this.type = gameType;
            this.session = session;
        }

        public int getId() {
            return id;
        }

        public GameType getType() {
            return type;
        }

        public WebSocketSession getSession() {
            return session;
        }
    }

    private static final class ScheduledGame {

        private static final ScheduledExecutorService SCHEDULER = Executors.newSingleThreadScheduledExecutor();

        private final BasicGame game;
        private ScheduledFuture<?> shutdownTask;
        private Runnable shutdownCommand;
        private long timeLeftMillis;
        private ScheduledFuture<?> repeatTask;

        ScheduledGame(BasicGame game) {

            this.game = game;
            this.shutdownTask = SCHEDULER.schedule(() -> (0), 0, TimeUnit.SECONDS);
        }

        void rechedule(Runnable task, int delaySeconds) {

            cancelShutdown();
            shutdownCommand = task;
            timeLeftMillis = 0;
            shutdownTask = SCHEDULER.schedule(task, delaySeconds, TimeUnit.SECONDS);
        }

        void addRepeatable(Runnable task, int periodSeconds) {

            this.repeatTask = SCHEDULER.schedule(task, periodSeconds, TimeUnit.SECONDS);
        }

        synchronized boolean cancelShutdown() {

            timeLeftMillis = shutdownTask.getDelay(TimeUnit.MILLISECONDS);

            if (!shutdownTask.isCancelled() && !shutdownTask.isDone() && (timeLeftMillis > 0)) {

                shutdownTask.cancel(false);
                return true;
            }

            return false;
        }

        synchronized void resumeShutdown() {

            if (shutdownTask.isCancelled() && (timeLeftMillis > 0)) {

                shutdownTask = SCHEDULER.schedule(shutdownCommand, timeLeftMillis, TimeUnit.MILLISECONDS);
            }
        }

        synchronized boolean cancelAll() {

            final boolean result = cancelShutdown();

            if (repeatTask != null) {

                repeatTask.cancel(false);
                return result;
            }

            return false;
        }

        public BasicGame getGame() {
            return game;
        }

        public float getTimeRemaining() {

            if (shutdownTask.isCancelled()) {
                return ((float) timeLeftMillis) / 1000;

            } else if (!shutdownTask.isDone()) {

                return ((float) shutdownTask.getDelay(TimeUnit.MILLISECONDS)) / 1000;
            }

            return Float.POSITIVE_INFINITY;
        }
    }

    public synchronized void endSingleplayerGame(int gameId, GameResult gameResult, int ratingDelta) {

        if (currentSingleplayerGames.containsKey(gameId)) {

            final ScheduledGame scheduledGame = currentSingleplayerGames.get(gameId);
            scheduledGame.cancelAll();
            singleplayerGamesService.shutdownGame(gameId);

            final WebSocketSession session = getGameSessions(gameId, GameType.SINGLEPLAYER).get(0);
            SessionOperator.sendMessage(session, new WebSocketMessage<>(
                MessageType.STOP_GAME.toString(), new StopGameContent(gameResult, ratingDelta)));
            currentSingleplayerGames.remove(gameId);
        }
    }

    public SingleplayerGame createSingleplayerGame(WebSocketSession session, String login, int dashesId) {

        final SingleplayerGame game = singleplayerGamesService.createGame(login, dashesId);
        final ScheduledGame scheduledGame = new ScheduledGame(game);

        final int gameId = game.getId();
        relatedGames.put(login, new GameRelation(gameId, GameType.SINGLEPLAYER, session));
        currentSingleplayerGames.put(game.getId(), scheduledGame);
        return game;
    }

    //todo throws error if not exists
    public void sendGameState(@NotNull String login) {

        final ScheduledGame game = getUserScheduledGame(login);

    }

    //todo throws error if not exists
    public float startTimer(int gameId, GameType gameType) {

        final ScheduledGame scheduledGame = getScheduledGame(gameId, gameType);

        if (scheduledGame != null) {

            scheduledGame.rechedule(
                getLoseTask(scheduledGame, gameType),
                getFinishTime(gameType));

            /*for ( WebSocketSession session : getGameSessions( gameId, gameType ) ) {

                scheduledGame.addRepeatable(
                    () -> (
                        sendMessage( session, );)
                );
            }*/

            return scheduledGame.getTimeRemaining();
        }

        return 0;
    }

    public synchronized void checkAnswer(String login, @Nullable String word) throws Exception {

        final ScheduledGame scheduledGame = getUserScheduledGame(login);

        if (scheduledGame == null) {

            throw new Exception("answer to game that does not exist");
        }

        if (!scheduledGame.cancelShutdown()) {
            return;
        }

        final boolean answerCorrect = scheduledGame.getGame().isCorrectAnswer(word);

        final GameRelation gameRelation = relatedGames.get(login);
        sendAnswerResponse(gameRelation.getSession(), answerCorrect);

        if (answerCorrect) {

            runWinTask(scheduledGame, gameRelation.getType(), login);

        } else {
            scheduledGame.resumeShutdown();
        }
    }

    public void clearUserGame(@NotNull String login) {

        final ScheduledGame scheduledGame = getUserScheduledGame(login);

        if (scheduledGame != null) {

            final int gameId = scheduledGame.getGame().getId();
            scheduledGame.cancelShutdown();
            currentSingleplayerGames.remove(gameId);
            singleplayerGamesService.shutdownGame(gameId);
        }
    }
    private int getFinishTime(GameType gameType) {

        return (gameType == GameType.SINGLEPLAYER) ?
            SINGLEPLAYER_TIME_LIMIT :
            SINGLEPLAYER_TIME_LIMIT; //todo correct for multiplayer
    }

    private Runnable getLoseTask(ScheduledGame scheduledGame, GameType gameType) {

        return (gameType == GameType.SINGLEPLAYER) ?
            () -> endSingleplayerGame(scheduledGame.getGame().getId(), GameResult.GAME_LOST, 0) :
            () -> endSingleplayerGame(scheduledGame.getGame().getId(), GameResult.GAME_LOST, 0); //todo correct for multiplayer
    }

    private void runWinTask(ScheduledGame scheduledGame, GameType gameType, String winnerLogin) {

        if (gameType == GameType.SINGLEPLAYER) {

            accountService.updateAccountRating(winnerLogin, SINGLEPLAYER_GAME_SCORE);
            endSingleplayerGame(scheduledGame.getGame().getId(), GameResult.GAME_WON, SINGLEPLAYER_GAME_SCORE);
        }

        //todo correct for multiplayer
    }

    private void sendAnswerResponse(WebSocketSession session, boolean answerCorrect) {

        final WebSocketMessage data = new WebSocketMessage<>(
            MessageType.CHECK_ANSWER.toString(), new AnswerResponseContent(answerCorrect));
        SessionOperator.sendMessage(session, data);
    }

    private @Nullable ScheduledGame getScheduledGame(int gameId, GameType gameType) {

        return (gameType == GameType.SINGLEPLAYER) ?
            currentSingleplayerGames.get(gameId) :
            currentMultiplayerGames.get(gameId);
    }

    private @Nullable ScheduledGame getUserScheduledGame(String login) {

        final GameRelation gameRelation = relatedGames.get(login);
        if (gameRelation == null) {
            return null;
        }

        final int gameId = gameRelation.getId();
        final GameType gameType = gameRelation.getType();

        return (gameType == GameType.SINGLEPLAYER) ?
            currentSingleplayerGames.get(gameId) :
            currentMultiplayerGames.get(gameId);
    }

    private ArrayList<WebSocketSession> getGameSessions(int gameId, GameType gameType) {

        final ScheduledGame scheduledGame = getScheduledGame(gameId, gameType);
        final ArrayList<WebSocketSession> sessions = new ArrayList<>();

        if (scheduledGame != null) {

            if (gameType == GameType.SINGLEPLAYER) {

                final String login = ((SingleplayerGame) scheduledGame.getGame()).getLogin();
                sessions.add(relatedGames.get(login).getSession());

            } else if (gameType == GameType.MULTIPLAYER) {

                //todo
            }
        }

        return sessions;
    }
}
