package entities;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BasicGame {

    protected final int id;
    protected final @NotNull String word;

    public BasicGame(
        int id,
        @NotNull String word) {

        this.id = id;
        this.word = word;
    }

    public int getId() {
        return id;
    }

    public @NotNull String getWord() {
        return word;
    }

    public boolean isCorrectAnswer(@Nullable String answer) {

        return (answer != null) &&
            this.word.equalsIgnoreCase(answer);
    }
}
