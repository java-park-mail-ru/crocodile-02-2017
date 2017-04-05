package messagedata;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import database.Dashes;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

@SuppressWarnings("unused")
public class DashesData {

    public static final String POINTS_ATTR = "points";

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final Dashes dashes;

    public DashesData(@NotNull Dashes dashes) {
        this.dashes = dashes;
    }

    @SuppressWarnings("OverlyBroadThrowsClause")
    @JsonProperty(POINTS_ATTR)
    public JsonNode getPoints() throws IOException {
        return OBJECT_MAPPER.readTree(dashes.getPointsJson());
    }
}
