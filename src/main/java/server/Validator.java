package server;

import java.util.regex.Pattern;

public final class Validator {

    public static final int PASSWORD_MIN_LENGTH = 6;
    public static final String EMAIL_REGEX =
        "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@" +
            "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

    private static Pattern emailPattern = Pattern.compile(EMAIL_REGEX);

    private Validator() {
    }

    public static boolean checkRegistrationFields(AccountData accountData) {
        return (accountData.getLogin() != null) && (accountData.getPassword() != null) && (accountData.getEmail() != null);
    }

    public static boolean checkLoggingInFields(AccountData accountData) {
        return (accountData.getLogin() != null) && (accountData.getPassword() != null);
    }

    public static boolean checkPassword(AccountData accountData) {
        final String password = accountData.getPassword();
        return (password != null) && (password.length() >= PASSWORD_MIN_LENGTH);
    }

    public static boolean checkEmail(AccountData accountData) {
        final String email = accountData.getEmail();
        return (email != null) && emailPattern.matcher(email).matches();
    }
}
