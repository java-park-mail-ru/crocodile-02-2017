package server;

import database.AccountServiceDb;
import database.DashesServiceDb;
import database.SingleplayerGamesServiceDb;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

@SuppressWarnings({"SpringJavaAutowiringInspection", "SpringFacetCodeInspection"})
@Configuration
public class ApplicationConfiguration {

    final Environment environment;

    @Autowired
    public ApplicationConfiguration(Environment environment) {
        this.environment = environment;
    }

    @Bean
    public AccountServiceDb accountService(NamedParameterJdbcTemplate database) {
        return new AccountServiceDb(database);
    }

    @Bean
    public DashesServiceDb dashesService(NamedParameterJdbcTemplate database) {
        return new DashesServiceDb(database);
    }

    @Bean
    public SingleplayerGamesServiceDb singleGameService(NamedParameterJdbcTemplate database) {
        return new SingleplayerGamesServiceDb(database);
    }

}

