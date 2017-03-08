package server;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public class AccountData {

    private static final String LOGIN_ATTR = "login";
    private static final String PASSWORD_ATTR = "password";
    private static final String EMAIL_ATTR = "email";

    private final String login;
    private final String password;
    private final String email;
    private int rating;

    @JsonCreator
    AccountData(
            @JsonProperty(LOGIN_ATTR) String login,
            @JsonProperty(PASSWORD_ATTR) String password,
            @JsonProperty(EMAIL_ATTR) String email) {

        this.login = login;
        this.password = password;
        this.email = email;
        this.rating = 0;
    }

    AccountData(@NotNull Account account) {

        this.login = account.getLogin();
        this.password = null;
        this.email = account.getEmail();
        this.rating = account.getRating();
    }

    public String getLogin() {
        return login;
    }

    public boolean hasLogin() {
        return (login != null);
    }

    String getPassword() {
        return password;
    }

    public boolean hasPassword() {
        return (password != null);
    }

    public String getEmail() {
        return email;
    }

    public boolean hasEmail() {
        return (email != null);
    }

    public int getRating() {
        return rating;
    }
}
