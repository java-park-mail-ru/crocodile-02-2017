package socketmessages;

public enum GameResult {

    GAME_LOST(0),
    GAME_WON(1);

    private final int intRepresentation;

    GameResult(int number) {
        this.intRepresentation = number;
    }

    public int asInt() {
        return intRepresentation;
    }
}
