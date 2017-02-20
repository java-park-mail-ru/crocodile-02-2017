package server;

@SuppressWarnings( "unused" )
class Account implements Comparable< Account > {

    public Account( int id, String login, String password ) {
        this.id = id;
        this.login = login;
        this.password = password;
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

    public boolean passwordMatches( String password ) {
        return this.password.equals( password );
    }

    public boolean equals( Account account ) {
        return ( this.id == account.id );
    }

    @Override
    public int compareTo( Account account ) {
        return ( this.id >= account.id ) ? ( this.id == account.id ) ? 0 : 1 : -1;
    }

    @Override
    public String toString() {
        return login + " " + password;
    }

    private int id;
    private String login;
    private String password;
}
