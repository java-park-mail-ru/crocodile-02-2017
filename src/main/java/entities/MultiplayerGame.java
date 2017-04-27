package entities;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class MultiplayerGame extends BasicGame {

    private @NotNull ArrayList<String> logins;

    public MultiplayerGame(
        int id,
        @NotNull String word,
        @NotNull List<String> logins) {

        super(id, word);
        this.logins = new ArrayList<>(logins);
    }

    public @NotNull ArrayList<String> getUserLogins() {
        return logins;
    }
}
