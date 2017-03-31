package database;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import server.Account;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Service
public class AccountServiceDatabase implements AccountService {

    private static final String LOGIN_PARAM = "plogin";
    private static final String PASSWORD_HASH_PARAM = "ppasshash";
    private static final String EMAIL_PARAM = "pemail";
    private static final String RATING_DELTA_PARAM = "pratingdelta";
    private static final String OLD_LOGIN_PARAM = "poldlogin";
    private static final String LIMIT_PARAM = "plimit";

    private static final AccountRowMapper ACCOUNT_MAPPER = new AccountRowMapper();

    private NamedParameterJdbcTemplate database;

    public AccountServiceDatabase(NamedParameterJdbcTemplate database) {

        this.database = database;
    }

    private static class AccountRowMapper implements RowMapper<Account> {

        @Override
        public Account mapRow(ResultSet resultSet, int i) throws SQLException {

            return new Account(
                resultSet.getInt("id"),
                resultSet.getString("login"),
                resultSet.getString("passhash"),
                resultSet.getString("email"),
                resultSet.getInt("rating")
            );
        }
    }

    @Override
    public @NotNull Account createAccount(
                                             @NotNull String login,
                                             @NotNull String password,
                                             @NotNull String email) throws DataRetrievalFailureException {

        final MapSqlParameterSource source = new MapSqlParameterSource();

        source.addValue(LOGIN_PARAM, login);
        source.addValue(PASSWORD_HASH_PARAM, Account.hashPassword(password));
        source.addValue(EMAIL_PARAM, email);

        final String insertAccountSql = String.format(
            " INSERT INTO account" +
                " ( login, passhash, email )" +
                " VALUES ( :%1$s, :%2$s, :%3$s )" +
                " RETURNING *",
            LOGIN_PARAM, PASSWORD_HASH_PARAM, EMAIL_PARAM);

        final List<Account> result = database.query(insertAccountSql, source, ACCOUNT_MAPPER);
        if (result.size() != 1) {
            throw new DataRetrievalFailureException("Account creation error");
        }
        return result.get(0);
    }

    @Override
    public @Nullable Account findAccount(@Nullable String login) {

        final MapSqlParameterSource source = new MapSqlParameterSource();

        source.addValue(LOGIN_PARAM, login);

        final String selectAccountSql = String.format(
            " SELECT * FROM account" +
                " WHERE login = :%1$s",
            LOGIN_PARAM);

        final List<Account> result = database.query(selectAccountSql, source, ACCOUNT_MAPPER);
        return (result.size() == 1) ? result.get(0) : null;
    }

    @Override
    public @NotNull Account updateAccountInfo(
                                                 @NotNull String oldLogin,
                                                 @Nullable String login,
                                                 @Nullable String password,
                                                 @Nullable String email) throws DataRetrievalFailureException {

        final MapSqlParameterSource source = new MapSqlParameterSource();

        source.addValue(LOGIN_PARAM, login);
        source.addValue(PASSWORD_HASH_PARAM, Account.hashPassword(password));
        source.addValue(EMAIL_PARAM, email);
        source.addValue(OLD_LOGIN_PARAM, oldLogin);

        final String updateAccountSql = String.format(
            " UPDATE account" +
                " SET ( login, passhash, email ) =" +
                " ( COALESCE( :%1$s, login )," +
                " COALESCE( :%2$s, passhash )," +
                " COALESCE( :%3$s, email ) )" +
                " WHERE login = :%4$s" +
                " RETURNING *",
            LOGIN_PARAM, PASSWORD_HASH_PARAM, EMAIL_PARAM, OLD_LOGIN_PARAM);

        final List<Account> result = database.query(updateAccountSql, source, ACCOUNT_MAPPER);
        if (result.size() != 1) {
            throw new DataRetrievalFailureException("Account update error");
        }
        return result.get(0);
    }

    @Override
    public @NotNull Account updateAccountRating(@NotNull String login, int ratingDelta) {

        final MapSqlParameterSource source = new MapSqlParameterSource();

        source.addValue(LOGIN_PARAM, login);
        source.addValue(RATING_DELTA_PARAM, ratingDelta);

        final String updateAccountSql = String.format(
            " UPDATE account" +
                " SET rating = rating + :%1$s" +
                " WHERE login = :%2$s" +
                " RETURNING *",
            RATING_DELTA_PARAM, LOGIN_PARAM);

        final List<Account> result = database.query(updateAccountSql, source, ACCOUNT_MAPPER);
        if (result.size() != 1) {
            throw new DataRetrievalFailureException("Account update error");
        }
        return result.get(0);
    }

    @Override
    public boolean hasAccount(@Nullable String login) {
        return findAccount(login) != null;
    }

    @Override
    public List<Account> getBest() {

        final MapSqlParameterSource source = new MapSqlParameterSource();

        source.addValue(LIMIT_PARAM, BEST_COUNT);

        final String selectAccountSql = String.format(
            " SELECT * FROM account" +
                " ORDER BY rating DESC, login ASC" +
                " LIMIT :%1$s",
            LIMIT_PARAM);

        return database.query(selectAccountSql, source, ACCOUNT_MAPPER);
    }
}
