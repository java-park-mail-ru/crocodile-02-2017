package websocket;

import entities.BasicGame;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

abstract class ScheduledGameManager <M extends BasicGame> {

    protected static final Logger LOGGER = LoggerFactory.getLogger(BasicGame.class);

    protected final ScheduledExecutorService scheduler;
    protected final Map<Integer, ScheduledGame<M>> currentGames;

    ScheduledGameManager(
        ScheduledExecutorService scheduler,
        Map<Integer, ScheduledGame<M>> currentGames) {

        this.scheduler = scheduler;
        this.currentGames = currentGames;
    }

    abstract @NotNull ScheduledGame<M> createScheduledGame(M game);

    public @Nullable ScheduledGame<M> getScheduledGame(int id) {

        return currentGames.get(id);
    }

    public void removeScheduledGame(int id) {

        currentGames.remove(id);
    }
}
