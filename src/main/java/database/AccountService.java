package database;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import server.Account;

import java.util.List;

public interface AccountService {

    int BEST_COUNT = 10;

    @Nullable Account createAccount(
        @NotNull String login,
        @NotNull String password,
        @NotNull String email);

    @Nullable Account findAccount(@Nullable String login);

    @Nullable Account updateAccount(@NotNull String oldLogin,
                                    @Nullable String login,
                                    @Nullable String password,
                                    @Nullable String email,
                                    @Nullable Integer rating);

    boolean hasAccount(String login);

    List<Account> getBest();
}
