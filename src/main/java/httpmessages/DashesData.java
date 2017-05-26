package httpmessages;

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
public class DashesData {

    public static final String WORD_ATTR = "word";
    public static final String POINTS_ATTR = "points";

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final String word;
    private final JsonNode dashesJson;

    @SuppressWarnings("OverlyBroadThrowsClause")
    public DashesData(@NotNull Dashes dashes) throws IOException {

        this.word = dashes.getWord();
        this.dashesJson = OBJECT_MAPPER.readTree(dashes.getPointsJson());
    }

    @JsonProperty(WORD_ATTR)
    public String getWord() {
        return word;
    }

    @JsonProperty(POINTS_ATTR)
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public JsonNode getPoints() {
        return dashesJson;
    }
}
