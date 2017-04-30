package socketmessages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
@JsonIgnoreProperties(ignoreUnknown = true)
public class PicturePointContent extends EmptyContent {

    public static final String X_ATTR = "x";
    public static final String Y_ATTR = "y";
    public static final String DOWN_ATTR = "down";
    public static final String COLOR_ATTR = "color";

    private final float pointX;
    private final float pointY;
    private final boolean down;
    private final @Nullable String color;

    @JsonCreator
    public PicturePointContent(
        @JsonProperty(X_ATTR) float x,
        @JsonProperty(Y_ATTR) float y,
        @JsonProperty(DOWN_ATTR) boolean down,
        @Nullable @JsonProperty(COLOR_ATTR) String color) {

        this.pointX = x;
        this.pointY = y;
        this.down = down;
        this.color = color;
    }

    @JsonProperty(X_ATTR)
    public float getPointX() {
        return pointX;
    }

    @JsonProperty(Y_ATTR)
    public float getPointY() {
        return pointY;
    }

    @JsonProperty(DOWN_ATTR)
    public boolean getDown() {
        return down;
    }

    @JsonProperty(COLOR_ATTR)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public @Nullable String getColor() {
        return color;
    }
}
