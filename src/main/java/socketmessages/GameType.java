package socketmessages;

import org.jetbrains.annotations.NotNull;

public enum GameType {

    SINGLEPLAYER("sp"),
    MULTIPLAYER("mp");

    private final String type;

    GameType(String type) {

        this.type = type;
    }

    @Override
    public @NotNull String toString() {
        return type;
    }
}
