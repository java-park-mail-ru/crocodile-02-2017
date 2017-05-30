package entities;

import org.jetbrains.annotations.NotNull;

public class SingleplayerGame extends BasicGame {

    private final @NotNull Dashes dashes;

    public SingleplayerGame(
        int id,
        @NotNull String login,
        @NotNull Dashes dashes) {

        super(id, dashes.getWord());
        this.logins.add(login);
        this.dashes = dashes;
    }

    public @NotNull Dashes getDashes() {
        return dashes;
    }
}
