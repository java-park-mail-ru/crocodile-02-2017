package socketmessages;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
@JsonIgnoreProperties(ignoreUnknown = true)
public class PlayerDisconnectContent extends EmptyContent {

    public static final String PLAYER_ATTR = "player";

    private final @NotNull String player;

    public PlayerDisconnectContent(@NotNull String player) {

        this.player = player;
    }

    @JsonProperty(PLAYER_ATTR)
    public @NotNull String getPlayer() {
        return player;
    }
}
