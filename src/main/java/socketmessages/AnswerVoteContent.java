package socketmessages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;


@SuppressWarnings("unused")
@JsonIgnoreProperties(ignoreUnknown = true)
public class AnswerVoteContent extends EmptyContent {

    public static final String ID_ATTR = "id";
    public static final String VOTE_ATTR = "vote";

    private final int id;
    private final boolean vote;

    @JsonCreator
    public AnswerVoteContent(
        @JsonProperty(ID_ATTR) int id,
        @JsonProperty(VOTE_ATTR) boolean vote) {

        this.id = id;
        this.vote = vote;
    }

    @JsonProperty(ID_ATTR)
    public int getId() {
        return id;
    }

    @JsonProperty(VOTE_ATTR)
    public boolean isVotePositive() {
        return vote;
    }
}
