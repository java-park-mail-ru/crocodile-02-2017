package database;

import entities.Dashes;
import entities.SingleplayerGame;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Service
public class SingleplayerGamesServiceDb implements SingleplayerGamesService {

    private static final String ID_ATTR = "pid";
    private static final String LOGIN_ATTR = "plogin";
    private static final String DASHES_ID_ATTR = "pdashesid";

    private static final SingleplayerGameRowMapper SINGLEPLAYER_GAME_MAPPER = new SingleplayerGameRowMapper();

    private final NamedParameterJdbcTemplate database;

    public SingleplayerGamesServiceDb(NamedParameterJdbcTemplate database) {
        this.database = database;
    }

    private static class SingleplayerGameRowMapper implements RowMapper<SingleplayerGame> {

        @Override
        public SingleplayerGame mapRow(ResultSet resultSet, int i) throws SQLException {

            final int dashesId = resultSet.getInt("dashesid");
            final String word = resultSet.getString("word");
            final String pointsJson = resultSet.getString("points");

            return new SingleplayerGame(
                resultSet.getInt("id"),
                resultSet.getString("login"),
                new Dashes(dashesId, word, pointsJson));
        }
    }

    @Override
    public @NotNull SingleplayerGame createGame(@NotNull String login, int dashesId) throws DataRetrievalFailureException {

        final MapSqlParameterSource source = new MapSqlParameterSource();
        source.addValue(LOGIN_ATTR, login);
        source.addValue(DASHES_ID_ATTR, dashesId);

        final String createGameSql = String.format(
            " With tempIns AS ( INSERT INTO single_game" +
                " ( accountid, dashesid )" +
                " SELECT account.id, :%1$s FROM account" +
                " WHERE login = :%2$s" +
                " RETURNING * )" +
                " SELECT tempIns.id, account.login, tempIns.dashesid, dashes.word, dashes.points FROM tempIns" +
                " JOIN account ON account.id = tempIns.accountid" +
                " JOIN dashes ON dashes.id = dashesid",
            DASHES_ID_ATTR, LOGIN_ATTR);

        final List<SingleplayerGame> result = database.query(createGameSql, source, SINGLEPLAYER_GAME_MAPPER);
        if (result.size() != 1) {
            throw new DataRetrievalFailureException("singleplayer game creation error");
        }
        return result.get(0);
    }

    @Override
    public @Nullable SingleplayerGame getGame(int gameId) throws DataAccessException {

        final MapSqlParameterSource source = new MapSqlParameterSource();
        source.addValue(ID_ATTR, gameId);

        final String selectGameSql = String.format(
            " SELECT single_game.id, account.login, single_game.dashesid, dashes.word, dashes.points FROM single_game" +
                " JOIN account ON account.id = accountid" +
                " JOIN dashes ON dashes.id = dashesid" +
                " WHERE single_game.id = :%1$s",
            ID_ATTR);

        final List<SingleplayerGame> result = database.query(selectGameSql, source, SINGLEPLAYER_GAME_MAPPER);
        return (result.size() == 1) ? result.get(0) : null;
    }

    @Override
    public void shutdownGame(int gameId) throws DataRetrievalFailureException {

        final MapSqlParameterSource source = new MapSqlParameterSource();
        source.addValue(ID_ATTR, gameId);

        final String deleteGameSql = String.format(
            " DELETE FROM single_game" +
                " WHERE id = :%1$s",
            ID_ATTR);

        final int deletedCount = database.update(deleteGameSql, source);
        if (deletedCount == 0) {
            throw new DataRetrievalFailureException("singleplayer game shutdown error");
        }
    }
}
