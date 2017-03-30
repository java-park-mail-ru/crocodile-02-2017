package server;

import database.AccountServiceDatabase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

@Configuration
public class ApplicationConfiguration {

    final Environment environment;

    @Autowired
    public ApplicationConfiguration(Environment environment) {
        this.environment = environment;
    }

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Bean
    public AccountServiceDatabase serviceDatabase(NamedParameterJdbcTemplate database) {
        return new AccountServiceDatabase(database);

    }
}

