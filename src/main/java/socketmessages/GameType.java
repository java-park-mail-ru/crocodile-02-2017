package socketmessages;

public enum GameType {

    SINGLEPLAYER("sp"),
    MULTIPLAYER("mp");

    private String type;

    GameType(String type) {

        this.type = type;
    }

    @Override
    public String toString() {
        return type;
    }
}
