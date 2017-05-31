package websocket;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.web.socket.WebSocketSession;
import socketmessages.GameType;
import socketmessages.PlayerRole;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class GameRelationManager {

    private final Map<String, GameRelation> relatedGames = new ConcurrentHashMap<>();

    public static final class GameRelation {

        private final int gameId;
        private final GameType type;
        private final PlayerRole role;
        private final WebSocketSession session;
        private final int playerNumber;
        
        GameRelation(int gameId, GameType gameType, WebSocketSession session, PlayerRole role, int playerNumber) {

            this.gameId = gameId;
            this.type = gameType;
            this.role = role;
            this.session = session;
            this.playerNumber = playerNumber;
        }

        public int getGameId() {
            return gameId;
        }

        public GameType getType() {
            return type;
        }

        public PlayerRole getRole() {
            return role;
        }

        public WebSocketSession getSession() {
            return session;
        }

        public int getPlayerNumber() {
            return playerNumber;
        }
    }

    public void addGuesserRelation(
        @NotNull WebSocketSession session, @NotNull ScheduledGame scheduledGame, int playerNumber) {

        final GameRelation gameRelation = new GameRelation(
            scheduledGame.getGame().getId(),
            scheduledGame.getType(),
            session,
            PlayerRole.GUESSER,
            playerNumber);

        relatedGames.put(SessionOperator.getLogin(session), gameRelation);
    }

    public void addPainterRelation(
        @NotNull WebSocketSession session, @NotNull ScheduledGame scheduledGame, int playerNumber) {

        final GameRelation gameRelation = new GameRelation(
            scheduledGame.getGame().getId(),
            scheduledGame.getType(),
            session,
            PlayerRole.PAINTER,
            playerNumber);

        relatedGames.put(SessionOperator.getLogin(session), gameRelation);
    }

    public ArrayList<WebSocketSession> getGameSessions(@NotNull ScheduledGame scheduledGame) {

        final ArrayList<WebSocketSession> sessions = new ArrayList<>();

        final ArrayList<String> logins = scheduledGame.getGame().getUserLogins();
        sessions.addAll(relatedGames.entrySet().stream()
            .filter(e -> logins.contains(e.getKey()))
            .map(e -> e.getValue().getSession())
            .collect(Collectors.toList()));

        return sessions;
    }

    public GameRelation getRelation(@Nullable String login) {

        return relatedGames.get(login);
    }

    public void removeRelation(@Nullable String login) {

        relatedGames.remove(login);
    }
}
