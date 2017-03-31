package server;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@SuppressWarnings("unused")
public class RatingChangeData {

    public static final String LOGIN_ATTR = "login";
    public static final String RATING_DELTA_ATTR = "delta";

    private final String login;
    private final int ratingDelta;

    @JsonCreator
    public RatingChangeData(
                               @JsonProperty(LOGIN_ATTR) String login,
                               @JsonProperty(RATING_DELTA_ATTR) int ratingDelta) {

        this.login = login;
        this.ratingDelta = ratingDelta;
    }

    public String getLogin() {
        return login;
    }

    public int getRatingDelta() {
        return ratingDelta;
    }
}
