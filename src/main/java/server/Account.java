package server;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings( "unused" )
public class Account {

    private static final AtomicInteger ID_GENERATOR = new AtomicInteger( 0 );

    public Account(
            @NotNull String login,
            @NotNull String password,
            @NotNull String email ) {
        this.id = ID_GENERATOR.getAndIncrement();
        this.login = login;
        this.password = password;
        this.email = email;
    }

    public int getId() {
        return id;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }

    public boolean passwordMatches( String other ) {
        return password.equals( other );
    }

    @Override
    public String toString() {
        return login + " :" + email;
    }

    private int id;
    private String login;
    private String password;
    private String email;
}
