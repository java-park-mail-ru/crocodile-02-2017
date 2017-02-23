package server;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

@RestController
@CrossOrigin( origins = { "http://vstaem.herokuapp.com/", "http://127.0.0.1" } )
public class AccountController {

    private static final String ID_ATTR = "userId";
    private static final String LOGIN_ATTR = "login";
    private static final String PASSWORD_ATTR = "password";
    private static final String EMAIL_ATTR = "email";

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

        AccountBody() {

            this.login = null;
            this.password = null;
            this.email = null;
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

        private String login;
        private String password;
        private String email;
    }

    @PostMapping( path = "/register/", consumes = "application/json", produces = "application/json" )
    public ResponseEntity< AccountBody > register( @RequestBody AccountBody body, HttpSession session ) {

        if ( body.isFull() ) {

            return ResponseEntity.status( HttpStatus.BAD_REQUEST ).body( new AccountBody() );
        }

        if ( accountService.has( body.getLogin() ) ) {

            return ResponseEntity.status( HttpStatus.CONFLICT ).body( new AccountBody() );
        }

        final AccountService.Account account = accountService.addAccount( body.getLogin(), body.getPassword(), body.getEmail() );
        session.setAttribute( ID_ATTR, account.getId() );
        return ResponseEntity.ok( new AccountBody( account ) );
    }

    @PostMapping( path = "/login/", consumes = "application/json" )
    public ResponseEntity login( @RequestBody AccountBody body, HttpSession session ) {

        if ( !body.isShort() ) {

            return ResponseEntity.status( HttpStatus.BAD_REQUEST ).body( "" );
        }

        final AccountService.Account account = accountService.find( body.getLogin() );

        if ( ( account != null ) && ( account.passwordMatches( body.getPassword() ) ) ) {

            session.setAttribute( ID_ATTR, account.getId() );
            return ResponseEntity.ok( "" );
        }

        return ResponseEntity.status( HttpStatus.FORBIDDEN ).body( "" );
    }

    @PostMapping( path = "/change/", consumes = "application/json", produces = "application/json" )
    public ResponseEntity< AccountBody > changeUser( @RequestBody AccountBody body, HttpSession session ) {

        if ( session.getAttribute( ID_ATTR ) == null ) {

            return ResponseEntity.status( HttpStatus.FORBIDDEN ).body( new AccountBody() );
        }

        final int id = ( Integer ) session.getAttribute( ID_ATTR );
        final AccountService.Account account = accountService.find( id );

        if ( account == null ) {

            return ResponseEntity.status( HttpStatus.NOT_FOUND ).body( new AccountBody() );
        }

        if ( account.getId() != id ) {

            return ResponseEntity.status( HttpStatus.FORBIDDEN ).body( new AccountBody() );
        }

        if ( accountService.has( body.getLogin() ) ) {

            return ResponseEntity.status( HttpStatus.CONFLICT ).body( new AccountBody() );
        }

        account.setProperties( body.getLogin(), body.getPassword(), body.getEmail() );
        return ResponseEntity.ok( new AccountBody( account ) );
    }

    @GetMapping( path = "/logout/" )
    public ResponseEntity logout( HttpSession session ) {

        session.removeAttribute( ID_ATTR );
        return ResponseEntity.ok( "" );
    }

    @GetMapping( path = "/who-am-i/", produces = "application/json" )
    public ResponseEntity< AccountBody > getId( HttpSession session ) {

        if ( session.getAttribute( ID_ATTR ) == null ) {

            return ResponseEntity.status( HttpStatus.FORBIDDEN ).body( new AccountBody() );
        }

        final AccountService.Account account = accountService.find( ( Integer ) session.getAttribute( ID_ATTR ) );

        if ( account != null ) {

            return ResponseEntity.ok( new AccountBody( account ) );
        }

        return ResponseEntity.status( HttpStatus.NOT_FOUND ).body( new AccountBody() );
    }

    private AccountService accountService;
}
