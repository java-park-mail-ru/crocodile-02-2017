package socketmessages;

public enum PlayerRole {

    GUESSER,
    PAINTER,
    ANYONE;

    @Override
    public String toString() {

        if (this == PAINTER) {

            return "main";
        }

        return "other";
    }
}
