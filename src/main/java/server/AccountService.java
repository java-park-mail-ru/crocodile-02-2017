package server;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

@Service
public class AccountService {

    private static final int BEST_COUNT = 10;

    private final @NotNull Map<String, Account> accounts;

    public AccountService() {
        accounts = new HashMap<>();
    }

    public @NotNull Account createAccount(
            @NotNull String login,
            @NotNull String password,
            @NotNull String email) {
        final Account account = new Account(login, password, email, this);
        accounts.put(login, account);
        return account;
    }

    public @Nullable Account find(String login) {
        return accounts.get(login);
    }

    public boolean has(String login) {
        return (this.find(login) != null);
    }

    public SortedSet<Account> getBest() {

        final TreeSet<Account> bestPlayers = new TreeSet<>(accounts.values());
        while (bestPlayers.size() > BEST_COUNT) {

            bestPlayers.remove(bestPlayers.last());
        }

        return bestPlayers;
    }

    public void updateKey(@NotNull String oldLogin, @NotNull Account account) {

        if (!account.getLogin().equals(oldLogin)) {

            accounts.remove(oldLogin);
            accounts.put(account.getLogin(), account);
        }
    }

    public void clear() {
        accounts.clear();
    }
}
