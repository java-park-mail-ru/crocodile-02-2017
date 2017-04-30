package server;

import com.fasterxml.jackson.databind.ObjectMapper;
import database.AccountService;
import database.AccountServiceDb;
import entities.Account;
import httpmessages.AccountData;
import httpmessages.ErrorCode;
import httpmessages.ErrorData;
import org.jetbrains.annotations.Nullable;
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
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SuppressWarnings({"OverlyBroadThrowsClause"})
@SpringBootTest
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc(print = MockMvcPrint.NONE)
@Transactional
public class InterfaceTest {

    private static final String CORRECT_PASSWORD = "correct";
    private static final String INCORRECT_PASSWORD = "incor";

    private static final String CORRECT_EMAIL = "correct@mail.ru";
    private static final String INCORRECT_EMAIL = "incorrectmail.ru";

    @Autowired
    private AccountServiceDb accountService;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    public void assertAccountFields(@Nullable Account account, String login, String password, String email, int rating) {

        Assert.assertNotNull(account);
        Assert.assertEquals(account.getLogin(), login);
        Assert.assertEquals(true, account.passwordMatches(password));
        Assert.assertEquals(account.getEmail(), email);
        Assert.assertEquals(account.getRating(), rating);
    }

    ///////////////////////////////////
    //Registration tests

