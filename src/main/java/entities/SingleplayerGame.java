package entities;

import org.jetbrains.annotations.NotNull;

public class SingleplayerGame extends BasicGame {

    private final @NotNull String login;
    private final int dashesId;

    public SingleplayerGame(
        int id,
        @NotNull String login,
        int dashesId,
        @NotNull String word) {

        super(id, word);

        this.login = login;
        this.dashesId = dashesId;
    }

    public @NotNull String getLogin() {
        return login;
    }

    public int getDashesId() {
        return dashesId;
    }
}
