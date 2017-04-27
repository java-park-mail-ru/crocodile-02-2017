package socketmessages;

public enum PlayerRole {

    GUESSER,
    PAINTER,
    ANYONE;

    @Override
    public String toString() {

        if (this == GUESSER) {

            return "main";
        }

        return "other";
    }
}
