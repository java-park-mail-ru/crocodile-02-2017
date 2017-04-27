package socketmessages;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
@JsonIgnoreProperties(ignoreUnknown = true)
public class BaseGameContent extends EmptyContent {

    public static final String TYPE_ATTR = "type";
    public static final String TIME_PASSED_ATTR = "current_time";
    public static final String TIME_LIMIT_ATTR = "timer";

    protected final GameType gameType;
    protected final float timePassed;
    protected final float timeLimit;

    public BaseGameContent(@NotNull GameType gameType, float timePassed, float timeLimit) {

        this.gameType = gameType;
        this.timePassed = timePassed;
        this.timeLimit = timeLimit;
    }

    @JsonProperty(TYPE_ATTR)
    public String getType() {
        return gameType.toString();
    }

    @JsonProperty(TIME_PASSED_ATTR)
    public float getTimePassed() {
        return timePassed;
    }

    @JsonProperty(TIME_LIMIT_ATTR)
    public float getTimeLimit() {
        return timeLimit;
    }
}