    @Test
    public void testRegisterLoggedIn() throws Exception {

        final AccountData data = new AccountData(
            "anyName1",
            CORRECT_PASSWORD,
            CORRECT_EMAIL);

        mvc
            .perform(post("/register/")
                .sessionAttr(ApplicationController.SESSION_LOGIN_ATTR, "anyName2")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(data)))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath(ErrorData.CODE_ATTR).value(ErrorCode.LOG_OUT.toString()));
    }

    @Test
    public void testRegisterNotAllFields() throws Exception {

        AccountData data = new AccountData(
            "anyName",
            CORRECT_PASSWORD,
            null);

        mvc
            .perform(post("/register/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(data)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath(ErrorData.CODE_ATTR).value(ErrorCode.INSUFFICIENT.toString()));

        data = new AccountData(
            "anyName",
            null,
            CORRECT_EMAIL);

        mvc
            .perform(post("/register/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(data)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath(ErrorData.CODE_ATTR).value(ErrorCode.INSUFFICIENT.toString()));

        data = new AccountData(
            null,
            CORRECT_PASSWORD,
            CORRECT_EMAIL);

        mvc
            .perform(post("/register/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(data)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath(ErrorData.CODE_ATTR).value(ErrorCode.INSUFFICIENT.toString()));
    }

    @Test
    public void testRegisterInvalidPassword() throws Exception {

        final AccountData data = new AccountData(
            "anyName",
            INCORRECT_PASSWORD,
            CORRECT_EMAIL);

        mvc
            .perform(post("/register/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(data)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath(ErrorData.CODE_ATTR).value(ErrorCode.INVALID_FIELD.toString()));
    }

    @Test
    public void testRegisterInvalidEmail() throws Exception {

        final AccountData data = new AccountData(
            "anyName",
            CORRECT_PASSWORD,
            INCORRECT_EMAIL);

        mvc
            .perform(post("/register/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(data)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath(ErrorData.CODE_ATTR).value(ErrorCode.INVALID_FIELD.toString()));
    }

    @Test
    public void testRegisterExists() throws Exception {

        final String login = "existingName";
        final AccountData data = new AccountData(
            login,
            CORRECT_PASSWORD,
            CORRECT_EMAIL);

        accountService.createAccount(login, CORRECT_PASSWORD, CORRECT_EMAIL);

        mvc
            .perform(post("/register/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(data)))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath(ErrorData.CODE_ATTR).value(ErrorCode.EXISTS.toString()));
    }

    @Test
    public void testRegisterSuccess() throws Exception {

        final AccountData data = new AccountData(
            "registerName",
            CORRECT_PASSWORD,
            CORRECT_EMAIL);

        mvc
            .perform(post("/register/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(data)))
            .andExpect(status().isOk())
            .andExpect(jsonPath(AccountData.LOGIN_ATTR).value(data.getLogin()))
            .andExpect(jsonPath(AccountData.EMAIL_ATTR).value(data.getEmail()))
            .andExpect(jsonPath(AccountData.RATING_ATTR).value(0))
            .andExpect(request().sessionAttribute(ApplicationController.SESSION_LOGIN_ATTR, data.getLogin()));

        assertAccountFields(accountService.findAccount(data.getLogin()),
            data.getLogin(), data.getPassword(), data.getEmail(), 0);
    }

    ///////////////////////////////////
    //SignIn tests

    @Test
    public void testSignInLoggedIn() throws Exception {

        final AccountData data = new AccountData(
            "anyName1",
            CORRECT_PASSWORD,
            null);

        mvc
            .perform(post("/login/")
                .sessionAttr(ApplicationController.SESSION_LOGIN_ATTR, "anyName2")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(data)))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath(ErrorData.CODE_ATTR).value(ErrorCode.LOG_OUT.toString()));
    }

    @Test
    public void testSignInNotAllFields() throws Exception {

        AccountData data = new AccountData(
            "anyName",
            null,
            null);

        mvc
            .perform(post("/login/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(data)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath(ErrorData.CODE_ATTR).value(ErrorCode.INSUFFICIENT.toString()));

        data = new AccountData(
            null,
            CORRECT_PASSWORD,
            null);

        mvc
            .perform(post("/login/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(data)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath(ErrorData.CODE_ATTR).value(ErrorCode.INSUFFICIENT.toString()));
    }

    @Test
    public void testSignInInvalidCredentials() throws Exception {

        AccountData data = new AccountData(
            "anyName",
            CORRECT_PASSWORD,
            null);

        mvc
            .perform(post("/login/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(data)))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath(ErrorData.CODE_ATTR).value(ErrorCode.FORBIDDEN.toString()));

        final String password = CORRECT_PASSWORD;
        accountService.createAccount(data.getLogin(), password, CORRECT_EMAIL);
        data = new AccountData(data.getLogin(), password + '0', null);

        mvc
            .perform(post("/login/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(data)))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath(ErrorData.CODE_ATTR).value(ErrorCode.FORBIDDEN.toString()));

        data = new AccountData(
            "anyName2",
            password,
            null);

        mvc
            .perform(post("/login/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(data)))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath(ErrorData.CODE_ATTR).value(ErrorCode.FORBIDDEN.toString()));
    }

    @Test
    public void testSignInSuccess() throws Exception {

        final AccountData data = new AccountData(
            "loginName",
            CORRECT_PASSWORD,
            null);
        accountService.createAccount(data.getLogin(), data.getPassword(), CORRECT_EMAIL);

        mvc
            .perform(post("/login/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(data)))
            .andExpect(status().isOk())
            .andExpect(request().sessionAttribute(ApplicationController.SESSION_LOGIN_ATTR, data.getLogin()));
    }

    ///////////////////////////////////
    //Change user tests

    @Test
    public void testChangeAccountNotLoggedIn() throws Exception {

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

        final AccountData data = new AccountData(null, null, null);

        mvc
            .perform(post("/change/")
                .sessionAttr(ApplicationController.SESSION_LOGIN_ATTR, "noSuchName")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(data)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath(ErrorData.CODE_ATTR).value(ErrorCode.NOT_FOUND.toString()));
    }

    @Test
    public void testChangeAccountLoginExists() throws Exception {

        final Account account = accountService.createAccount(
            "initialName",
            CORRECT_PASSWORD,
            CORRECT_EMAIL);
        final String login = account.getLogin();

        final Account takenAccount = accountService.createAccount(
            "existingName",
            CORRECT_PASSWORD,
            CORRECT_EMAIL);

        final AccountData data = new AccountData(takenAccount.getLogin(), null, null);

        mvc
            .perform(post("/change/")
                .sessionAttr(ApplicationController.SESSION_LOGIN_ATTR, login)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(data)))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath(ErrorData.CODE_ATTR).value(ErrorCode.EXISTS.toString()));
    }

    @Test
    public void testChangeAccountInvalidPassword() throws Exception {

        final AccountData data = new AccountData(
            null,
            INCORRECT_PASSWORD,
            null);

        final Account account = accountService.createAccount(
            "anyName",
            CORRECT_PASSWORD,
            CORRECT_EMAIL);
        final String login = account.getLogin();
        mvc
            .perform(post("/change/")
                .sessionAttr(ApplicationController.SESSION_LOGIN_ATTR, login)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(data)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath(ErrorData.CODE_ATTR).value(ErrorCode.INVALID_FIELD.toString()));
    }

    @Test
    public void testChangeAccountInvalidEmail() throws Exception {

        final AccountData data = new AccountData(
            null,
            null,
            INCORRECT_EMAIL);

        final Account account = accountService.createAccount(
            "AnyName",
            CORRECT_PASSWORD,
            CORRECT_EMAIL);
        final String login = account.getLogin();

        mvc
            .perform(post("/change/")
                .sessionAttr(ApplicationController.SESSION_LOGIN_ATTR, login)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(data)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath(ErrorData.CODE_ATTR).value(ErrorCode.INVALID_FIELD.toString()));
    }

    @Test
    public void testChangeAccountNoChanges() throws Exception {

        final String password = CORRECT_PASSWORD;
        final Account account = accountService.createAccount(
            "initialName",
            password,
            CORRECT_EMAIL);

        final AccountData data = new AccountData(null, null, null);

        mvc
            .perform(post("/change/")
                .sessionAttr(ApplicationController.SESSION_LOGIN_ATTR, account.getLogin())
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(data)))
            .andExpect(status().isOk())
            .andExpect(jsonPath(AccountData.LOGIN_ATTR).value(account.getLogin()))
            .andExpect(jsonPath(AccountData.EMAIL_ATTR).value(account.getEmail()))
            .andExpect(jsonPath(AccountData.RATING_ATTR).value(0));

        assertAccountFields(accountService.findAccount(account.getLogin()),
            account.getLogin(), password, account.getEmail(), 0);
    }

    @Test
    public void testChangeAccountSuccess() throws Exception {

        final Account account = accountService.createAccount(
            "initialName",
            "initialPassword",
            "initialMail@mail.ru");

        final String newPassword = "initialPassword";
        final AccountData data = new AccountData(
            "newName",
            newPassword,
            "newMail@mail.ru");

        mvc
            .perform(post("/change/")
                .sessionAttr(ApplicationController.SESSION_LOGIN_ATTR, account.getLogin())
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(data)))
            .andExpect(status().isOk())
            .andExpect(jsonPath(AccountData.LOGIN_ATTR).value(data.getLogin()))
            .andExpect(jsonPath(AccountData.EMAIL_ATTR).value(data.getEmail()))
            .andExpect(jsonPath(AccountData.RATING_ATTR).value(0));

        assertAccountFields(accountService.findAccount(data.getLogin()),
            data.getLogin(), newPassword, data.getEmail(), 0);
    }

    @Test
    public void testChangeAccountSameLogin() throws Exception {

        final Account account = accountService.createAccount(
            "initialName",
            "initialPassword",
            "initialMail@mail.ru");

        final String newPassword = "newPassword";
        final AccountData data = new AccountData(
            account.getLogin(),
            newPassword,
            "newMail@mail.ru");

        mvc
            .perform(post("/change/")
                .sessionAttr(ApplicationController.SESSION_LOGIN_ATTR, account.getLogin())
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(data)))
            .andExpect(status().isOk())
            .andExpect(jsonPath(AccountData.LOGIN_ATTR).value(data.getLogin()))
            .andExpect(jsonPath(AccountData.EMAIL_ATTR).value(data.getEmail()))
            .andExpect(jsonPath(AccountData.RATING_ATTR).value(0));

        assertAccountFields(accountService.findAccount(account.getLogin()),
            account.getLogin(), newPassword, data.getEmail(), 0);
    }

    ///////////////////////////////////
    //Logout tests

    @Test
    public void testLogoutSuccess() throws Exception {

        mvc
            .perform(post("/logout/")
                .sessionAttr(ApplicationController.SESSION_LOGIN_ATTR, "anyName"))
            .andExpect(request().sessionAttribute(ApplicationController.SESSION_LOGIN_ATTR, ( Object ) null));
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
                .sessionAttr(ApplicationController.SESSION_LOGIN_ATTR, "noSuchName"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath(ErrorData.CODE_ATTR).value(ErrorCode.NOT_FOUND.toString()));
    }

    @Test
    public void testWhoAmISuccess() throws Exception {

        final Account account = accountService.createAccount(
            "yourName",
            CORRECT_PASSWORD,
            CORRECT_EMAIL);

        mvc
            .perform(get("/who-am-i/").sessionAttr(ApplicationController.SESSION_LOGIN_ATTR, account.getLogin()))
            .andExpect(status().isOk())
            .andExpect(jsonPath(AccountData.LOGIN_ATTR).value(account.getLogin()))
            .andExpect(jsonPath(AccountData.EMAIL_ATTR).value(account.getEmail()))
            .andExpect(jsonPath(AccountData.RATING_ATTR).value(0));
    }

    ///////////////////////////////////
    //Get best tests

    @Test
    public void testGetBestEmpty() throws Exception {

        mvc
            .perform(get("/best/"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    public void testGetBestSuccess() throws Exception {

        final ArrayList<Account> createdAccounts = new ArrayList<>();

        for (int i = 0; i < AccountService.BEST_COUNT * 2; ++i) {
            Account account = accountService.createAccount(
                "best#" + String.valueOf(i),
                CORRECT_PASSWORD,
                CORRECT_EMAIL);
            account = accountService.updateAccountRating(account.getLogin(), i);
            createdAccounts.add(account);
        }

        final ResultActions result = mvc
            .perform(get("/best/"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(AccountService.BEST_COUNT)));

        for (int i = 0; i < AccountService.BEST_COUNT; ++i) {

            final String path = String.format("$[%1$d].", i);
            final Account account = createdAccounts.get(createdAccounts.size() - i - 1);
            result
                .andExpect(jsonPath(path + AccountData.LOGIN_ATTR).value(account.getLogin()))
                .andExpect(jsonPath(path + AccountData.EMAIL_ATTR).value(account.getEmail()))
                .andExpect(jsonPath(path + AccountData.RATING_ATTR).value(account.getRating()));
        }
    }
}
