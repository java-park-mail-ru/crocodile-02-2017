package server;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = {
        "http://vstaem.herokuapp.com", "https://vstaem.herokuapp.com", "http://vstaem-dev.herokuapp.com", "https://vstaem-dev.herokuapp.com",
        "http://localhost", "http://127.0.0.1"})
public class AccountController {

    private static final String ID_ATTR = "userId";
    private static final String LOGIN_ATTR = "login";
    private static final String PASSWORD_ATTR = "password";
    private static final String EMAIL_ATTR = "email";
    private static final Logger LOGGER = LoggerFactory.getLogger(AccountController.class);

    private final AccountService accountService;

    @Autowired
    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @SuppressWarnings("unused")
    private static class AccountData {

        private static final int PASSWORD_MIN_LENGTH = 6;

        private final String login;
        private final String password;
        private final String email;
        private int rating;

        @JsonCreator
        AccountData(
                @JsonProperty(LOGIN_ATTR) String login,
                @JsonProperty(PASSWORD_ATTR) String password,
                @JsonProperty(EMAIL_ATTR) String email) {

            this.login = login;
            this.password = password;
            this.email = email;
            this.rating = 0;
        }

        AccountData(@NotNull Account account) {

            this.login = account.getLogin();
            this.password = null;
            this.email = account.getEmail();
            this.rating = account.getRating();
        }

        public String getLogin() {
            return login;
        }

        String getPassword() {
            return password;
        }

        public String getEmail() {
            return email;
        }

        public int getRating() {
            return rating;
        }

        boolean satisfiesRegistration() {

            final boolean allFieldsProvided = (login != null) && (password != null) && (email != null);
            return allFieldsProvided && (password.length() >= PASSWORD_MIN_LENGTH) && (email.contains("@"));
        }

        boolean satisfiesLoggingIn() {
            return (login != null) && (password != null);
        }

        boolean satisfiesChanges() {

            return ( password == null ) || ( password.length() >= PASSWORD_MIN_LENGTH );
        }
    }

    private enum ErrorCode {

        BAD_DATA("bad_data"),
        LOG_OUT("log_out"),
        EXISTS("exists"),
        FORBIDDEN("forbidden"),
        LOG_IN("log_in"),
        NOT_FOUND("not_found");

        private String text;

        ErrorCode(final String text) {

            this.text = text;
        }

        @Override
        public String toString() {
            return text;
        }
    }

    @SuppressWarnings("unused")
    private static class ErrorBody {

        ErrorBody(ErrorCode code, String message) {

            this.code = code.toString();
            this.message = message;
        }

        public String getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }

