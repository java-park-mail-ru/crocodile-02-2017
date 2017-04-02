package database;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Random;

@Service
public class DashServiceDatabase implements DashService {

    private static final String LOGIN_PARAM = "plogin";
    private static final String WORD_PARAM = "pword";

    private static final Random RANDOM = new Random();
    private static final DashRowMapper DASH_MAPPER = new DashRowMapper();

    private final NamedParameterJdbcTemplate database;

    @Autowired
    public DashServiceDatabase(NamedParameterJdbcTemplate database) {
        this.database = database;
    }

    private static class DashRowMapper implements RowMapper<Dashes> {

        @Override
        public Dashes mapRow(ResultSet resultSet, int i) throws SQLException {

            return new Dashes(
                resultSet.getString("color"),
                resultSet.getString("word"),
                resultSet.getString("points"));
        }
    }

    @Override
    public void addUsedWord(@NotNull String login, @NotNull String word) throws DataRetrievalFailureException {

        final MapSqlParameterSource source = new MapSqlParameterSource();
        source.addValue(WORD_PARAM, word);
        source.addValue(LOGIN_PARAM, login);

        final String insertDashesRecordText = String.format(
            " INSERT INTO account_dashes ( accountid, dashesid )" +
                " SELECT account.id, dashes.id from account" +
                " JOIN dashes ON dashes.word = :%1$s" +
                " WHERE login = :%2$s",
            WORD_PARAM, LOGIN_PARAM);

        final int insertsCount = database.update(insertDashesRecordText, source);
        if (insertsCount != 1) {
            throw new DataRetrievalFailureException("Used words insertion error");
        }
    }

    @Override
    public @NotNull Dashes getRandomDash(@NotNull String login) throws DataRetrievalFailureException {

        if (checkAllWordsUsed(login)) {
            removeUsedWords(login);
        }

        final MapSqlParameterSource source = new MapSqlParameterSource();
        source.addValue(LOGIN_PARAM, login);

        final String selectDashSql = String.format(
            " SELECT * FROM dashes" +
                " WHERE dashes.id NOT IN (" +
                " SELECT dashesid FROM account_dashes" +
                " JOIN account ON account.login = :%1$s AND account.id = accountid )",
            LOGIN_PARAM);

        final List<Dashes> result = database.query(selectDashSql, source, DASH_MAPPER);
        if (result.isEmpty()) {
            throw new DataRetrievalFailureException("Dashes retrieval error");
        }
        return result.get(RANDOM.nextInt(result.size()));
    }

    private boolean checkAllWordsUsed(@NotNull String login) {

        final MapSqlParameterSource source = new MapSqlParameterSource();
        source.addValue(LOGIN_PARAM, login);

        final String checkWordsSql = String.format(
            " SELECT count(*) FROM dashes" +
                " WHERE dashes.id NOT IN (" +
                " SELECT dashesid FROM account_dashes" +
                " JOIN account ON account.login = :%1$s AND account.id = accountid )",
            LOGIN_PARAM);

        final int result = database.queryForObject(checkWordsSql, source, Integer.class);
        return result == 0;
    }

    private void removeUsedWords(@NotNull String login) {

        final MapSqlParameterSource source = new MapSqlParameterSource();
        source.addValue(LOGIN_PARAM, login);

        final String checkWordsSql = String.format(
            " DELETE FROM account_dashes USING account" +
                " WHERE account.login = :%1$s",
            LOGIN_PARAM);

        database.update(checkWordsSql, source);
    }
}
