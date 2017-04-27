package server;

import database.AccountServiceDb;
import database.DashesServiceDb;
import database.MultiplayerGamesServiceDb;
import database.SingleplayerGamesServiceDb;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.handler.PerConnectionWebSocketHandler;
import websocket.GameManagerService;
import websocket.GameSocketHandler;
import websocket.WebSocketMessageHandler;

@SuppressWarnings("SpringJavaAutowiringInspection")
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

    @Bean
    public MultiplayerGamesServiceDb multiplayerGamesService(NamedParameterJdbcTemplate database) {
        return new MultiplayerGamesServiceDb(database);
    }

    @Bean
    public WebSocketMessageHandler webSocketMessageHandler() {
        return new WebSocketMessageHandler();
    }

    @Bean
    public GameManagerService gameManagerService(AccountServiceDb accountServiceDb, DashesServiceDb dashesService, SingleplayerGamesServiceDb singleplayerGamesService, MultiplayerGamesServiceDb multiplayerGamesService) {
        return new GameManagerService(accountServiceDb, dashesService, singleplayerGamesService, multiplayerGamesService);
    }

    @Bean
    public WebSocketHandler gameWebSocketHandler() {
        return new PerConnectionWebSocketHandler(GameSocketHandler.class);
    }

}

