package database;

import org.jetbrains.annotations.NotNull;
import org.springframework.dao.DataAccessException;

public interface DashService {

    boolean checkWord(@NotNull String word, int id);

    void addUsedDashes(@NotNull String login, int id) throws DataAccessException;

    @NotNull Dashes getRandomDash(@NotNull String login) throws DataAccessException;
}
