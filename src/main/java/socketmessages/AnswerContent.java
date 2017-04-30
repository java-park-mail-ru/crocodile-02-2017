package socketmessages;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
@JsonIgnoreProperties(ignoreUnknown = true)
public class AnswerContent extends EmptyContent {

    public static final String WORD_ATTR = "answer";

    private final @Nullable String word;

    public AnswerContent(
        @Nullable @JsonProperty(WORD_ATTR) String word) {

        this.word = word;
    }

    @JsonProperty(WORD_ATTR)
    public @Nullable String getWord() {
        return word;
    }
}
