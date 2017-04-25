package socketmessages;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import entities.Dashes;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

@SuppressWarnings("unused")
@JsonIgnoreProperties(ignoreUnknown = true)
public class SingleplayerGameStateContent extends BaseGameContent {

    public static final String POINTS_ATTR = "points";
    public static final String TIME_PASSED_ATTR = "current_time";
    public static final String TIME_LIMIT_ATTR = "timer";

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final String dashesJson;

    @SuppressWarnings("OverlyBroadThrowsClause")
    public SingleplayerGameStateContent(@NotNull Dashes dashes, float timePassed, float timeLimit) throws IOException {

        super(GameType.SINGLEPLAYER, timePassed, timeLimit);
        this.dashesJson = OBJECT_MAPPER.readTree(dashes.getPointsJson()).asText();
    }


    @JsonProperty(POINTS_ATTR)
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public String getPoints() throws IOException {
        return dashesJson;
    }
}
