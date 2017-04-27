package entities;

import org.jetbrains.annotations.NotNull;

public class SingleplayerGame extends BasicGame {

    private final @NotNull String login;
    private final @NotNull Dashes dashes;

    public SingleplayerGame(
        int id,
        @NotNull String login,
        @NotNull Dashes dashes) {

        super(id, dashes.getWord());

        this.login = login;
        this.dashes = dashes;
    }

    public @NotNull String getLogin() {
        return login;
    }

    public @NotNull Dashes getDashes() {
        return dashes;
    }
}
