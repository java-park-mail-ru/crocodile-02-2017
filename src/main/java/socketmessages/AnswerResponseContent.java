package socketmessages;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
@JsonIgnoreProperties(ignoreUnknown = true)
public class AnswerResponseContent extends EmptyContent {

    public static final String CORRECT_ATTR = "right";
    public static final String PLAYER_ATTR = "player";
    public static final String PLAYER_NUMBER_ATTR = "color";

    private final boolean correct;
    private final @NotNull PlayerInfo playerInfo;

    public AnswerResponseContent(boolean correct, @NotNull PlayerInfo playerInfo) {

        this.correct = correct;
        this.playerInfo = playerInfo;
    }

    @JsonProperty(CORRECT_ATTR)
    public boolean isCorrect() {
        return correct;
    }

    @JsonProperty(PLAYER_ATTR)
    public @NotNull String getPlayerLogin() {
        return playerInfo.getLogin();
    }

    @JsonProperty(PLAYER_NUMBER_ATTR)
    public int getPlayerNumber() {
        return playerInfo.getPlayerNumber();
    }
}
