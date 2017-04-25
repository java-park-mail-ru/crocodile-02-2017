package socketmessages;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@SuppressWarnings("unused")
@JsonIgnoreProperties(ignoreUnknown = true)
public class AnswerResponseContent extends EmptyContent {

    public static final String CORRECT_ATTR = "right";

    private final boolean correct;

    public AnswerResponseContent(boolean correct) {

        this.correct = correct;
    }

    @JsonProperty(CORRECT_ATTR)
    public boolean isCorrect() {
        return correct;
    }
}
