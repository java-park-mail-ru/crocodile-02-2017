package websocket;

import entities.BasicGame;
import org.jetbrains.annotations.NotNull;
import socketmessages.*;

import java.util.ArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("unused")
abstract class ScheduledGame <T extends BasicGame> {

    protected final T game;

    private final ScheduledExecutorService scheduler;
    private final ArrayList<PicturePointContent> points = new ArrayList<>();
    private ScheduledFuture<?> shutdownTask;
    private Runnable shutdownCommand;
    private long timeLeftMillis;
    private ScheduledFuture<?> repeatTask;

    ScheduledGame(ScheduledExecutorService scheduler, T game) {

        this.scheduler = scheduler;
        this.game = game;
        this.shutdownTask = scheduler.schedule(() -> (0), 0, TimeUnit.SECONDS);
    }

    abstract GameType getType();

    public void addPoint(PicturePointContent point) {
        points.add(point);
    }

    public ArrayList<PicturePointContent> getPoints() {
        return points;
    }

    public void rechedule(Runnable task, int delaySeconds) {

        cancelShutdown();
        shutdownCommand = task;
        timeLeftMillis = delaySeconds;
        shutdownTask = scheduler.schedule(task, delaySeconds, TimeUnit.SECONDS);
    }

    public void setRepeatable(Runnable task, int periodSeconds) {
        this.repeatTask = scheduler.schedule(task, periodSeconds, TimeUnit.SECONDS);
    }

    public synchronized boolean cancelShutdown() {

        timeLeftMillis = shutdownTask.getDelay(TimeUnit.MILLISECONDS);

        if (!shutdownTask.isCancelled() && !shutdownTask.isDone() && (timeLeftMillis > 0)) {

            shutdownTask.cancel(false);
            return true;
        }

        return false;
    }

    public synchronized void resumeShutdown() {

        if (shutdownTask.isCancelled() && (timeLeftMillis > 0)) {

            shutdownTask = scheduler.schedule(shutdownCommand, timeLeftMillis, TimeUnit.MILLISECONDS);
        }
    }

    public synchronized void cancelAll() {

        cancelShutdown();
        if (repeatTask != null) {
            repeatTask.cancel(false);
        }
    }

    public T getGame() {
        return game;
    }

    public float getTimeLeft() {

        if (shutdownTask.isCancelled()) {
            return ((float) timeLeftMillis) / 1000;

        } else if (!shutdownTask.isDone()) {
            return ((float) shutdownTask.getDelay(TimeUnit.MILLISECONDS)) / 1000;
        }

        return Float.POSITIVE_INFINITY;
    }

    public abstract @NotNull WebSocketMessage<BaseGameContent> getGameStateMessage(@NotNull String login);

    public abstract @NotNull WebSocketMessage<BaseGameContent> getJoinGameMessage(@NotNull String login);

    public abstract void runWinTask(@NotNull String winnerLogin);

    public abstract void runLoseTask(@NotNull GameResult result);

    public abstract int getWinScore();

    public abstract int getFinishTime();
}
