package server;

import org.jetbrains.annotations.NotNull;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings({"unused", "ComparableImplementedButEqualsNotOverridden"})
public class Account implements Comparable<Account> {

    private static final AtomicInteger ID_GENERATOR = new AtomicInteger(0);
    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();

    private final int id;
    private @NotNull String login;
    private @NotNull String passwordHash;
    private @NotNull String email;
    private int rating;
    private final @NotNull AccountService database;

    public Account(
            @NotNull String login,
            @NotNull String password,
            @NotNull String email,
            @NotNull AccountService database) {
        this.id = ID_GENERATOR.getAndIncrement();
        this.login = login;
        this.passwordHash = ENCODER.encode(password);
        this.email = email;
        this.database = database;
        this.rating = 0;
    }

    public int getId() {
        return id;
    }

    public void setLogin(@NotNull String login) {

        final String oldLogin = this.login;
        this.login = login;
        database.updateKey(oldLogin, this);
    }

    public @NotNull String getLogin() {
        return login;
    }

    public void setPasswordHash(@NotNull String password) {
        this.passwordHash = ENCODER.encode(password);
    }

    public @NotNull String getPasswordHash() {
        return passwordHash;
    }

    public void setEmail(@NotNull String email) {
        this.email = email;
    }

    public @NotNull String getEmail() {
        return email;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public int getRating() {
        return rating;
    }

    //changes properties if they are not null
    public void setProperties(String login, String password, String email) {

        if (login != null) {
            setLogin(login);
        }
        if (password != null) {
            this.passwordHash = password;
        }
        if (email != null) {
            this.email = email;
        }
    }

    public boolean passwordMatches(String password) {
        return ENCODER.matches(password, passwordHash);
    }

    @Override
    public String toString() {
        return login + ": " + email;
    }

    @Override
    public int compareTo(@NotNull Account other) {
        return this.rating - other.rating;
    }
}
