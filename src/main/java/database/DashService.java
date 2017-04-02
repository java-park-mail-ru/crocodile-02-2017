package database;

import org.jetbrains.annotations.NotNull;
import org.springframework.dao.DataAccessException;

public interface DashService {

    void addUsedWord(@NotNull String login, @NotNull String word) throws DataAccessException;

    @NotNull Dashes getRandomDash(@NotNull String login) throws DataAccessException;
}
