package entities;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicInteger;

public class SingleplayerGame extends BasicGame {

    private static final AtomicInteger ID_GEN = new AtomicInteger(1);

    private final @NotNull Dashes dashes;

    public SingleplayerGame(
        @NotNull String login,
        @NotNull Dashes dashes) {

        super(ID_GEN.getAndIncrement(), dashes.getWord());
        this.logins.add(login);
        this.dashes = dashes;
    }

    public @NotNull Dashes getDashes() {
        return dashes;
    }
}
