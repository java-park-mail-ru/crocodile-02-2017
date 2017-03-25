package server;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SuppressWarnings({"unused", "ComparableImplementedButEqualsNotOverridden"})
public class Account implements Comparable<Account> {

    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();

    private final int id;
    private @NotNull String login;
    private @NotNull String passwordHash;
    private @NotNull String email;
    private int rating;

    @Contract("!null->!null")
    public static @Nullable String hashPassword(@Nullable String password) {
        return (password != null) ? ENCODER.encode(password) : null;
    }

    public Account(
        int id,
        @NotNull String login,
        @NotNull String passwordHash,
        @NotNull String email,
        int rating) {
        this.id = id;
        this.login = login;
        this.passwordHash = passwordHash;
        this.email = email;
        this.rating = rating;
    }

    public int getId() {
        return id;
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
    public void setProperties(
        @Nullable String login,
        @Nullable String passwordHash,
        @Nullable String email,
        @Nullable Integer rating) {

        if (login != null) {
            this.login = login;
        }
        if (passwordHash != null) {
            this.passwordHash = passwordHash;
        }
        if (email != null) {
            this.email = email;
        }

        if (rating != null) {
            this.rating = rating;
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
        return this.rating == other.rating ?
            this.login.compareTo(other.login) :
            other.rating - this.rating;
    }
}
