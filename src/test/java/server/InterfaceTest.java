package server;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.RandomStringUtils;
import org.jetbrains.annotations.Nullable;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.MockMvcPrint;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Random;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SuppressWarnings("OverlyBroadThrowsClause")
@SpringBootTest
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc(print = MockMvcPrint.NONE)
public class InterfaceTest {

    @SuppressWarnings("SpringJavaAutowiredMembersInspection")
    @Autowired
    private AccountService accountService;

    @SuppressWarnings("SpringJavaAutowiredMembersInspection")
    @Autowired
    private MockMvc mvc;

    private String generateLogin() {

        final int minLength = 5;
        final int length = new Random().nextInt(10) + minLength;
        return RandomStringUtils.randomAlphanumeric(length) + String.valueOf(System.currentTimeMillis());
    }

    private String generatePassword(boolean isValid) {

        final int minLength = Validator.PASSWORD_MIN_LENGTH;
        final int length = new Random().nextInt(10) + minLength;
        return (isValid) ?
            RandomStringUtils.randomAlphanumeric(length) :
            RandomStringUtils.randomAlphanumeric(minLength - 1);
    }

    private String generateEmail(boolean isValid) {

        final int minLength = 4;
        final int length = new Random().nextInt(6) + minLength;
        final String email = RandomStringUtils.randomAlphanumeric(length) + '@' + RandomStringUtils.randomAlphanumeric(length).toLowerCase() + ".ru";
        return (isValid) ?
            email :
            email.replace('@', '0');
    }

    public void assertAccountFields(@Nullable Account account, String login, String password, String email, int rating) {

        assert account != null;
        Assert.assertEquals(account.getLogin(), login);
        Assert.assertEquals(true, account.passwordMatches(password));
        Assert.assertEquals(account.getEmail(), email);
        Assert.assertEquals(account.getRating(), rating);
    }

    ///////////////////////////////////

    @After
    public void clear() {
        accountService.clear();
    }

    ///////////////////////////////////
    //Registration tests

