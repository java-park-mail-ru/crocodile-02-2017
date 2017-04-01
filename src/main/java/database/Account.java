package database;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class Account {

    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();

    private final int id;
    private final @NotNull String login;
    private final @NotNull String passwordHash;
    private final @NotNull String email;
    private final int rating;

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
