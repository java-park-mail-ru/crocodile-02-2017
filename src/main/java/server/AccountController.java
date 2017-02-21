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
                @JsonProperty( ID_ATTR ) Integer id,
                @JsonProperty( LOGIN_ATTR ) String login,
                @JsonProperty( PASSWORD_ATTR ) String password,
                @JsonProperty( EMAIL_ATTR ) String email ) {

            this.id = id;
            this.login = login;
            this.password = password;
            this.email = email;
        }

        public Integer getId() {
            return id;
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

        private Integer id;
        private String login;
        private String password;
        private String email;
    }

    @SuppressWarnings( "unused" )
    private static class AnswerBody {

        AnswerBody() {
            parameters = new HashMap< String, String >();
        }

        public void addParameter( @NotNull String name, String value ) {
            parameters.put( name, value );
        }

        public HashMap< String, String > getParameters() {
            return new HashMap< String, String >( parameters );
        }

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

        return ResponseEntity.status( HttpStatus.BAD_REQUEST ).body( answerBody );
    }

    @PostMapping( path = "/login/", consumes = "application/json", produces = "application/json" )
    public ResponseEntity< AnswerBody > login( @RequestBody AccountBody body, HttpSession session ) {

        final AnswerBody answerBody = new AnswerBody();
        final Account account = accountService.find( body.getLogin() );

        if ( ( account != null ) && ( account.passwordMatches( body.getPassword() ) ) ) {

            session.setAttribute( ID_ATTR, account.getId() );
            return ResponseEntity.ok( answerBody );
        }

        return ResponseEntity.status( HttpStatus.FORBIDDEN ).body( answerBody );
    }

    /*!!!!!!!!!!!!!!!!!!!!!!
    @PostMapping( path = "/change/", consumes = "application/json", produces = "application/json" )
    public ResponseEntity< AnswerBody > changeUser( @RequestBody AccountBody body, HttpSession session ) {

        final AnswerBody answerBody = new AnswerBody();

        if ( body.getId() == null ) {

            return ResponseEntity.status( HttpStatus.NOT_FOUND ).body( answerBody );
        }

        final Account account = accountService.find( body.getId() );

        if ( ( account != null ) && ( account.passwordMatches( body.getPassword() ) ) ) {

            if ( account.getLogin() != null ) {

                if ( accountService.find( account.getLogin() ) != null ) {

                    answerBody.addParameter( LOGIN_ATTR, account.getLogin() );
                    return ResponseEntity.status( HttpStatus.BAD_REQUEST ).body( answerBody );
                }


            }

            session.setAttribute( ID_ATTR, account.getId() );
            return ResponseEntity.ok( answerBody );
        }

        return ResponseEntity.status( HttpStatus.FORBIDDEN ).body( answerBody );
    }*/

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

        return ResponseEntity.status( HttpStatus.NOT_FOUND ).body( answerBody );
    }

    private AccountService accountService;
}
