package server;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.HashMap;

@RestController
@CrossOrigin( origins = "http://vstaem.herokuapp.com/" )
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

        @JsonCreator
        AccountBody(
                @JsonProperty( LOGIN_ATTR ) String login,
                @JsonProperty( PASSWORD_ATTR ) String password,
                @JsonProperty( EMAIL_ATTR ) String email ) {

            this.login = login;
            this.password = password;
            this.email = email;
        }

        String getLogin() {
            return login;
        }

        String getPassword() {
            return password;
        }

        public String getEmail() {
            return email;
        }

        public boolean isFull() {

            return ( login != null ) && ( password != null ) && ( email != null );
        }

        private String login;
        private String password;
        private String email;
    }

    @SuppressWarnings( "unused" )
    private static class AnswerBody {

        AnswerBody() {
            properties = new HashMap< String, String >();
        }

        public void addProperty( @NotNull String name, String value ) {
            properties.put( name, value );
        }

        public HashMap< String, String > getProperties() {
            return new HashMap< String, String >( properties );
        }

        private HashMap< String, String > properties;
    }

    @PostMapping( path = "/register/", consumes = "application/json", produces = "application/json" )
    public ResponseEntity< AnswerBody > register( @RequestBody AccountBody body, HttpSession session ) {

        final AnswerBody answerBody = new AnswerBody();

        if ( body.isFull() && !accountService.has( body.getLogin() ) ) {

            final AccountService.Account account =
                    accountService.addAccount( body.getLogin(), body.getPassword(), body.getEmail() );

            session.setAttribute( ID_ATTR, account.getId() );
            answerBody.addProperty( ID_ATTR, ( ( Integer ) account.getId() ).toString() );
            answerBody.addProperty( LOGIN_ATTR, account.getLogin() );
            return ResponseEntity.ok( answerBody );
        }

        return ResponseEntity.status( HttpStatus.BAD_REQUEST ).body( answerBody );
    }

    @PostMapping( path = "/login/", consumes = "application/json", produces = "application/json" )
    public ResponseEntity< AnswerBody > login( @RequestBody AccountBody body, HttpSession session ) {

        final AnswerBody answerBody = new AnswerBody();
        final AccountService.Account account = accountService.find( body.getLogin() );

        if ( ( account != null ) && ( account.passwordMatches( body.getPassword() ) ) ) {

            session.setAttribute( ID_ATTR, account.getId() );
            return ResponseEntity.ok( answerBody );
        }

        return ResponseEntity.status( HttpStatus.FORBIDDEN ).body( answerBody );
    }

    @PostMapping( path = "/change/", consumes = "application/json", produces = "application/json" )
    public ResponseEntity< AnswerBody > changeUser( @RequestBody AccountBody body, HttpSession session ) {

        final AnswerBody answerBody = new AnswerBody();

        if ( session.getAttribute( ID_ATTR ) == null ) {

            return ResponseEntity.status( HttpStatus.FORBIDDEN ).body( answerBody );
        }

        final int id = ( Integer ) session.getAttribute( ID_ATTR );
        final AccountService.Account account = accountService.find( id );

        if ( account == null ) {

            return ResponseEntity.status( HttpStatus.NOT_FOUND ).body( answerBody );
        }

        if ( accountService.has( body.getLogin() ) ) {

            return ResponseEntity.status( HttpStatus.CONFLICT ).body( answerBody );
        }

        account.setProperties( body.getLogin(), body.getPassword(), body.getEmail() );
        answerBody.addProperty( LOGIN_ATTR, account.getLogin() );
        answerBody.addProperty( PASSWORD_ATTR, account.getPassword() );
        answerBody.addProperty( EMAIL_ATTR, account.getEmail() );
        return ResponseEntity.ok( answerBody );
    }

    @GetMapping( path = "/logout/" )
    public ResponseEntity logout( HttpSession session ) {

        final AnswerBody answerBody = new AnswerBody();
        session.removeAttribute( ID_ATTR );
        return ResponseEntity.ok( answerBody );
    }

    @GetMapping( path = "/who-am-i/", produces = "application/json" )
    public ResponseEntity getId( HttpSession session ) {

        final AnswerBody answerBody = new AnswerBody();

        if ( session.getAttribute( ID_ATTR ) != null ) {

            final AccountService.Account account = accountService.find( ( Integer ) session.getAttribute( ID_ATTR ) );

            if ( account != null ) {

                answerBody.addProperty( ID_ATTR, ( ( Integer ) account.getId() ).toString() );
                answerBody.addProperty( LOGIN_ATTR, account.getLogin() );
                return ResponseEntity.ok( answerBody );
            }
        }

        return ResponseEntity.status( HttpStatus.NOT_FOUND ).body( answerBody );
    }

    private AccountService accountService;
}
