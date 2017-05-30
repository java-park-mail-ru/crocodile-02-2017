package entities;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MultiplayerGame extends BasicGame {


    public MultiplayerGame(
        int id,
        @NotNull String word,
        @NotNull List<String> logins) {

        super(id, word);
        this.logins.addAll(logins);
    }
}
