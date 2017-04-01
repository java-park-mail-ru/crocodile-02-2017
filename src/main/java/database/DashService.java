package database;

import org.jetbrains.annotations.NotNull;
import org.springframework.dao.DataAccessException;

public interface DashService {

    @NotNull Dashes getRandomDash(@NotNull String login) throws DataAccessException;
}
