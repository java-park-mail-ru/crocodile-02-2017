package server;

import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AccountService {

    public Account addAccount( String login, String password ) {
        Account account = new Account( lastKey++, login, password );
        accounts.add( account );
        return account;
    }

    public int getIdFor( String login, String password ) {

        if ( ( login == null ) || ( password == null ) ) {

            return -1;
        }

        for ( Account account: accounts ) {

            if ( account.getLogin().equals( login ) && account.getPassword().equals( password ) ) {

                return account.getId();
            }
        }

        return -1;
    }

    public boolean anyMatch( String login ) {

        if ( login == null ) {

            return false;
        }

        for ( Account account: accounts ) {

            if ( account.getLogin().equals( login ) ) {

                return true;
            }
        }

        return false;
    }

    public Account find( int id ) {

        for ( Account account: accounts ) {

            if ( account.getId() == id ) {

                return account;
            }
        }

        return null;
    }

    public TreeSet< Account > accounts = new TreeSet< Account >();
    private int lastKey = 0;
}
