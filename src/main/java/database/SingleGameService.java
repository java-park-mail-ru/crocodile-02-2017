package database;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.dao.DataAccessException;

public interface SingleGameService {

    @NotNull SingleGame createGame(@NotNull String login, int dashesId) throws DataAccessException;

    @Nullable SingleGame getGame(int gameId) throws DataAccessException;

    void shutdownGame(int gameId) throws DataAccessException;
}
