package server;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.HashMap;

@RestController
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

        private String login;
        private String password;
        private String email;
    }

    @SuppressWarnings( "unused" )
    private static class AnswerBody {

        static final int STATUS_OK = 200;
        static final int STATUS_BAD = 400;
        static final int STATUS_FORBIDDEN = 403;
        static final int STATUS_NOT_FOUND = 404;

        AnswerBody() {
            this.code = STATUS_OK;
            parameters = new HashMap< String, String >();
        }

        AnswerBody( int error ) {
            code = error;
            parameters = new HashMap< String, String >();
        }

        public void addParameter( @NotNull String name, String value ) {
            parameters.put( name, value );
        }

        public void setCode( int error ) {
            code = error;
        }

        public int getCode() {
            return code;
        }

        public HashMap< String, String > getParameters() {
            return new HashMap< String, String >( parameters );
        }

        private int code;
        private HashMap< String, String > parameters;
    }

    @PostMapping( path = "/register/", consumes = "application/json", produces = "application/json" )
    public ResponseEntity< AnswerBody > register( @RequestBody AccountBody body, HttpSession session ) {

        final AnswerBody answerBody = new AnswerBody();

        if ( ( body.getPassword() != null ) && !accountService.has( body.getLogin() ) ) {

            final Account account = accountService.addAccount( body.getLogin(), body.getPassword(), body.getEmail() );
            session.setAttribute( ID_ATTR, account.getId() );
            answerBody.addParameter( ID_ATTR, ( ( Integer ) account.getId() ).toString() );
            answerBody.addParameter( LOGIN_ATTR, account.getLogin() );
            return ResponseEntity.ok( answerBody );
        }

        answerBody.setCode( AnswerBody.STATUS_BAD );
        return ResponseEntity.status( HttpStatus.BAD_REQUEST ).body( answerBody );
    }

    @PostMapping( path = "/login/", consumes = "application/json", produces = "application/json" )
    public ResponseEntity< AnswerBody > login( @RequestBody AccountBody body, HttpSession session ) {

        final Account account = accountService.find( body.getLogin() );
        final AnswerBody answerBody = new AnswerBody();

        if ( ( account != null ) && ( account.passwordMatches( body.getPassword() ) ) ) {

            session.setAttribute( ID_ATTR, account.getId() );
            return ResponseEntity.ok( answerBody );
        }

        answerBody.setCode( AnswerBody.STATUS_FORBIDDEN );
        return ResponseEntity.status( HttpStatus.FORBIDDEN ).body( answerBody );
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

            final Account account = accountService.find( ( Integer ) session.getAttribute( ID_ATTR ) );

            if ( account != null ) {

                answerBody.addParameter( ID_ATTR, ( ( Integer ) account.getId() ).toString() );
                answerBody.addParameter( LOGIN_ATTR, account.getLogin() );
                return ResponseEntity.ok( answerBody );
            }
        }

        answerBody.setCode( AnswerBody.STATUS_NOT_FOUND );
        return ResponseEntity.status( HttpStatus.NOT_FOUND ).body( answerBody );
    }

    private AccountService accountService;
}