        private final String code;
        private final String message;
    }

    @PostMapping(path = "/register/", consumes = "application/json", produces = "application/json")
    public ResponseEntity register(@RequestBody AccountData body, HttpSession session) {

        if (session.getAttribute(ID_ATTR) != null) {

            LOGGER.debug("User #{} tried to register while he was logged in.", session.getAttribute(ID_ATTR));
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(new ErrorBody(ErrorCode.LOG_OUT, "You must be logged out to perform this operation."));
        }

        if (!body.satisfiesRegistration()) {

            LOGGER.debug("Not all neccessary fields were provided for registration.");
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorBody(ErrorCode.BAD_DATA, "Incorrect registration information was provided."));
        }

        if (accountService.has(body.getLogin())) {

            LOGGER.debug("User with login {} is already registered.", body.getLogin());
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(new ErrorBody(ErrorCode.EXISTS, "Login is already taken."));
        }

        final Account account = accountService.addAccount(body.getLogin(), body.getPassword(), body.getEmail());
        LOGGER.info("User #{}: {}, {} registered.", account.getId(), account.getLogin(), account.getEmail());
        session.setAttribute(ID_ATTR, account.getId());
        return ResponseEntity.ok(new AccountData(account));
    }

    @PostMapping(path = "/login/", consumes = "application/json")
    public ResponseEntity login(@RequestBody AccountData body, HttpSession session) {

        if (session.getAttribute(ID_ATTR) != null) {

            LOGGER.debug("User #{} tried to login while he was logged in.", session.getAttribute(ID_ATTR));
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(new ErrorBody(ErrorCode.LOG_OUT, "You must be logged out to perform this operation."));
        }

        if (!body.satisfiesLoggingIn()) {

            LOGGER.debug("Not all neccessary fields were provided for logging in.");
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorBody(ErrorCode.BAD_DATA, "Incorrect signing in information was provided."));
        }

        final Account account = accountService.find(body.getLogin());

        if ((account != null) && (account.passwordMatches(body.getPassword()))) {

            LOGGER.info("User #{} logged in.", account.getId());
            session.setAttribute(ID_ATTR, account.getId());
            return ResponseEntity.ok("");
        }

        LOGGER.debug("Invalid credentials {}, {} for logging in.", body.getLogin(), body.getPassword());
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(new ErrorBody(ErrorCode.FORBIDDEN, "Login or password doesn't match."));
    }

    @PostMapping(path = "/change/", consumes = "application/json", produces = "application/json")
    public ResponseEntity changeUser(@RequestBody AccountData body, HttpSession session) {

        if (session.getAttribute(ID_ATTR) == null) {

            LOGGER.debug("Unlogged user tried to change credentials.");
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(new ErrorBody(ErrorCode.LOG_IN, "You must be logged in to perform this operation."));
        }

        final int id = ( Integer ) session.getAttribute(ID_ATTR);
        final Account account = accountService.find(id);

        if (account == null) {

            LOGGER.error("Account #{} is no longer valid.", id);
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new ErrorBody(ErrorCode.NOT_FOUND, "Your account is no longer valid."));
        }

        if (!body.getLogin().equals(account.getLogin()) && accountService.has(body.getLogin())) {

            LOGGER.debug("Login {} to change on is already taken.", body.getLogin());
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(new ErrorBody(ErrorCode.EXISTS, "Login is already taken."));
        }

        if (!body.satisfiesChanges()) {

            LOGGER.debug("Password to change on is incorrect");
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body( new ErrorBody(ErrorCode.BAD_DATA, "Your password is too short."));
        }

        account.setProperties(body.getLogin(), body.getPassword(), body.getEmail());
        LOGGER.info("User #{}-> {}, {} was changed.", account.getId(), account.getLogin(), account.getEmail());
        return ResponseEntity.ok(new AccountData(account));
    }

    @PostMapping(path = "/logout/")
    public ResponseEntity logout(HttpSession session) {

        if (session.getAttribute(ID_ATTR) != null) {

            LOGGER.info("User #{} logged out.", session.getAttribute(ID_ATTR));
        }

        session.invalidate();
        return ResponseEntity.ok("");
    }

    @GetMapping(path = "/who-am-i/", produces = "application/json")
    public ResponseEntity getInfo(HttpSession session) {

        if (session.getAttribute(ID_ATTR) == null) {

            LOGGER.debug("Unlogged user tried to get his credentials.");
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(new ErrorBody(ErrorCode.LOG_IN, "You must be logged in to perform this operation."));
        }

        final int id = ( Integer ) session.getAttribute(ID_ATTR);
        final Account account = accountService.find(id);

        if (account != null) {

            LOGGER.info("Credentials were sent to user #{}.", account.getId());
            return ResponseEntity.ok(new AccountData(account));
        }

        LOGGER.error("Account #{} is no longer valid.", id);
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorBody(ErrorCode.NOT_FOUND, "Your account is no longer valid."));
    }

    @GetMapping(path = "/best/", produces = "application/json")
    public ResponseEntity getBest() {

        return ResponseEntity.ok(accountService.getBest().stream().map(AccountData::new).collect(Collectors.toSet()));
    }
}
