package socketmessages;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

@SuppressWarnings("unused")
public class PlayerConnectContent extends EmptyContent {

    public static final String PLAYERS_ATTR = "players";

    private final ArrayList<PlayerInfo> players;

    public PlayerConnectContent(@NotNull ArrayList<PlayerInfo> players) {

        this.players = players;
    }

    @JsonProperty(PLAYERS_ATTR)
    public @NotNull ArrayList<PlayerInfo> getPlayers() {
        return players;
    }
}
