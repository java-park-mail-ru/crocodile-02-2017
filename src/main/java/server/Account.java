package server;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class Account {

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

    public @NotNull String getPasswordHash() {
        return passwordHash;
    }

    public @NotNull String getEmail() {
        return email;
    }

    public int getRating() {
        return rating;
    }

    public boolean passwordMatches(String password) {
        return ENCODER.matches(password, passwordHash);
    }
}
