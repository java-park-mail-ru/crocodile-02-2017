package database;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;
import server.Account;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("unused")
@Service
public class AccountServiceHashmap implements AccountService {

    private static final int BEST_COUNT = 10;
    private static final AtomicInteger ID_GENERATOR = new AtomicInteger(0);

    private final @NotNull Map<String, Account> accounts;

    public AccountServiceHashmap() {
        accounts = new HashMap<>();
    }

    @Override
    public @Nullable Account createAccount(
        @NotNull String login,
        @NotNull String password,
        @NotNull String email) {
        final Account account = new Account(
            ID_GENERATOR.getAndIncrement(),
            login, Account.hashPassword(password),
            email, 0);
        accounts.put(login, account);
        return account;
    }

    @Override
    public @Nullable Account findAccount(String login) {
        return accounts.get(login);
    }

    @Override
    public @Nullable Account updateAccount(
        @NotNull String oldLogin,
        @Nullable String login,
        @Nullable String passwordHash,
        @Nullable String email,
        @Nullable Integer rating) {

        final Account account = accounts.get(oldLogin);
        account.setProperties(login, passwordHash, email, rating);

        if ((login != null) && !oldLogin.equals(login)) {

            accounts.remove(oldLogin);
            accounts.put(login, account);
            return accounts.get(login);
        }

        return accounts.get(oldLogin);
    }

    @Override
    public boolean hasAccount(String login) {
        return (this.findAccount(login) != null);
    }

    @Override
    public SortedSet<Account> getBest() {

        final TreeSet<Account> bestPlayers = new TreeSet<>(accounts.values());
        while (bestPlayers.size() > BEST_COUNT) {

            bestPlayers.remove(bestPlayers.last());
        }

        return bestPlayers;
    }

    @Override
    public void clear() {
        accounts.clear();
    }
}
