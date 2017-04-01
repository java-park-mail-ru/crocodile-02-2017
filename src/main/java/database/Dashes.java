package database;

import org.jetbrains.annotations.NotNull;

public class Dashes {

    private final @NotNull String color;
    private final @NotNull String word;
    private final @NotNull String points;

    public Dashes(
        @NotNull String color,
        @NotNull String word,
        @NotNull String points) {

        this.color = color;
        this.word = word;
        this.points = points;
    }

    public @NotNull String getWord() {
        return word;
    }

    public @NotNull String getColor() {
        return color;
    }

    public @NotNull String getPointsJson() {
        return points;
    }
}
