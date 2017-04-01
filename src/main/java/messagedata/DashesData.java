package messagedata;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import database.Dashes;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;

@SuppressWarnings("unused")
public class DashesData {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final TypeFactory TYPE_FACTORY = new ObjectMapper().getTypeFactory();

    private final Dashes dashes;

    public DashesData(@NotNull Dashes dashes) {

        this.dashes = dashes;
    }

    private static final class DrawingPoint {

        private static final String TIME_ATTR = "time";
        private static final String X_ATTR = "x";
        private static final String Y_ATTR = "y";

        private final int time;
        private final int pointX;
        private final int pointY;

        @JsonCreator
        DrawingPoint(
            @JsonProperty(TIME_ATTR) int time,
            @JsonProperty(X_ATTR) int pointX,
            @JsonProperty(Y_ATTR) int pointY) {

            this.time = time;
            this.pointX = pointX;
            this.pointY = pointY;
        }

        @JsonProperty(TIME_ATTR)
        public int getTime() {
            return time;
        }

        @JsonProperty(X_ATTR)
        public int getX() {
            return pointX;
        }

        @JsonProperty(Y_ATTR)
        public int getPointY() {
            return pointY;
        }
    }

    @JsonProperty("color")
    public String getColor() {
        return dashes.getColor();
    }

    @JsonProperty("word")
    public String getWord() {
        return dashes.getWord();
    }

    @SuppressWarnings("OverlyBroadThrowsClause")
    @JsonProperty("points")
    public List<DrawingPoint> getPoints() throws IOException {

        return new ObjectMapper()
            .readValue(dashes.getPointsJson(), TYPE_FACTORY.constructCollectionType(List.class, DrawingPoint.class));
    }
}
