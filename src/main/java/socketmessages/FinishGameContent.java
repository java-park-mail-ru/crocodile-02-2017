package socketmessages;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
@JsonIgnoreProperties(ignoreUnknown = true)
public class FinishGameContent extends EmptyContent {

    public static final String RESULT_ATTR = "result";
    public static final String RATING_DELTA_ATTR = "score";
    public static final String WINNER_ATTR = "winner";
    public static final String WORD_ATTR = "word";

    private final @NotNull GameResult result;
    private final int ratingDelta;
    private final @Nullable String winner;
    private final @NotNull String word;

    public FinishGameContent(@NotNull GameResult result, int ratingDelta, @Nullable String winner, @NotNull String word) {

        this.result = result;
        this.ratingDelta = ratingDelta;
        this.winner = winner;
        this.word = word;
    }

    @JsonProperty(RESULT_ATTR)
    public int getResult() {
        return result.asInt();
    }

    @JsonProperty(RATING_DELTA_ATTR)
    public int getRatingDelta() {
        return ratingDelta;
    }

    @JsonProperty(WINNER_ATTR)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public @Nullable String getWinner() {
        return winner;
    }

    @JsonProperty(WORD_ATTR)
    public @NotNull String getWord() {
        return word;
    }
}
