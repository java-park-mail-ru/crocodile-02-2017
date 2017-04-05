package server;

import database.*;
import messagedata.AccountData;
import messagedata.DashesData;
import messagedata.ErrorCode;
import messagedata.ErrorData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = {
    "http://vstaem.herokuapp.com", "https://vstaem.herokuapp.com", "http://vstaem-dev.herokuapp.com", "https://vstaem-dev.herokuapp.com",
    "http://localhost", "http://127.0.0.1"})
public class ApplicationController {

    public static final String SESSION_ATTR = "login";
    public static final String GAME_ATTR = "pgameid";
    public static final int SINGLE_GAME_TIME = 30;

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationController.class);

    private final AccountServiceDb accountService;
    private final DashesServiceDb dashesService;
    private final SingleGameServiceDb singleGameService;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final HashMap<Integer, SingleGame> currentSingleGames = new HashMap<>();

    @Autowired
    public ApplicationController(
        AccountServiceDb accountService,
        DashesServiceDb dashesService,
        SingleGameServiceDb singleGameService) {

        this.accountService = accountService;
        this.dashesService = dashesService;
        this.singleGameService = singleGameService;
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity databaseError(HttpServletRequest request, DataAccessException exception) {

        LOGGER.error("Request: " + request.getRequestURL() + " raised " + exception);
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ErrorData(ErrorCode.INTERNAL, "Internal database error."));
    }

    @PostMapping(path = "/register/", consumes = "application/json", produces = "application/json")
    public ResponseEntity register(@RequestBody AccountData body, HttpSession session) {

        if (session.getAttribute(SESSION_ATTR) != null) {

            LOGGER.debug("User #{} tried to register while he was logged in.", session.getAttribute(SESSION_ATTR));
            return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(new ErrorData(ErrorCode.LOG_OUT, "You must be logged out to perform this operation."));
        }

        if (!Validator.checkRegistrationFields(body)) {

            LOGGER.debug("Not all fields were provided for registration.");
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorData(ErrorCode.INSUFFICIENT, "Not all fields were provided."));
        }

        if (!Validator.checkPassword(body)) {

            LOGGER.debug("Invalid password was provided for registration.");
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorData(ErrorCode.INVALID_FIELD, "Invalid password was provided."));
        }

        if (!Validator.checkEmail(body)) {

            LOGGER.debug("Invalid email was provided for registration.");
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorData(ErrorCode.INVALID_FIELD, "Invalid email was provided."));
        }

        try {
            final Account account = accountService.createAccount(body.getLogin(), body.getPassword(), body.getEmail());
            LOGGER.info("User #{}: {}, {} registered.", account.getId(), account.getLogin(), account.getEmail());
            session.setAttribute(SESSION_ATTR, account.getLogin());
            return ResponseEntity.ok(new AccountData(account));

        } catch (DuplicateKeyException exception) {

            LOGGER.debug("User with login {} is already registered.", body.getLogin());
            return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(new ErrorData(ErrorCode.EXISTS, "Login is already taken."));
        }

    }

    @PostMapping(path = "/login/", consumes = "application/json")
    public ResponseEntity login(@RequestBody AccountData body, HttpSession session) {

        if (session.getAttribute(SESSION_ATTR) != null) {

            LOGGER.debug("User #{} tried to login while he was logged in.", session.getAttribute(SESSION_ATTR));
            return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(new ErrorData(ErrorCode.LOG_OUT, "You must be logged out to perform this operation."));
        }

        if (!Validator.checkLoggingInFields(body)) {

            LOGGER.debug("Not all fields were provided for logging in.");
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorData(ErrorCode.INSUFFICIENT, "Not all fields were provided."));
        }

        final Account account = accountService.findAccount(body.getLogin());

        if ((account != null) && (account.passwordMatches(body.getPassword()))) {

            LOGGER.info("User #{} logged in.", account.getLogin());
            session.setAttribute(SESSION_ATTR, account.getLogin());
            return ResponseEntity.ok("");
        }

        LOGGER.debug("Invalid credentials {}, {} for logging in.", body.getLogin(), body.getPassword());
        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(new ErrorData(ErrorCode.FORBIDDEN, "Login or password doesn't match."));
    }

    @PostMapping(path = "/change/", consumes = "application/json", produces = "application/json")
    public ResponseEntity changeAccount(@RequestBody AccountData body, HttpSession session) {

        if (session.getAttribute(SESSION_ATTR) == null) {

            LOGGER.debug("Unlogged user tried to change credentials.");
            return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(new ErrorData(ErrorCode.LOG_IN, "You must be logged in to perform this operation."));
        }

        final String login = (String) session.getAttribute(SESSION_ATTR);
        Account account = accountService.findAccount(login);

        if (account == null) {

            LOGGER.error("Account #{} is no longer valid.", login);
            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorData(ErrorCode.NOT_FOUND, "Your account is no longer valid."));
        }

        if (body.hasPassword() && !Validator.checkPassword(body)) {

            LOGGER.debug("Invalid password was provided for changing.");
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorData(ErrorCode.INVALID_FIELD, "Invalid password was provided."));
        }

        if (body.hasEmail() && !Validator.checkEmail(body)) {

            LOGGER.debug("Invalid email was provided for changing.");
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorData(ErrorCode.INVALID_FIELD, "Invalid email was provided."));
        }

        try {
            account = accountService.updateAccountInfo(
                account.getLogin(),
                body.getLogin(),
                body.getPassword(),
                body.getEmail());
            LOGGER.info("User #{} was changed -> {}, {}.", account.getId(), account.getLogin(), account.getEmail());
            return ResponseEntity.ok(new AccountData(account));

        } catch (DuplicateKeyException exception) {

            LOGGER.debug("Login {} to change on is already taken.", body.getLogin());
            return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(new ErrorData(ErrorCode.EXISTS, "Login is already taken."));
        }
    }

    @PostMapping(path = "/logout/")
    public ResponseEntity logout(HttpSession session) {

        if (session.getAttribute(SESSION_ATTR) != null) {

            LOGGER.info("User #{} logged out.", session.getAttribute(SESSION_ATTR));
        }

        session.invalidate();
        return ResponseEntity.ok("");
    }

    @GetMapping(path = "/who-am-i/", produces = "application/json")
    public ResponseEntity getInfo(HttpSession session) {

        if (session.getAttribute(SESSION_ATTR) == null) {

            LOGGER.debug("Unlogged user tried to get his credentials.");
            return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(new ErrorData(ErrorCode.LOG_IN, "You must be logged in to perform this operation."));
        }

        final String login = (String) session.getAttribute(SESSION_ATTR);
        final Account account = accountService.findAccount(login);

        if (account != null) {

            LOGGER.info("Credentials were sent to user #{}.", account.getLogin());
            return ResponseEntity.ok(new AccountData(account));
        }

        LOGGER.error("Account #{} is no longer valid.", login);
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(new ErrorData(ErrorCode.NOT_FOUND, "Your account is no longer valid."));
    }

    @GetMapping(path = "/best/", produces = "application/json")
    public ResponseEntity getBest() {
        return ResponseEntity.ok(accountService
            .getBest().stream().map(AccountData::new)
            .collect(Collectors.toCollection(LinkedHashSet::new)));
    }

    ///////////////////////////////////
    //Temp section

    private synchronized boolean changeGameState(HttpSession session, int gameId, boolean shutdown) {

        if (shutdown && currentSingleGames.containsKey(gameId)) {

            currentSingleGames.remove(gameId);
            singleGameService.shutdownGame(gameId);
            session.removeAttribute(GAME_ATTR);
            return true;
        }

        return false;
    }

    private Runnable runDeletion(HttpSession session, int gameId) {

        return () -> changeGameState(session, gameId, true);
    }

    @PostMapping(path = "/start-game/", produces = "application/json")
    public ResponseEntity startSingleGame(HttpSession session) {

        final String login = (String) session.getAttribute(SESSION_ATTR);
        final Dashes dashes = dashesService.getRandomDash(login);
        LOGGER.info("Got dashes {}, {}", dashes.getId(), dashes.getWord());

        final SingleGame game = singleGameService.createGame(login, dashes.getId());
        currentSingleGames.put(game.getId(), game);
        session.setAttribute(GAME_ATTR, game.getId());

        scheduler.schedule(runDeletion(session, game.getId()), SINGLE_GAME_TIME, TimeUnit.SECONDS);

        return ResponseEntity.ok(new DashesData(dashes));
    }

    @PostMapping(path = "/check-answer/", produces = "application/json")
    public ResponseEntity checkAnswer(@RequestParam(value = "word") String word, HttpSession session) {

        final String login = (String) session.getAttribute(SESSION_ATTR);
        final int gameId = (int) session.getAttribute(GAME_ATTR);
        final SingleGame game = currentSingleGames.get(gameId);

        if ((game == null) || !game.getLogin().equals(login)) {

            return ResponseEntity.ok("{ \"correct\": false }");
        }

        final int dashesId = game.getDashesId();
        final boolean isCorrect = dashesService.checkWord(word, dashesId);

        if (changeGameState(session, gameId, isCorrect)) {

            dashesService.addUsedDashes(login, dashesId);
            accountService.updateAccountRating(login, 1);
            session.removeAttribute(GAME_ATTR);
            return ResponseEntity.ok("{ \"correct\": true }");
        }

        return ResponseEntity.ok("{ \"correct\": false }");
    }

    @PostMapping(path = "/exit-game/", produces = "application/json")
    public ResponseEntity manualShutdown(HttpSession session) {

        final String login = (String) session.getAttribute(SESSION_ATTR);
        final int gameId = (int) session.getAttribute(GAME_ATTR);
        final SingleGame game = currentSingleGames.get(gameId);

        if ((game != null) && game.getLogin().equals(login)) {

            changeGameState(session, gameId, true);
        }

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("");
    }
}
