package server;

import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.util.*;

@Service
public class AccountService {

    public AccountService() {
        accounts = new TreeSet< Account >();
        lastId = 0;
    }

    public Account addAccount( @NotNull String login, @NotNull String password ) {

        if ( !this.has( login ) ) {

            Account account = new Account( lastId++, login, password );
            accounts.add( account );
            return account;
        }

        return null;
    }

    public Account find( int id ) {

        for ( Account account: accounts ) {

            if ( account.getId() == id ) {

                return account;
            }
        }

        return null;
    }

    public Account find( String login ) {

        if ( login == null ) {
            return null;
        }

        for ( Account account: accounts ) {

            if ( account.getLogin().equals( login ) ) {

                return account;
            }
        }

        return null;
    }

    public boolean has( String login ) {
        return ( this.find( login ) != null );
    }

    private TreeSet< Account > accounts;
    private int lastId;
}
