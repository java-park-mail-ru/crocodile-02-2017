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

@RestController
@CrossOrigin( origins = { "http://vstaem.herokuapp.com/", "https://vstaem.herokuapp/com/", "http://127.0.0.1" } )
public class AccountController {

    private static final String ID_ATTR = "userId";
    private static final String LOGIN_ATTR = "login";
    private static final String PASSWORD_ATTR = "password";
    private static final String EMAIL_ATTR = "email";

    private static final Logger LOGGER = LoggerFactory.getLogger( AccountController.class );

    @Autowired
    public AccountController( AccountService accountService ) {
        this.accountService = accountService;
    }

    private static class AccountBody {

        @SuppressWarnings( "unused" )
        @JsonCreator
        AccountBody(
                @JsonProperty( LOGIN_ATTR ) String login,
                @JsonProperty( PASSWORD_ATTR ) String password,
                @JsonProperty( EMAIL_ATTR ) String email ) {

            this.login = login;
            this.password = password;
            this.email = email;
        }

        AccountBody( @NotNull AccountService.Account account ) {

            this.login = account.getLogin();
            this.password = null;
            this.email = account.getEmail();
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

        boolean isFull() {

            return ( login != null ) && ( password != null ) && ( email != null );
        }

        boolean isShort() {

            return ( login != null ) && ( password != null );
        }

        private final String login;
        private final String password;
        private final String email;
    }

    private static class ErrorBody {

        ErrorBody( HttpStatus code, String message ) {

            this.code = code;
            this.message = message;
        }


        @SuppressWarnings( "unused" )
        public HttpStatus getCode() {
            return code;
        }

        @SuppressWarnings( "unused" )
        public String getMessage() {
            return message;
        }

        private final HttpStatus code;
        private final String message;
    }

    @PostMapping( path = "/register/", consumes = "application/json", produces = "application/json" )
    public ResponseEntity register( @RequestBody AccountBody body, HttpSession session ) {

        if ( session.getAttribute( ID_ATTR ) != null ) {

            LOGGER.error( "User #{} tried to register while he was logged in.", session.getAttribute( ID_ATTR ) );
            return ResponseEntity
                    .status( HttpStatus.FORBIDDEN )
                    .body( new ErrorBody( HttpStatus.FORBIDDEN, "You must be logged out to perform this operation." ) );
        }

        if ( !body.isFull() ) {

            LOGGER.error( "Not all neccessary fields were provided for registration." );
            return ResponseEntity
                    .status( HttpStatus.BAD_REQUEST )
                    .body( new ErrorBody( HttpStatus.BAD_REQUEST, "Not all fields were provided." ) );
        }

        if ( accountService.has( body.getLogin() ) ) {

            LOGGER.error( "User with login {} is already registered.", body.getLogin() );
            return ResponseEntity
                    .status( HttpStatus.CONFLICT )
                    .body( new ErrorBody( HttpStatus.CONFLICT, "Login is already taken." ) );
        }

        final AccountService.Account account = accountService.addAccount( body.getLogin(), body.getPassword(), body.getEmail() );
        LOGGER.info( "User #{}: {}, {} registered.", account.getId(), account.getLogin(), account.getEmail() );
        session.setAttribute( ID_ATTR, account.getId() );
        return ResponseEntity.ok( new AccountBody( account ) );
    }

