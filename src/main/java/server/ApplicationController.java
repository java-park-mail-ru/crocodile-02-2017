package server;

import database.Account;
import database.AccountServiceDatabase;
import database.DashServiceDatabase;
import database.Dashes;
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
import java.util.LinkedHashSet;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = {
    "http://vstaem.herokuapp.com", "https://vstaem.herokuapp.com", "http://vstaem-dev.herokuapp.com", "https://vstaem-dev.herokuapp.com",
    "http://localhost", "http://127.0.0.1"})
public class ApplicationController {

    public static final String SESSION_ATTR = "login";
    public static final String DASHES_ATTR = "picid";

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationController.class);

    private final AccountServiceDatabase accountService;
    private final DashServiceDatabase dashService;

    @Autowired
    public ApplicationController(
        AccountServiceDatabase accountService,
        DashServiceDatabase dashService) {

        this.accountService = accountService;
        this.dashService = dashService;
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
    //Beta section

    @PostMapping(path = "/update-rating/", produces = "application/json")
    public ResponseEntity updateRating(@RequestParam(value = "delta") int delta, HttpSession session) {

        final String login = (String) session.getAttribute(SESSION_ATTR);
        return ResponseEntity.ok(new AccountData(accountService.updateAccountRating(login, delta)));
    }

    @GetMapping(path = "/get-dashes/", produces = "application/json")
    public ResponseEntity getDashes(HttpSession session) {

        final String login = (String) session.getAttribute(SESSION_ATTR);
        final Dashes dashes = dashService.getRandomDash(login);
        session.setAttribute(DASHES_ATTR, dashes.getId());
        return ResponseEntity.ok(new DashesData(dashService.getRandomDash(login)));
    }

    @GetMapping(path = "/check-answer/", produces = "application/json")
    public ResponseEntity addUsedDashes(@RequestParam(value = "word") String word, HttpSession session) {

        final int dashesID = (int) session.getAttribute(DASHES_ATTR);
        final boolean isCorrect = dashService.checkWord(word, dashesID);

        return (isCorrect) ?
            ResponseEntity.ok("{ \"correct\": true }") :
            ResponseEntity.ok("{ \"correct\": false }");
    }
}
