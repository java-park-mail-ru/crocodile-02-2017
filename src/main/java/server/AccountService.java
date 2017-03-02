package server;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
public class AccountService {
    
    private final @NotNull HashMap<String, Account> accounts;

    public AccountService() {
        accounts = new HashMap<String, Account>();
    }

    public @NotNull Account addAccount(
            @NotNull String login,
            @NotNull String password,
            @NotNull String email) {
        final Account account = new Account(login, password, email, this);
        accounts.put(login, account);
        return account;
    }

    public @Nullable Account find(int id) {

        for (Account account : accounts.values()) {

            if (account.getId() == id) {

                return account;
            }
        }

        return null;
    }

    public @Nullable Account find(String login) {

        return accounts.get(login);
    }

    public void updateKey(@NotNull String oldLogin, @NotNull Account account) {

        if (!account.getLogin().equals(oldLogin)) {

            accounts.remove(oldLogin);
            accounts.put(account.getLogin(), account);
        }
    }

    public boolean has(String login) {
        return (this.find(login) != null);
    }
}