    @Test
    public void testRegisterLoggedIn() throws Exception {

        final ObjectMapper mapper = new ObjectMapper();
        final AccountData data = new AccountData(generateLogin(), generatePassword(true), generateEmail(true));

        mvc
            .perform(post("/register/")
                .sessionAttr(ApplicationController.SESSION_ATTR, generateLogin())
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(data)))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath(ErrorData.CODE_ATTR).value(ErrorCode.LOG_OUT.toString()));
    }

    @Test
    public void testRegisterNotAllFields() throws Exception {

        final ObjectMapper mapper = new ObjectMapper();
        AccountData data = new AccountData(generateLogin(), generatePassword(true), null);

        mvc
            .perform(post("/register/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(data)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath(ErrorData.CODE_ATTR).value(ErrorCode.INSUFFICIENT.toString()));

        data = new AccountData(generateLogin(), null, generateEmail(true));

        mvc
            .perform(post("/register/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(data)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath(ErrorData.CODE_ATTR).value(ErrorCode.INSUFFICIENT.toString()));

        data = new AccountData(null, generatePassword(true), generateEmail(true));

        mvc
            .perform(post("/register/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(data)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath(ErrorData.CODE_ATTR).value(ErrorCode.INSUFFICIENT.toString()));
    }

    @Test
    public void testRegisterInvalidPassword() throws Exception {

        final ObjectMapper mapper = new ObjectMapper();
        final AccountData data = new AccountData(generateLogin(), generatePassword(false), generateEmail(true));

        mvc
            .perform(post("/register/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(data)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath(ErrorData.CODE_ATTR).value(ErrorCode.INVALID_FIELD.toString()));
    }

    @Test
    public void testRegisterInvalidEmail() throws Exception {

        final ObjectMapper mapper = new ObjectMapper();
        final AccountData data = new AccountData(generateLogin(), generatePassword(true), generateEmail(false));

        mvc
            .perform(post("/register/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(data)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath(ErrorData.CODE_ATTR).value(ErrorCode.INVALID_FIELD.toString()));
    }

    @Test
    public void testRegisterExists() throws Exception {

        final ObjectMapper mapper = new ObjectMapper();
        final String login = generateLogin();
        final AccountData data = new AccountData(login, generatePassword(true), generateEmail(true));

        accountService.createAccount(login, generatePassword(true), generateEmail(true));

        mvc
            .perform(post("/register/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(data)))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath(ErrorData.CODE_ATTR).value(ErrorCode.EXISTS.toString()));
    }

    @Test
    public void testRegisterSuccess() throws Exception {

        final ObjectMapper mapper = new ObjectMapper();
        final AccountData data = new AccountData(generateLogin(), generatePassword(true), generateEmail(true));

        mvc
            .perform(post("/register/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(data)))
            .andExpect(status().isOk())
            .andExpect(jsonPath(AccountData.LOGIN_ATTR).value(data.getLogin()))
            .andExpect(jsonPath(AccountData.EMAIL_ATTR).value(data.getEmail()))
            .andExpect(jsonPath(AccountData.RATING_ATTR).value(0))
            .andExpect(request().sessionAttribute(ApplicationController.SESSION_ATTR, data.getLogin()));

        assertAccountFields(accountService.find(data.getLogin()), data.getLogin(), data.getPassword(), data.getEmail(), 0);
    }

    ///////////////////////////////////
    //Signup tests

    @Test
    public void testSignupLoggedIn() throws Exception {

        final ObjectMapper mapper = new ObjectMapper();
        final AccountData data = new AccountData(generateLogin(), generatePassword(true), null);

        mvc
            .perform(post("/login/")
                .sessionAttr(ApplicationController.SESSION_ATTR, generateLogin())
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(data)))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath(ErrorData.CODE_ATTR).value(ErrorCode.LOG_OUT.toString()));
    }

    @Test
    public void testSignupNotAllFields() throws Exception {

        final ObjectMapper mapper = new ObjectMapper();
        AccountData data = new AccountData(generateLogin(), null, null);

        mvc
            .perform(post("/login/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(data)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath(ErrorData.CODE_ATTR).value(ErrorCode.INSUFFICIENT.toString()));

        data = new AccountData(null, generatePassword(true), null);

        mvc
            .perform(post("/login/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(data)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath(ErrorData.CODE_ATTR).value(ErrorCode.INSUFFICIENT.toString()));
    }

    @Test
    public void testSignupInvalidCredentials() throws Exception {

        final ObjectMapper mapper = new ObjectMapper();
        AccountData data = new AccountData(generateLogin(), generatePassword(true), null);

        mvc
            .perform(post("/login/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(data)))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath(ErrorData.CODE_ATTR).value(ErrorCode.FORBIDDEN.toString()));

        final String password = generatePassword(true);
        final String email = generateEmail(true);
        accountService.createAccount(data.getLogin(), password, email);
        data = new AccountData(data.getLogin(), generatePassword(false), null);

        mvc
            .perform(post("/login/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(data)))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath(ErrorData.CODE_ATTR).value(ErrorCode.FORBIDDEN.toString()));

        data = new AccountData(generateLogin(), password, null);

        mvc
            .perform(post("/login/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(data)))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath(ErrorData.CODE_ATTR).value(ErrorCode.FORBIDDEN.toString()));
    }

    @Test
    public void testSignupSuccess() throws Exception {

        final ObjectMapper mapper = new ObjectMapper();
        final AccountData data = new AccountData(generateLogin(), generatePassword(true), null);
        accountService.createAccount(data.getLogin(), data.getPassword(), generateEmail(true));

        mvc
            .perform(post("/login/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(data)))
            .andExpect(status().isOk())
            .andExpect(request().sessionAttribute(ApplicationController.SESSION_ATTR, data.getLogin()));
    }

    ///////////////////////////////////
    //Change user tests

    @Test
    public void testChangeAccountNotLoggedIn() throws Exception {

        final ObjectMapper mapper = new ObjectMapper();
        final AccountData data = new AccountData(null, null, null);

        mvc
            .perform(post("/change/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(data)))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath(ErrorData.CODE_ATTR).value(ErrorCode.LOG_IN.toString()));
    }

    @Test
    public void testChangeAccountNotFound() throws Exception {

        final ObjectMapper mapper = new ObjectMapper();
        final AccountData data = new AccountData(null, null, null);

        mvc
            .perform(post("/change/")
                .sessionAttr(ApplicationController.SESSION_ATTR, generateLogin())
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(data)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath(ErrorData.CODE_ATTR).value(ErrorCode.NOT_FOUND.toString()));
    }

    @Test
    public void testChangeAccountLoginExists() throws Exception {

        final ObjectMapper mapper = new ObjectMapper();
        final String login = accountService.createAccount(generateLogin(), generatePassword(true), generateEmail(true)).getLogin();
        final String takenLogin = accountService.createAccount(generateLogin(), generatePassword(true), generateEmail(true)).getLogin();
        final AccountData data = new AccountData(takenLogin, null, null);

        mvc
            .perform(post("/change/")
                .sessionAttr(ApplicationController.SESSION_ATTR, login)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(data)))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath(ErrorData.CODE_ATTR).value(ErrorCode.EXISTS.toString()));
    }

    @Test
    public void testChangeAccountInvalidPassword() throws Exception {

        final ObjectMapper mapper = new ObjectMapper();
        final AccountData data = new AccountData(null, generatePassword(false), null);
        final String login = accountService.createAccount(generateLogin(), generatePassword(true), generateEmail(true)).getLogin();

        mvc
            .perform(post("/change/")
                .sessionAttr(ApplicationController.SESSION_ATTR, login)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(data)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath(ErrorData.CODE_ATTR).value(ErrorCode.INVALID_FIELD.toString()));
    }

    @Test
    public void testChangeAccountInvalidEmail() throws Exception {

        final ObjectMapper mapper = new ObjectMapper();
        final AccountData data = new AccountData(null, null, generateEmail(false));
        final String login = accountService.createAccount(generateLogin(), generatePassword(true), generateEmail(true)).getLogin();

        mvc
            .perform(post("/change/")
                .sessionAttr(ApplicationController.SESSION_ATTR, login)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(data)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath(ErrorData.CODE_ATTR).value(ErrorCode.INVALID_FIELD.toString()));
    }

    @Test
    public void testChangeAccountNoChanges() throws Exception {

        final ObjectMapper mapper = new ObjectMapper();
        final String password = generatePassword(true);
        final Account account = accountService.createAccount(generateLogin(), password, generateEmail(true));
        final AccountData data = new AccountData(null, null, null);

        mvc
            .perform(post("/change/")
                .sessionAttr(ApplicationController.SESSION_ATTR, account.getLogin())
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(data)))
            .andExpect(status().isOk())
            .andExpect(jsonPath(AccountData.LOGIN_ATTR).value(account.getLogin()))
            .andExpect(jsonPath(AccountData.EMAIL_ATTR).value(account.getEmail()))
            .andExpect(jsonPath(AccountData.RATING_ATTR).value(0));

        assertAccountFields(accountService.find(account.getLogin()), account.getLogin(), password, account.getEmail(), 0);
    }

    @Test
    public void testChangeAccountSuccess() throws Exception {

        final ObjectMapper mapper = new ObjectMapper();
        final String password = generatePassword(true);
        final Account account = accountService.createAccount(generateLogin(), generatePassword(true), generateEmail(true));
        final AccountData data = new AccountData(generateLogin(), password, generateEmail(true));

        mvc
            .perform(post("/change/")
                .sessionAttr(ApplicationController.SESSION_ATTR, account.getLogin())
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(data)))
            .andExpect(status().isOk())
            .andExpect(jsonPath(AccountData.LOGIN_ATTR).value(data.getLogin()))
            .andExpect(jsonPath(AccountData.EMAIL_ATTR).value(data.getEmail()))
            .andExpect(jsonPath(AccountData.RATING_ATTR).value(0));

        assertAccountFields(accountService.find(account.getLogin()), account.getLogin(), password, account.getEmail(), 0);
    }

    @Test
    public void testChangeAccountSameLogin() throws Exception {

        final ObjectMapper mapper = new ObjectMapper();
        final String password = generatePassword(true);
        final Account account = accountService.createAccount(generateLogin(), generatePassword(true), generateEmail(true));
        final AccountData data = new AccountData(account.getLogin(), password, generateEmail(true));

        mvc
            .perform(post("/change/")
                .sessionAttr(ApplicationController.SESSION_ATTR, account.getLogin())
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(data)))
            .andExpect(status().isOk())
            .andExpect(jsonPath(AccountData.LOGIN_ATTR).value(data.getLogin()))
            .andExpect(jsonPath(AccountData.EMAIL_ATTR).value(data.getEmail()))
            .andExpect(jsonPath(AccountData.RATING_ATTR).value(0));

        assertAccountFields(accountService.find(account.getLogin()), account.getLogin(), password, data.getEmail(), 0);
    }

    ///////////////////////////////////
    //Logout tests

    @Test
    public void testLogoutSuccess() throws Exception {

        mvc
            .perform(post("/logout/")
                .sessionAttr(ApplicationController.SESSION_ATTR, generateLogin()))
            .andExpect(request().sessionAttribute(ApplicationController.SESSION_ATTR, ( Object ) null));
    }

    ///////////////////////////////////
    //Get account info tests

    @Test
    public void testWhoAmINotLoggedIn() throws Exception {

        mvc
            .perform(get("/who-am-i/"))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath(ErrorData.CODE_ATTR).value(ErrorCode.LOG_IN.toString()));
    }

    @Test
    public void testWhoAmINotFound() throws Exception {

        mvc
            .perform(get("/who-am-i/")
                .sessionAttr(ApplicationController.SESSION_ATTR, generateLogin()))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath(ErrorData.CODE_ATTR).value(ErrorCode.NOT_FOUND.toString()));
    }

    @Test
    public void testWhoAmISuccess() throws Exception {

        final Account account = accountService.createAccount(generateLogin(), generatePassword(true), generateEmail(true));
        mvc
            .perform(get("/who-am-i/").sessionAttr(ApplicationController.SESSION_ATTR, account.getLogin()))
            .andExpect(status().isOk())
            .andExpect(jsonPath(AccountData.LOGIN_ATTR).value(account.getLogin()))
            .andExpect(jsonPath(AccountData.EMAIL_ATTR).value(account.getEmail()))
            .andExpect(jsonPath(AccountData.RATING_ATTR).value(0));
    }
}
