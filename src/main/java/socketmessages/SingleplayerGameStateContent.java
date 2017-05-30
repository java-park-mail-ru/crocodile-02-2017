package socketmessages;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import entities.Dashes;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@SuppressWarnings("unused")
@JsonIgnoreProperties(ignoreUnknown = true)
public class SingleplayerGameStateContent extends BaseGameContent {

    private static final Logger LOGGER = LoggerFactory.getLogger(SingleplayerGameStateContent.class);

    public static final String POINTS_ATTR = "points";
    public static final String TIME_PASSED_ATTR = "current_time";

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private JsonNode dashesJson;

    @SuppressWarnings("OverlyBroadCatchBlock")
    public SingleplayerGameStateContent(@NotNull Dashes dashes, float timePassed, float timeLimit) {

        super(GameType.SINGLEPLAYER, timePassed, timeLimit);
        try {
            this.dashesJson = OBJECT_MAPPER.readTree(dashes.getPointsJson());
        } catch (IOException exception) {

            this.dashesJson = null;
            LOGGER.error("CREATION OF SINGLEPLAYER STATE CONTENT FAILED FOR DASHED {} - BAD JSON", dashes.getId());
        }

    }

    @JsonProperty(POINTS_ATTR)
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public JsonNode getPoints() {
        return dashesJson;
    }
}
