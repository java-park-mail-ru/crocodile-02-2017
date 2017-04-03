package server;

import database.AccountServiceDatabase;
import database.DashServiceDatabase;
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
    public AccountServiceDatabase accountService(NamedParameterJdbcTemplate database) {
        return new AccountServiceDatabase(database);
    }

    @Bean
    public DashServiceDatabase dashService(NamedParameterJdbcTemplate database) {
        return new DashServiceDatabase(database);
    }

}

