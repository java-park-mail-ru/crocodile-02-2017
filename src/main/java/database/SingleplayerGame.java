package database;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SingleplayerGame {

    private final int id;
    private final @NotNull String login;
    private final int dashesId;
    private final @NotNull String word;

    public SingleplayerGame(
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

    public boolean isCorrectAnswer(@Nullable String answer) {

        return (answer != null) &&
            this.word.toLowerCase().equals(answer.toLowerCase());
    }
}
