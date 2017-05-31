package entities;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class MultiplayerGame extends BasicGame {

    private static final AtomicInteger ID_GEN = new AtomicInteger(1);

    public MultiplayerGame(
        @NotNull String word,
        @NotNull List<String> logins) {

        super(ID_GEN.getAndIncrement(), word);
        this.logins.addAll(logins);
    }
}
