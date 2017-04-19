package database;

import org.jetbrains.annotations.NotNull;

public class Dashes {

    private final int id;
    private final @NotNull String word;
    private final @NotNull String pointsJson;

    public Dashes(
        int id,
        @NotNull String word,
        @NotNull String pointsJson) {

        this.id = id;
        this.word = word;
        this.pointsJson = pointsJson;
    }

    public int getId() {
        return id;
    }

    public @NotNull String getWord() {
        return word;
    }

    public @NotNull String getPointsJson() {
        return pointsJson;
    }
}
