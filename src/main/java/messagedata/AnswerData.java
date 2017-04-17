package messagedata;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public class AnswerData {

    public static final String WORD_ATTR = "word";

    private final String word;

    public AnswerData(@Nullable @JsonProperty(WORD_ATTR) String word) {
        this.word = word;
    }

    @JsonProperty(WORD_ATTR)
    public @Nullable String getWord() {
        return word;
    }
}
