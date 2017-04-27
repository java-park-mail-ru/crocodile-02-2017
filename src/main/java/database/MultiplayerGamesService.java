package database;

import entities.MultiplayerGame;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public interface MultiplayerGamesService {

    @NotNull MultiplayerGame createGame(String word, ArrayList<String> logins);

    @Nullable MultiplayerGame getGame(int gameId);

    void shutdownGame(int gameId);
}
