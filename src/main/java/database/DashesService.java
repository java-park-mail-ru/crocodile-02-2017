package database;

import entities.Dashes;
import org.jetbrains.annotations.NotNull;
import org.springframework.dao.DataAccessException;

public interface DashesService {

    boolean checkWord(@NotNull String word, int dashesId);

    void addUsedDashes(@NotNull String login, int dashesId) throws DataAccessException;

    @NotNull Dashes getRandomDash(@NotNull String login) throws DataAccessException;
}
