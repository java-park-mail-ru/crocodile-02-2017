package messagedata;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import database.Account;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public class AccountData {

    public static final String LOGIN_ATTR = "login";
    public static final String PASSWORD_ATTR = "password";
    public static final String EMAIL_ATTR = "email";
    public static final String RATING_ATTR = "rating";

    private final String login;
    private final String password;
    private final String email;
    private final int rating;

    @JsonCreator
    public AccountData(
        @Nullable @JsonProperty(LOGIN_ATTR) String login,
        @Nullable @JsonProperty(PASSWORD_ATTR) String password,
        @Nullable @JsonProperty(EMAIL_ATTR) String email) {

        this.login = login;
        this.password = password;
        this.email = email;
        this.rating = 0;
    }

    public AccountData(@NotNull Account account) {

        this.login = account.getLogin();
        this.password = null;
        this.email = account.getEmail();
        this.rating = account.getRating();
    }

    @JsonProperty(value = LOGIN_ATTR)
    public String getLogin() {
        return login;
    }

    @JsonIgnore
    public boolean hasLogin() {
        return (login != null);
    }

    @JsonProperty(value = PASSWORD_ATTR)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getPassword() {
        return password;
    }

    @JsonIgnore
    public boolean hasPassword() {
        return (password != null);
    }

    @JsonProperty(value = EMAIL_ATTR)
    public String getEmail() {
        return email;
    }

    @JsonIgnore
    public boolean hasEmail() {
        return (email != null);
    }

    @JsonProperty(value = RATING_ATTR)
    public int getRating() {
        return rating;
    }
}
