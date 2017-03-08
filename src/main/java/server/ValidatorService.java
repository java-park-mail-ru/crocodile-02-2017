package server;

import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
public class ValidatorService {

    private static final int PASSWORD_MIN_LENGTH = 6;
    private static final String EMAIL_REGEX =
            "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@" +
                    "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

    private Pattern emailPattern;

    public ValidatorService() {
        emailPattern = Pattern.compile(EMAIL_REGEX);
    }

    public boolean hasRegistrationFields(AccountData accountData) {
        return (accountData.getLogin() != null) && (accountData.getPassword() != null) && (accountData.getEmail() != null);
    }

    public boolean hasLoggingInFields(AccountData accountData) {
        return (accountData.getLogin() != null) && (accountData.getPassword() != null);
    }

    public boolean passwordValid(AccountData accountData) {
        final String password = accountData.getPassword();
        return (password != null) && (password.length() >= PASSWORD_MIN_LENGTH);
    }

    public boolean emailValid(AccountData accountData) {
        final String email = accountData.getEmail();
        return (email != null) && emailPattern.matcher(email).matches();
    }


}
