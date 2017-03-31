package database;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.dao.DataAccessException;
import server.Account;

import java.util.List;

public interface AccountService {

    int BEST_COUNT = 10;

    @NotNull Account createAccount(
                                      @NotNull String login,
                                      @NotNull String password,
                                      @NotNull String email) throws DataAccessException;

    @Nullable Account findAccount(@Nullable String login);

    @NotNull Account updateAccountInfo(@NotNull String oldLogin,
                                       @Nullable String login,
                                       @Nullable String password,
                                       @Nullable String email) throws DataAccessException;

    @NotNull Account updateAccountRating(@NotNull String login, int ratingDelta);

    boolean hasAccount(String login);

    List<Account> getBest();
}
