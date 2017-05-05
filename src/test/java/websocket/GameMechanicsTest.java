package websocket;

import database.AccountServiceDb;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.MockMvcPrint;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.websocket.ContainerProvider;
import javax.websocket.WebSocketContainer;
import java.net.URI;
import java.util.ArrayList;

@SuppressWarnings({"OverlyBroadThrowsClause", "SpringJavaAutowiredMembersInspection"})
@SpringBootTest
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc(print = MockMvcPrint.NONE)
@Transactional
public class GameMechanicsTest {

    private static final String CORRECT_EMAIL = "correct@mail.ru";
    private static final String CORRECT_PASSWORD = "correct";
    private static final String WS_URI = "ws://localhost:8082/sp-games/";

    @Autowired
    private AccountServiceDb accountService;

    @Autowired
    private GameManagerService gameManagerService;

    @Autowired
    private GameSocketHandler gameSocketHandler;

    @Autowired
    private WebSocketContainer webSocketContainer;

    private final ArrayList<String> users = new ArrayList<>();

    @Before
    public void setup() {

        webSocketContainer = ContainerProvider.getWebSocketContainer();

        for (int i = 0; i < GameManagerService.MULTIPLAYER_UPPER_PLAYERS_LIMIT; i++) {
            users.add("User" + Integer.toString(i + 1));
            accountService.createAccount(users.get(i), CORRECT_PASSWORD, CORRECT_EMAIL);
        }
    }


    @Test
    public void socketOpen() throws Exception {

        webSocketContainer.connectToServer(gameSocketHandler, URI.create(WS_URI));
    }
}
