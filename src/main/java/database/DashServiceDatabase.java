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

@Service
public class DashServiceDatabase implements DashService {

    private static final String LOGIN_PARAM = "plogin";

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
    public @NotNull Dashes getRandomDash(@NotNull String login) throws DataRetrievalFailureException {

        final MapSqlParameterSource source = new MapSqlParameterSource();
        source.addValue(LOGIN_PARAM, login);

        //todo update this method
        final String selectDashSql = String.format(
            "SELECT * from dash",
            LOGIN_PARAM);


        final List<Dashes> result = database.query(selectDashSql, source, DASH_MAPPER);
        if (result.size() != 1) {
            throw new DataRetrievalFailureException("Account creation error");
        }
        return result.get(0);
    }
}
