package server;

import database.AccountServiceDb;
import database.DashesServiceDb;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.handler.PerConnectionWebSocketHandler;
import websocket.GameManagerService;
import websocket.GameSocketHandler;
import websocket.WebSocketMessageHandler;

@SuppressWarnings("SpringJavaAutowiringInspection")
@Configuration
public class ApplicationConfiguration {

    @Bean
    public AccountServiceDb accountService(NamedParameterJdbcTemplate database) {
        return new AccountServiceDb(database);
    }

    @Bean
    public DashesServiceDb dashesService(NamedParameterJdbcTemplate database) {
        return new DashesServiceDb(database);
    }

    @Bean
    public WebSocketMessageHandler webSocketMessageHandler() {
        return new WebSocketMessageHandler();
    }

    @Bean
    public GameManagerService gameManagerService(AccountServiceDb accountServiceDb, DashesServiceDb dashesService) {
        return new GameManagerService(accountServiceDb, dashesService);
    }

    @Bean
    public WebSocketHandler gameWebSocketHandler() {
        return new PerConnectionWebSocketHandler(GameSocketHandler.class);
    }
}

