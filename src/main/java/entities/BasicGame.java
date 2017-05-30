package entities;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public abstract class BasicGame {

    protected final int id;
    protected final @NotNull String word;
    protected final @NotNull ArrayList<String> logins;

    public BasicGame(
        int id,
        @NotNull String word) {

        this.id = id;
        this.word = word;
        logins = new ArrayList<>();
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

    public ArrayList<String> getUserLogins() {
        return logins;
    }
}
