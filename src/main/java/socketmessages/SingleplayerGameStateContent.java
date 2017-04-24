package socketmessages;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import entities.Dashes;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

@SuppressWarnings("unused")
@JsonIgnoreProperties(ignoreUnknown = true)
public class SingleplayerGameStateContent extends EmptyContent {

    public static final String POINTS_ATTR = "points";
    public static final String TIME_PASSED_ATTR = "current_time";
    public static final String TIME_LIMIT_ATTR = "timer";

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final Dashes dashes;
    private final float timePassed;
    private final float timeLimit;

    public SingleplayerGameStateContent(@NotNull Dashes dashes, float timePassed, float timeLimit) {

        //super(type);
        this.dashes = dashes;
        this.timePassed = timePassed;
        this.timeLimit = timeLimit;
    }

    @SuppressWarnings("OverlyBroadThrowsClause")
    @JsonProperty(POINTS_ATTR)
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public JsonNode getPoints() throws IOException {
        return OBJECT_MAPPER.readTree(dashes.getPointsJson());
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
