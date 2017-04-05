package database;

import org.jetbrains.annotations.NotNull;

public class Dashes {

    private final int id;
    private final @NotNull String word;
    private final @NotNull String points;

    public Dashes(
        int id,
        @NotNull String word,
        @NotNull String points) {

        this.id = id;
        this.word = word;
        this.points = points;
    }

    public int getId() {
        return id;
    }

    public @NotNull String getWord() {
        return word;
    }

    public @NotNull String getPointsJson() {
        return points;
    }
}
