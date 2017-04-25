package socketmessages;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@SuppressWarnings("unused")
@JsonIgnoreProperties(ignoreUnknown = true)
public class StopGameContent extends EmptyContent {

    public static final String RESULT_ATTR = "result";
    public static final String RATING_DELTA_ATTR = "score";

    private final GameResult result;
    private final int ratingDelta;

    public StopGameContent(GameResult result, int ratingDelta) {

        this.result = result;
        this.ratingDelta = ratingDelta;
    }

    @JsonProperty(RESULT_ATTR)
    public int getResult() {
        return result.asInt();
    }

    @JsonProperty(RATING_DELTA_ATTR)
    public int getRatingDelta() {
        return ratingDelta;
    }
}
