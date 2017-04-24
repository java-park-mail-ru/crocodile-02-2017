package database;

import entities.SingleplayerGame;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.dao.DataAccessException;

public interface SingleplayerGamesService {

    @NotNull SingleplayerGame createGame(@NotNull String login, int dashesId) throws DataAccessException;

    @Nullable SingleplayerGame getGame(int gameId) throws DataAccessException;

    void shutdownGame(int gameId) throws DataAccessException;
}
