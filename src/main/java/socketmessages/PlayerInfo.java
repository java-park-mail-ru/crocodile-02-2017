package socketmessages;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;

public class PlayerInfo {

    public static final String LOGIN_ATTR = "login";
    public static final String NUMBER_ATTR = "color";

    private final @NotNull String login;
    private final int playerNumber;

    public PlayerInfo(@NotNull String login, int playerNumber) {

        this.login = login;
        this.playerNumber = playerNumber;
    }

    @JsonProperty(LOGIN_ATTR)
    public @NotNull String getLogin() {
        return login;
    }

    @JsonProperty(NUMBER_ATTR)
    public int getPlayerNumber() {
        return playerNumber;
    }
}
