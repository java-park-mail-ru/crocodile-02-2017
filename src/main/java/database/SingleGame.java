package database;

import org.jetbrains.annotations.NotNull;

public class SingleGame {

    private final int id;
    private final @NotNull String login;
    private final int dashesId;
    private final @NotNull String word;

    public SingleGame(
        int id,
        @NotNull String login,
        int dashesId,
        @NotNull String word) {

        this.id = id;
        this.login = login;
        this.dashesId = dashesId;
        this.word = word;
    }

    public int getId() {
        return id;
    }

    public @NotNull String getLogin() {
        return login;
    }

    public int getDashesId() {
        return dashesId;
    }

    public @NotNull String getWord() {
        return word;
    }
}
