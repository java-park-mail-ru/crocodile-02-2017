package database;

import entities.MultiplayerGame;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MultiplayerGamesServiceDb implements MultiplayerGamesService {

    private static final String ID_ATTR = "pid";
    private static final String WORD_ATTR = "pword";
    private static final String USERS_ATTR = "pusers";

    private static final MultiplayerGameRowMapper MULTIPLAYER_GAME_MAPPER = new MultiplayerGameRowMapper();

    private final NamedParameterJdbcTemplate database;

    public MultiplayerGamesServiceDb(NamedParameterJdbcTemplate database) {
        this.database = database;
    }

    private static class MultiplayerGameRowMapper implements RowMapper<MultiplayerGame> {

        @Override
        public MultiplayerGame mapRow(ResultSet resultSet, int i) throws SQLException {

            return new MultiplayerGame(
                resultSet.getInt("id"),
                resultSet.getString("word"),
                Arrays.asList((String[]) resultSet.getArray("users").getArray()));
        }
    }

    @Override
    public @NotNull MultiplayerGame createGame(String word, ArrayList<String> logins) throws DataRetrievalFailureException {

        final MapSqlParameterSource source = new MapSqlParameterSource();
        source.addValue(WORD_ATTR, word);
        source.addValue(USERS_ATTR, logins);

        final String createGameSql = String.format(
            " With tempIns AS ( INSERT INTO multi_game" +
                " ( word, users )" +
                " VALUES ( :%1$s, ARRAY[ :%2$s ] )" +
                " RETURNING * )" +
                " SELECT tempIns.id, tempIns.word, tempIns.users FROM tempIns",
            WORD_ATTR, USERS_ATTR);

        final List<MultiplayerGame> result = database.query(createGameSql, source, MULTIPLAYER_GAME_MAPPER);
        if (result.size() != 1) {
            throw new DataRetrievalFailureException("multiplayer game creation error");
        }
        return result.get(0);
    }

    @Override
    public @Nullable MultiplayerGame getGame(int gameId) throws DataAccessException {

        final MapSqlParameterSource source = new MapSqlParameterSource();
        source.addValue(ID_ATTR, gameId);

        final String selectGameSql = String.format(
            " SELECT multi_game.id, multi_game.word, multi_game.users FROM multi_game" +
                " WHERE single_game.id = :%1$s",
            ID_ATTR);

        final List<MultiplayerGame> result = database.query(selectGameSql, source, MULTIPLAYER_GAME_MAPPER);
        return (result.size() == 1) ? result.get(0) : null;
    }

    @Override
    public void shutdownGame(int gameId) throws DataRetrievalFailureException {

        final MapSqlParameterSource source = new MapSqlParameterSource();
        source.addValue(ID_ATTR, gameId);

        final String deleteGameSql = String.format(
            " DELETE FROM multi_game" +
                " WHERE id = :%1$s",
            ID_ATTR);

        final int deletedCount = database.update(deleteGameSql, source);
        if (deletedCount == 0) {
            throw new DataRetrievalFailureException("multiplayer game shutdown error");
        }
    }
}
