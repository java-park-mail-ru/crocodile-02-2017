package socketmessages;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
@JsonIgnoreProperties(ignoreUnknown = true)
public class AnswerResponseContent extends EmptyContent {

    public static final String ID_ATTR = "id";
    public static final String ANSWER_ATTR = "answer";
    public static final String CORRECT_ATTR = "right";
    public static final String PLAYER_ATTR = "player";
    public static final String PLAYER_NUMBER_ATTR = "color";

    private final int id;
    private final @Nullable String answer;
    private final boolean correct;
    private final @NotNull PlayerInfo playerInfo;

    public AnswerResponseContent(int id, @Nullable String answer, boolean correct, @NotNull PlayerInfo playerInfo) {

        this.id = id;
        this.answer = answer;
        this.correct = correct;
        this.playerInfo = playerInfo;
    }

    @JsonProperty(ID_ATTR)
    public int getid() {
        return id;
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

    @JsonProperty(ANSWER_ATTR)
    public @Nullable String getAnswer() {
        return answer;
    }
}
