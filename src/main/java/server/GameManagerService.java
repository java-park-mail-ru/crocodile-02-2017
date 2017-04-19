package server;

import database.*;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Service
public class GameManagerService {

    private static final int TIMEOUT_SECONDS = 30;
    public static final int SINGLEPLAYER_GAME_TIME_SECONDS = 30;

    private final AccountServiceDb accountService;
    private final DashesServiceDb dashesService;
    private final SingleplayerGamesServiceDb singleplayerGamesService;

    private final Map<String, ScheduledSingleplayerGame> singleplayerGamesToStart =
        Collections.synchronizedMap(new HashMap<String, ScheduledSingleplayerGame>());
    private final Map<Integer, ScheduledSingleplayerGame> singleplayerGamesToEnd =
        Collections.synchronizedMap(new HashMap<Integer, ScheduledSingleplayerGame>());

    @Autowired
    public GameManagerService(
        AccountServiceDb accountService,
        DashesServiceDb dashesService,
        SingleplayerGamesServiceDb singleplayerGamesService) {

        this.accountService = accountService;
        this.dashesService = dashesService;
        this.singleplayerGamesService = singleplayerGamesService;
    }

    private static final class ScheduledSingleplayerGame {

        private static final ScheduledExecutorService SCHEDULER = Executors.newSingleThreadScheduledExecutor();

        private SingleplayerGame game;
        private ScheduledFuture<?> finishTask;

        ScheduledSingleplayerGame(
            String login,
            int dashesId,
            SingleplayerGamesService gamesService) {

            this.game = gamesService.createGame(login, dashesId);
            this.finishTask = null;
        }

        void scheduleFinish(Runnable task, int delaySeconds) {

            cancelTask();
            finishTask = SCHEDULER.schedule(task, delaySeconds, TimeUnit.SECONDS);
        }

        public SingleplayerGame getGame() {
            return game;
        }

        void cancelTask() {

            if (finishTask != null) {

                finishTask.cancel(false);
                finishTask = null;
            }
        }
    }

    private synchronized @Nullable ScheduledSingleplayerGame unscheduleSingleplayerStart(String login, boolean shouldShutdown) {

        if (singleplayerGamesToStart.containsKey(login)) {

            final ScheduledSingleplayerGame result = singleplayerGamesToStart.get(login);
            result.cancelTask();
            singleplayerGamesToStart.remove(login);
            if (shouldShutdown) {
                singleplayerGamesService.shutdownGame(result.getGame().getId());
            }
            return result;
        }

        return null;
    }

    private synchronized @Nullable ScheduledSingleplayerGame endSingleplayerGame(int gameId, boolean shouldFinish) {

        if (shouldFinish && singleplayerGamesToEnd.containsKey(gameId)) {

            final ScheduledSingleplayerGame result = singleplayerGamesToEnd.get(gameId);
            result.cancelTask();
            singleplayerGamesToEnd.remove(gameId);
            singleplayerGamesService.shutdownGame(gameId);
            return result;
        }

        return null;
    }

    public int scheduleSingleplayerGameStart(String login, int dashesId) {

        final ScheduledSingleplayerGame game =
            new ScheduledSingleplayerGame(login, dashesId, singleplayerGamesService);

        game.scheduleFinish(() -> unscheduleSingleplayerStart(login, true), TIMEOUT_SECONDS);
        singleplayerGamesToStart.put(login, game);
        return game.getGame().getId();
    }

    //todo add throws error if game timedout ended
    public void startSingleplayerGame(String login) {

        final ScheduledSingleplayerGame game = unscheduleSingleplayerStart(login, false);

        if (game != null) {

            game.scheduleFinish(
                () -> endSingleplayerGame(game.getGame().getId(), true),
                SINGLEPLAYER_GAME_TIME_SECONDS);

            singleplayerGamesToEnd.put(game.getGame().getId(), game);
        }
    }

    public boolean checkSingleplayerAnswer(int gameId, @Nullable String word) {

        final boolean shouldFinish = singleplayerGamesToEnd.containsKey(gameId) &&
            singleplayerGamesToEnd.get(gameId).getGame().isCorrectAnswer(word);

        final ScheduledSingleplayerGame game = endSingleplayerGame(gameId, shouldFinish);

        if (game != null) {

            dashesService.addUsedDashes(game.getGame().getLogin(), game.getGame().getDashesId());
            accountService.updateAccountRating(game.getGame().getLogin(), 1);
        }

        return game != null; //true if game has finished due to correct answer
    }
}
