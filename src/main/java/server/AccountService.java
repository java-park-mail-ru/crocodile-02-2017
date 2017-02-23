package server;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class AccountService {

    public AccountService() {
        accounts = new HashMap< String, Account >();
    }

    private static final AtomicInteger ID_GENERATOR = new AtomicInteger( 0 );
    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();

    @SuppressWarnings( "unused" )
    public final class Account {

        private Account(
                @NotNull String login,
                @NotNull String password,
                @NotNull String email ) {
            this.id = ID_GENERATOR.getAndIncrement();
            this.login = login;
            this.passwordHash = ENCODER.encode( password );
            this.email = email;
        }

        public int getId() {
            return id;
        }

        public void setLogin( @NotNull String login ) {

            AccountService.this.accounts.remove( this.login );
            this.login = login;
            AccountService.this.accounts.put( login, this );
        }

        public @NotNull String getLogin() {
            return login;
        }

        public void setPasswordHash( @NotNull String password ) {
            this.passwordHash = ENCODER.encode( password );
        }

        public @NotNull String getPasswordHash() {
            return passwordHash;
        }

        public void setEmail( @NotNull String email ) {
            this.email = email;
        }

        public @NotNull String getEmail() {
            return email;
        }

        public void setProperties( String login, String password, String email ) {

            if ( login != null ) {
                setLogin( login );
            }
            if ( password != null ) {
                this.passwordHash = password;
            }
            if ( email != null ) {
                this.email = email;
            }
        }

        public boolean passwordMatches( String password ) {
            return ENCODER.matches( password, passwordHash );
        }

        @Override
        public String toString() {
            return login + ": " + email;
        }

        private int id;
        private @NotNull String login;
        private @NotNull String passwordHash;
        private @NotNull String email;
    }

    public @NotNull Account addAccount(
            @NotNull String login,
            @NotNull String password,
            @NotNull String email ) {
        final Account account = new Account( login, password, email );
        accounts.put( login, account );
        return account;
    }

    public @Nullable Account find( int id ) {

        for ( Account account : accounts.values() ) {

            if ( account.getId() == id ) {

                return account;
            }
        }

        return null;
    }

    public @Nullable Account find( String login ) {

        return accounts.get( login );
    }

    public boolean has( String login ) {
        return ( this.find( login ) != null );
    }

    private @NotNull HashMap< String, Account > accounts;
}
