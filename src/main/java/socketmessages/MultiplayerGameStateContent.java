package socketmessages;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@SuppressWarnings("unused")
public class MultiplayerGameStateContent extends BaseGameContent {

    public static final String ROLE_ATTR = "role";
    public static final String PLAYERS_ATTR = "players";
    public static final String WORD_ATTR = "word";

    private final PlayerRole role;
    private final ArrayList<String> players;
    private final String word;

    public MultiplayerGameStateContent(
        float timePassed,
        float timeLimit,
        PlayerRole role,
        ArrayList<String> players,
        @Nullable String word
    ) {
        super(GameType.MULTIPLAYER, timePassed, timeLimit);
        this.role = role;
        this.players = players;
        this.word = (role == PlayerRole.PAINTER) ? word : null;
    }

    @JsonProperty(ROLE_ATTR)
    public String getRole() {
        return role.toString();
    }

    @JsonProperty(PLAYERS_ATTR)
    public ArrayList<String> getPlayers() {
        return players;
    }

    @JsonInclude(NON_NULL)
    @JsonProperty(WORD_ATTR)
    public String getWord() {
        return word;
    }
}