    @PostMapping( path = "/login/", consumes = "application/json" )
    public ResponseEntity login( @RequestBody AccountBody body, HttpSession session ) {

        if ( session.getAttribute( ID_ATTR ) != null ) {

            LOGGER.error( "User #{} tried to login while he was logged in.", session.getAttribute( ID_ATTR ) );
            return ResponseEntity
                    .status( HttpStatus.CONFLICT )
                    .body( new ErrorBody( HttpStatus.CONFLICT, "You must be logged out to perform this operation." ) );
        }

        if ( !body.isShort() ) {

            LOGGER.error( "Not all neccessary fields were provided for logging in." );
            return ResponseEntity
                    .status( HttpStatus.BAD_REQUEST )
                    .body( new ErrorBody( HttpStatus.BAD_REQUEST, "Not all fields were provided." ) );
        }

        final AccountService.Account account = accountService.find( body.getLogin() );

        if ( ( account != null ) && ( account.passwordMatches( body.getPassword() ) ) ) {

            LOGGER.info( "User #{} logged in.", account.getId() );
            session.setAttribute( ID_ATTR, account.getId() );
            return ResponseEntity.ok( "" );
        }

        LOGGER.error( "Invalid credentials {}, {} for logging in.", body.getLogin(), body.getPassword() );
        return ResponseEntity
                .status( HttpStatus.FORBIDDEN )
                .body( new ErrorBody( HttpStatus.FORBIDDEN, "Login or password doesn't match." ) );
    }

    @PostMapping( path = "/change/", consumes = "application/json", produces = "application/json" )
    public ResponseEntity changeUser( @RequestBody AccountBody body, HttpSession session ) {

        if ( session.getAttribute( ID_ATTR ) == null ) {

            LOGGER.error( "Unlogged user tried to change credentials." );
            return ResponseEntity
                    .status( HttpStatus.FORBIDDEN )
                    .body( new ErrorBody( HttpStatus.FORBIDDEN, "You must be logged in to perform this operation." ) );
        }

        final int id = ( Integer ) session.getAttribute( ID_ATTR );
        final AccountService.Account account = accountService.find( id );

        if ( account == null ) {

            LOGGER.error( "Account #{} is no longer valid.", id );
            return ResponseEntity
                    .status( HttpStatus.NOT_FOUND )
                    .body( new ErrorBody( HttpStatus.NOT_FOUND, "Your account is no longer valid." ) );
        }

        if ( !body.getLogin().equals( account.getLogin() ) && accountService.has( body.getLogin() ) ) {

            LOGGER.error( "Login {} to change on is already taken.", body.getLogin() );
            return ResponseEntity
                    .status( HttpStatus.CONFLICT )
                    .body( new ErrorBody( HttpStatus.CONFLICT, "Login is already taken." ) );
        }

        LOGGER.info( "User #{} was changed.", account.getId() );
        account.setProperties( body.getLogin(), body.getPassword(), body.getEmail() );
        return ResponseEntity.ok( new AccountBody( account ) );
    }

    @PostMapping( path = "/logout/" )
    public ResponseEntity logout( HttpSession session ) {

        if ( session.getAttribute( ID_ATTR ) != null ) {

            LOGGER.info( "User #{} logged out.", session.getAttribute( ID_ATTR ) );
        }

        session.invalidate();
        return ResponseEntity.ok( "" );
    }

    @GetMapping( path = "/who-am-i/", produces = "application/json" )
    public ResponseEntity getId( HttpSession session ) {

        if ( session.getAttribute( ID_ATTR ) == null ) {

            LOGGER.error( "Unlogged user tried to get his credentials." );
            return ResponseEntity
                    .status( HttpStatus.FORBIDDEN )
                    .body( new ErrorBody( HttpStatus.FORBIDDEN, "You must be logged out to perform this operation." ) );
        }

        final int id = ( Integer ) session.getAttribute( ID_ATTR );
        final AccountService.Account account = accountService.find( id );

        if ( account != null ) {

            LOGGER.info( "Credentials were sent to user #{}.", account.getId() );
            return ResponseEntity.ok( new AccountBody( account ) );
        }

        LOGGER.error( "Account #{} is no longer valid.", id );
        return ResponseEntity
                .status( HttpStatus.NOT_FOUND )
                .body( new ErrorBody( HttpStatus.NOT_FOUND, "Your account is no longer valid." ) );
    }

    private final AccountService accountService;
}
