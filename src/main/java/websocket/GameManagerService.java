package websocket;

import database.*;
import entities.BasicGame;
import entities.Dashes;
import entities.MultiplayerGame;
import entities.SingleplayerGame;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;
import socketmessages.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
public class GameManagerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GameManagerService.class);

    public static final int SINGLEPLAYER_TIME_LIMIT = 60;
    public static final int SINGLEPLAYER_GAME_SCORE = 1;

    public static final int MULTIPLAYER_LOWER_PLAYERS_LIMIT = 2;
    public static final int MULTIPLAYER_UPPER_PLAYERS_LIMIT = 6;
    public static final int MULTIPLAYER_GAME_SCORE = 3;
    public static final int MULTIPLAYER_TIME_LIMIT = 120;

    private final AccountService accountService;
    private final DashesService dashesService;
    private final SingleplayerGamesService singleplayerGamesService;
    private final MultiplayerGamesService multiplayerGamesService;

    private final Map<String, GameRelation> relatedGames = new ConcurrentHashMap<>();
    private final Map<Integer, ScheduledGame> currentSingleplayerGames = new ConcurrentHashMap<>();
    private final Map<Integer, ScheduledGame> currentMultiplayerGames = new ConcurrentHashMap<>();
    private final Map<String, QueueRelation> queuedPlayers = new ConcurrentHashMap<>();

    private static final ScheduledExecutorService SCHEDULER = Executors.newSingleThreadScheduledExecutor();


    @Autowired
    public GameManagerService(
        AccountServiceDb accountService,
        DashesServiceDb dashesService,
        SingleplayerGamesServiceDb singleplayerGamesService,
        MultiplayerGamesServiceDb multiplayerGamesService) {

        this.accountService = accountService;
        this.dashesService = dashesService;
        this.singleplayerGamesService = singleplayerGamesService;
        this.multiplayerGamesService = multiplayerGamesService;
    }

    private static final class QueueRelation {

        private PlayerRole role;
        private WebSocketSession session;

        QueueRelation(PlayerRole role, WebSocketSession session) {

            this.role = role;
            this.session = session;
        }

        public PlayerRole getRole() {
            return role;
        }

        public WebSocketSession getSession() {
            return session;
        }
    }

    @SuppressWarnings("unused")
    private static final class GameRelation {

        private int gameId;
        private GameType type;
        private PlayerRole role;
        private WebSocketSession session;

        GameRelation(int gameId, GameType gameType, WebSocketSession session) {

            this.gameId = gameId;
            this.type = gameType;
            this.role = PlayerRole.GUESSER;
            this.session = session;
        }

        GameRelation(int gameId, GameType gameType, PlayerRole role, WebSocketSession session) {

            this.gameId = gameId;
            this.type = gameType;
            this.role = role;
            this.session = session;
        }

        public int getGameId() {
            return gameId;
        }

        public GameType getType() {
            return type;
        }

        public PlayerRole getRole() {
            return role;
        }

        public WebSocketSession getSession() {
            return session;
        }
    }

    @SuppressWarnings("unused")
    private static final class ScheduledGame {

        private final BasicGame game;
        private final GameType type;
        private ScheduledFuture<?> shutdownTask;
        private Runnable shutdownCommand;
        private long timeLeftMillis;
        private ScheduledFuture<?> repeatTask;

        private ArrayList<PicturePointContent> points = new ArrayList<>();

        ScheduledGame(BasicGame game, GameType type) {

            this.game = game;
            this.type = type;
            this.shutdownTask = SCHEDULER.schedule(() -> (0), 0, TimeUnit.SECONDS);
        }

        public GameType getType() {
            return type;
        }

        public void addPoint(PicturePointContent point) {
            points.add(point);
        }

        public ArrayList<PicturePointContent> getPoints() {
            return points;
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

    public synchronized void endSingleplayerGame(int gameId, GameResult gameResult) {

        if (currentSingleplayerGames.containsKey(gameId)) {

            final ScheduledGame scheduledGame = currentSingleplayerGames.get(gameId);
            scheduledGame.cancelAll();
            singleplayerGamesService.shutdownGame(gameId);

            final int score = (gameResult == GameResult.GAME_WON) ? SINGLEPLAYER_GAME_SCORE : 0;
            final WebSocketSession session = getGameSessions(gameId, GameType.SINGLEPLAYER).get(0);
            SessionOperator.sendMessage(session, new WebSocketMessage<>(
                MessageType.STOP_GAME.toString(), new StopGameContent(gameResult, score)));
            currentSingleplayerGames.remove(gameId);
            relatedGames.remove(SessionOperator.getLogin(session));
        }
    }

    public synchronized void endMultiplayerGame(int gameId, GameResult gameResult, @Nullable String winnerLogin) {

        if (currentMultiplayerGames.containsKey(gameId)) {

            final ScheduledGame scheduledGame = currentMultiplayerGames.get(gameId);
            scheduledGame.cancelAll();
            multiplayerGamesService.shutdownGame(gameId);

            final int score = (gameResult == GameResult.GAME_WON) ? MULTIPLAYER_GAME_SCORE : 0;
            final ArrayList<WebSocketSession> losers = getGameSessions(gameId, GameType.MULTIPLAYER);

            if (gameResult == GameResult.GAME_WON) {

                SessionOperator.sendMessage(
                    relatedGames.get(winnerLogin).getSession(),
                    new WebSocketMessage<>(
                        MessageType.STOP_GAME.toString(), new StopGameContent(gameResult, score))
                );

                losers.remove(relatedGames.get(winnerLogin).getSession());
                relatedGames.remove(winnerLogin);
            }

            losers.forEach(
                (WebSocketSession session) -> {

                    SessionOperator.sendMessage(
                        session, new WebSocketMessage<>(
                            MessageType.STOP_GAME.toString(),
                            new StopGameContent(GameResult.GAME_LOST, 0)));

                    relatedGames.remove(SessionOperator.getLogin(session));
                });

            currentMultiplayerGames.remove(gameId);
        }
    }

    public SingleplayerGame createSingleplayerGame(WebSocketSession session) {

        final String login = SessionOperator.getLogin(session);
        final Dashes dashes = dashesService.getRandomDash(login);

        LOGGER.info("Got dashes #{}, {} for {}", dashes.getId(), dashes.getWord(), login);

        final SingleplayerGame game = singleplayerGamesService.createGame(login, dashes.getId());
        final ScheduledGame scheduledGame = new ScheduledGame(game, GameType.SINGLEPLAYER);

        final int gameId = game.getId();
        relatedGames.put(login, new GameRelation(gameId, GameType.SINGLEPLAYER, session));
        currentSingleplayerGames.put(game.getId(), scheduledGame);

        return game;
    }

    @SuppressWarnings({"Convert2MethodRef", "InfiniteLoopStatement"})
    private void checkQueue() throws IOException {

        final ArrayList<String> possiblePainters = new ArrayList<>();
        possiblePainters.addAll(
            queuedPlayers.entrySet().stream()
                .filter(e -> e.getValue().getRole() != PlayerRole.GUESSER)
                .map(e -> e.getKey())
                .collect(Collectors.toList()));

        final ArrayList<String> possibleGuessers = new ArrayList<>();
        possibleGuessers.addAll(
            queuedPlayers.entrySet().stream()
                .filter(e -> e.getValue().getRole() != PlayerRole.PAINTER)
                .map(e -> e.getKey())
                .collect(Collectors.toList()));

        if (!possiblePainters.isEmpty()) {

            final String painter = possiblePainters.get(0);
            possibleGuessers.remove(painter);

            if (possibleGuessers.size() >= (MULTIPLAYER_LOWER_PLAYERS_LIMIT - 1)) {

                final ArrayList<String> guessers = new ArrayList<>();
                guessers.addAll(possibleGuessers.subList(0, Math.min(possibleGuessers.size(), MULTIPLAYER_UPPER_PLAYERS_LIMIT)));
                final MultiplayerGame game = createMultiplayerGame(painter, guessers);
                startTimer(game.getId(), GameType.MULTIPLAYER);
            }
        }
    }

    public void queueForMultiplayerGame(WebSocketSession session, PlayerRole role) throws IOException {

        final String login = SessionOperator.getLogin(session);
        queuedPlayers.put(login, new QueueRelation(role, session));
        checkQueue();
    }

    private MultiplayerGame createMultiplayerGame(String painterLogin, ArrayList<String> guesserLogins) {

        @SuppressWarnings("UnnecessaryLocalVariable")
        final ArrayList<String> players = guesserLogins;
        players.add(painterLogin);

        final String word = dashesService.getRandomDash(painterLogin).getWord();

        final MultiplayerGame game = multiplayerGamesService.createGame(word, players);
        currentMultiplayerGames.put(game.getId(), new ScheduledGame(game, GameType.MULTIPLAYER));
        LOGGER.info("Got word {} for multiplayer game #{}", word, game.getId());

        guesserLogins.forEach(
            (String login) ->
                relatedGames.put(
                    login,
                    new GameRelation(
                        game.getId(),
                        GameType.MULTIPLAYER,
                        PlayerRole.GUESSER,
                        queuedPlayers.get(login).getSession())));

        relatedGames.put(
            painterLogin,
            new GameRelation(
                game.getId(),
                GameType.MULTIPLAYER,
                PlayerRole.PAINTER,
                queuedPlayers.get(painterLogin).getSession()));

        players.forEach(queuedPlayers::remove);

        return game;
    }

    //todo throws error if not exists
    public void addPoint(WebSocketSession session, PicturePointContent point) {

        final String login = SessionOperator.getLogin(session);

        final ScheduledGame scheduledGame = getUserScheduledGame(login);

        if ((scheduledGame == null) || (scheduledGame.getType() == GameType.SINGLEPLAYER)) {
            return;
        }

        final ArrayList<WebSocketSession> recieverSessions = getGameSessions(scheduledGame.getGame().getId(), GameType.MULTIPLAYER);
        recieverSessions.removeIf(e -> SessionOperator.getLogin(e).equals(login));
        scheduledGame.addPoint(point);
        recieverSessions.forEach(
            (WebSocketSession reciever) ->
                SessionOperator.sendMessage(
                    reciever,
                    new WebSocketMessage<>(
                        MessageType.NEW_POINT.toString(),
                        point)));
    }

    //todo throws error if not exists
    public void sendGameState(WebSocketSession session) throws IOException {

        final String login = SessionOperator.getLogin(session);

        final ScheduledGame scheduledGame = getUserScheduledGame(login);

        if (scheduledGame == null) {
            return;
        }

        final GameRelation gameRelation = relatedGames.get(login);

        SessionOperator.sendMessage(
            gameRelation.getSession(),
            getGameState(scheduledGame, login));
    }

    //todo throws error if not exists
    public float startTimer(int gameId, GameType gameType) throws IOException {

        final ScheduledGame scheduledGame = getScheduledGame(gameId, gameType);

        if (scheduledGame != null) {

            scheduledGame.rechedule(
                getLoseTask(scheduledGame),
                getFinishTime(gameType));

            final ArrayList<WebSocketSession> playerSessions = getGameSessions(gameId, gameType);

            for (WebSocketSession session : playerSessions) {

                final String login = SessionOperator.getLogin(session);

                final WebSocketMessage<BaseGameContent> gameState = getGameState(scheduledGame, login);
                SessionOperator.sendMessage(session, gameState);
            }

            return scheduledGame.getTimeRemaining();
        }

        return 0;
    }

    public synchronized void checkAnswer(WebSocketSession session, @Nullable String word) throws Exception {

        final String login = SessionOperator.getLogin(session);
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

            runWinTask(scheduledGame, login);

        } else {
            scheduledGame.resumeShutdown();
        }
    }

    private int getFinishTime(GameType gameType) {

        return (gameType == GameType.SINGLEPLAYER) ?
            SINGLEPLAYER_TIME_LIMIT :
            MULTIPLAYER_TIME_LIMIT;
    }

    private Runnable getLoseTask(ScheduledGame scheduledGame) {

        return (scheduledGame.getType() == GameType.SINGLEPLAYER) ?
            () -> endSingleplayerGame(scheduledGame.getGame().getId(), GameResult.GAME_LOST) :
            () -> endMultiplayerGame(scheduledGame.getGame().getId(), GameResult.GAME_LOST, null);
    }

    private void runWinTask(ScheduledGame scheduledGame, String winnerLogin) {

        if (scheduledGame.getType() == GameType.SINGLEPLAYER) {

            accountService.updateAccountRating(winnerLogin, SINGLEPLAYER_GAME_SCORE);
            endSingleplayerGame(scheduledGame.getGame().getId(), GameResult.GAME_WON);
        } else {

            accountService.updateAccountRating(winnerLogin, MULTIPLAYER_GAME_SCORE);
            endMultiplayerGame(scheduledGame.getGame().getId(), GameResult.GAME_WON, winnerLogin);
        }
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

        final int gameId = gameRelation.getGameId();
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

                final ArrayList<String> logins = ((MultiplayerGame) scheduledGame.getGame()).getUserLogins();
                sessions.addAll(relatedGames.entrySet().stream()
                    .filter(e -> logins.contains(e.getKey()))
                    .map(e -> e.getValue().getSession())
                    .collect(Collectors.toList()));
            }
        }

        return sessions;
    }

    private @NotNull WebSocketMessage<BaseGameContent> getGameState(@NotNull ScheduledGame scheduledGame, @NotNull String login) throws IOException {

        if (scheduledGame.getType() == GameType.SINGLEPLAYER) {

            final SingleplayerGame singleplayerGame = (SingleplayerGame) scheduledGame.getGame();

            return new WebSocketMessage<>(
                MessageType.STATE.toString(),
                new SingleplayerGameStateContent(
                    singleplayerGame.getDashes(),
                    scheduledGame.getTimeRemaining(),
                    SINGLEPLAYER_TIME_LIMIT));

        } else {

            final MultiplayerGame multiplayerGame = (MultiplayerGame) scheduledGame.getGame();

            return new WebSocketMessage<>(
                MessageType.STATE.toString(),
                new MultiplayerGameStateContent(
                    scheduledGame.getTimeRemaining(),
                    MULTIPLAYER_TIME_LIMIT,
                    relatedGames.get(login).getRole(),
                    multiplayerGame.getUserLogins(),
                    multiplayerGame.getWord()));
        }
    }
}
