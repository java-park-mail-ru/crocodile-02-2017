package server;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

@RestController
public class AccountController {

    private final static String ID_ATTR = "userId";
    private final static String LOGIN_ATTR = "login";
    private final static String PASSWORD_ATTR = "password";

    @Autowired
    public AccountController( AccountService accountService ) {
        this.accountService = accountService;
    }

    private static class AccountBody {

        private String login;
        private String password;

        @JsonCreator
        public AccountBody(
                @JsonProperty( LOGIN_ATTR ) String login,
                @JsonProperty( PASSWORD_ATTR ) String password ) {

            this.login = login;
            this.password = password;
        }

        String getLogin() {
            return login;
        }

        String getPassword() {
            return password;
        }
    }

    @PostMapping( path = "/login/", consumes = "application/json" )
    public ResponseEntity login( @RequestBody AccountBody body, HttpSession session ) {

        Account account = accountService.find( body.getLogin() );

        if ( ( account != null ) && ( account.passwordMatches( body.getPassword() ) ) ) {

            session.setAttribute( ID_ATTR, account.getId() );
            return ResponseEntity.ok( body.getLogin() + " " + body.getPassword() + " logged in." );
        }

        return ResponseEntity.status( HttpStatus.FORBIDDEN ).body( "Failed to login" );
    }

    @PostMapping( path = "/register/", consumes = "application/json" )
    public ResponseEntity register( @RequestBody AccountBody body, HttpSession session ) {

        if ( ( body.getPassword() != null ) && !accountService.has( body.getLogin() ) ) {

            int id = accountService.addAccount( body.getLogin(), body.getPassword() ).getId();
            session.setAttribute( ID_ATTR, id );
            return ResponseEntity.ok( body.getLogin() + " " + body.getPassword() + " registered.");
        }

        return ResponseEntity.status( HttpStatus.BAD_REQUEST ).body( "Something is wrong with your request" );
    }

    @GetMapping( path = "/logout/" )
    public ResponseEntity logout( HttpSession session ) {

        session.removeAttribute( ID_ATTR );
        return ResponseEntity.ok( "Logged out." );
    }

    @GetMapping( path = "/who_am_i/" )
    public ResponseEntity getId( HttpSession session ) {

        if ( session.getAttribute( ID_ATTR ) != null ) {

            return ResponseEntity.ok( accountService.find( ( Integer ) session.getAttribute( ID_ATTR ) ).getLogin() );
        }

        return ResponseEntity.ok( "Account not found." );
    }

    private AccountService accountService;
}
