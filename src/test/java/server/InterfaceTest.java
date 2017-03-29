package server;

import com.fasterxml.jackson.databind.ObjectMapper;
import database.AccountService;
import database.AccountServiceDatabase;
import org.apache.commons.lang3.RandomStringUtils;
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
import java.util.Random;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SuppressWarnings({"OverlyBroadThrowsClause", "SpringJavaAutowiredMembersInspection"})
@SpringBootTest
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc(print = MockMvcPrint.NONE)
public class InterfaceTest {

    @Autowired
    private AccountServiceDatabase accountService;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    private String generateLogin(@Nullable String mainName) {

        return (mainName != null) ?
            mainName + String.valueOf(System.currentTimeMillis()) :
            RandomStringUtils.randomAlphabetic(5) + String.valueOf(System.currentTimeMillis());
    }

    private String generatePassword(boolean isValid) {

        final String password = String.format(
            "%-" + Validator.PASSWORD_MIN_LENGTH + 's', "correct")
            .replace(' ', '*');

        return (isValid) ?
            password :
            "incorrect".substring(0, Validator.PASSWORD_MIN_LENGTH - 1);
    }

    private String generateEmail(boolean isValid) {

        final int minLength = 5;
        final int length = new Random().nextInt(5) + minLength;
        final String email = RandomStringUtils.randomAlphanumeric(length) +
            '@' + RandomStringUtils.randomAlphanumeric(6).toLowerCase() + ".ru";
        return (isValid) ?
            email :
            email.replace('@', '0');
    }

    public void assertAccountFields(@Nullable Account account, String login, String password, String email, int rating) {

        Assert.assertNotNull(account);
        Assert.assertEquals(account.getLogin(), login);
        Assert.assertEquals(true, account.passwordMatches(password));
        Assert.assertEquals(account.getEmail(), email);
        Assert.assertEquals(account.getRating(), rating);
    }

    ///////////////////////////////////
    //Registration tests

    @Transactional
    @Test
    public void testRegisterLoggedIn() throws Exception {

        final AccountData data = new AccountData(
            generateLogin(null),
            generatePassword(true),
            generateEmail(true));

        mvc
            .perform(post("/register/")
                .sessionAttr(ApplicationController.SESSION_ATTR, generateLogin(null))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(data)))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath(ErrorData.CODE_ATTR).value(ErrorCode.LOG_OUT.toString()));
    }

    @Transactional
    @Test
    public void testRegisterNotAllFields() throws Exception {

        AccountData data = new AccountData(
            generateLogin(null),
            generatePassword(true),
            null);

        mvc
            .perform(post("/register/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(data)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath(ErrorData.CODE_ATTR).value(ErrorCode.INSUFFICIENT.toString()));

        data = new AccountData(
            generateLogin(null),
            null,
            generateEmail(true));

        mvc
            .perform(post("/register/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(data)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath(ErrorData.CODE_ATTR).value(ErrorCode.INSUFFICIENT.toString()));

        data = new AccountData(
            null,
            generatePassword(true),
            generateEmail(true));

        mvc
            .perform(post("/register/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(data)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath(ErrorData.CODE_ATTR).value(ErrorCode.INSUFFICIENT.toString()));
    }

    @Transactional
    @Test
    public void testRegisterInvalidPassword() throws Exception {

        final AccountData data = new AccountData(
            generateLogin(null),
            generatePassword(false),
            generateEmail(true));

        mvc
            .perform(post("/register/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(data)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath(ErrorData.CODE_ATTR).value(ErrorCode.INVALID_FIELD.toString()));
    }

    @Transactional
    @Test
    public void testRegisterInvalidEmail() throws Exception {

        final AccountData data = new AccountData(
            generateLogin(null),
            generatePassword(true),
            generateEmail(false));

        mvc
            .perform(post("/register/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(data)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath(ErrorData.CODE_ATTR).value(ErrorCode.INVALID_FIELD.toString()));
    }

    @Transactional
    @Test
    public void testRegisterExists() throws Exception {

        final String login = generateLogin("existingName");
        final AccountData data = new AccountData(
            login,
            generatePassword(true),
            generateEmail(true));

        accountService.createAccount(login, generatePassword(true), generateEmail(true));

        mvc
            .perform(post("/register/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(data)))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath(ErrorData.CODE_ATTR).value(ErrorCode.EXISTS.toString()));
    }

    @Transactional
    @Test
    public void testRegisterSuccess() throws Exception {

        final AccountData data = new AccountData(
            generateLogin("registerName"),
            generatePassword(true),
            generateEmail(true));

        mvc
            .perform(post("/register/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(data)))
            .andExpect(status().isOk())
            .andExpect(jsonPath(AccountData.LOGIN_ATTR).value(data.getLogin()))
            .andExpect(jsonPath(AccountData.EMAIL_ATTR).value(data.getEmail()))
            .andExpect(jsonPath(AccountData.RATING_ATTR).value(0))
            .andExpect(request().sessionAttribute(ApplicationController.SESSION_ATTR, data.getLogin()));

        assertAccountFields(accountService.findAccount(data.getLogin()), data.getLogin(), data.getPassword(), data.getEmail(), 0);
    }

    ///////////////////////////////////
    //Signup tests

    @Transactional
    @Test
    public void testSignupLoggedIn() throws Exception {

        final AccountData data = new AccountData(
            generateLogin(null),
            generatePassword(true),
            null);

        mvc
            .perform(post("/login/")
                .sessionAttr(ApplicationController.SESSION_ATTR, generateLogin(null))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(data)))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath(ErrorData.CODE_ATTR).value(ErrorCode.LOG_OUT.toString()));
    }

    @Transactional
    @Test
    public void testSignupNotAllFields() throws Exception {

        AccountData data = new AccountData(
            generateLogin(null),
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
            generatePassword(true),
            null);

        mvc
            .perform(post("/login/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(data)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath(ErrorData.CODE_ATTR).value(ErrorCode.INSUFFICIENT.toString()));
    }

    @Transactional
    @Test
    public void testSignupInvalidCredentials() throws Exception {

        AccountData data = new AccountData(
            generateLogin(null),
            generatePassword(true),
            null);

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

        data = new AccountData(
            generateLogin(null),
            password,
            null);

        mvc
            .perform(post("/login/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(data)))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath(ErrorData.CODE_ATTR).value(ErrorCode.FORBIDDEN.toString()));
    }

    @Transactional
    @Test
    public void testSignupSuccess() throws Exception {

        final AccountData data = new AccountData(
            generateLogin("loginName"),
            generatePassword(true),
            null);
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

    @Transactional
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

    @Transactional
    @Test
    public void testChangeAccountNotFound() throws Exception {

        final AccountData data = new AccountData(null, null, null);

        mvc
            .perform(post("/change/")
                .sessionAttr(ApplicationController.SESSION_ATTR, generateLogin("noSuchName"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(data)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath(ErrorData.CODE_ATTR).value(ErrorCode.NOT_FOUND.toString()));
    }

    @Transactional
    @Test
    public void testChangeAccountLoginExists() throws Exception {

        final Account account = accountService.createAccount(
            generateLogin("initialName"),
            generatePassword(true),
            generateEmail(true));
        Assert.assertNotNull(account);
        final String login = account.getLogin();

        final Account takenAccount = accountService.createAccount(
            generateLogin("existingName"),
            generatePassword(true),
            generateEmail(true));
        Assert.assertNotNull(takenAccount);
        final String takenLogin = takenAccount.getLogin();

        final AccountData data = new AccountData(takenLogin, null, null);

        mvc
            .perform(post("/change/")
                .sessionAttr(ApplicationController.SESSION_ATTR, login)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(data)))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath(ErrorData.CODE_ATTR).value(ErrorCode.EXISTS.toString()));
    }

    @Transactional
    @Test
    public void testChangeAccountInvalidPassword() throws Exception {

        final AccountData data = new AccountData(
            null,
            generatePassword(false),
            null);
        final Account account = accountService.createAccount(
            generateLogin(null),
            generatePassword(true),
            generateEmail(true));
        Assert.assertNotNull(account);
        final String login = account.getLogin();
        mvc
            .perform(post("/change/")
                .sessionAttr(ApplicationController.SESSION_ATTR, login)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(data)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath(ErrorData.CODE_ATTR).value(ErrorCode.INVALID_FIELD.toString()));
    }

    @Transactional
    @Test
    public void testChangeAccountInvalidEmail() throws Exception {

        final AccountData data = new AccountData(null, null, generateEmail(false));
        final Account account = accountService.createAccount(
            generateLogin(null),
            generatePassword(true),
            generateEmail(true));
        Assert.assertNotNull(account);
        final String login = account.getLogin();

        mvc
            .perform(post("/change/")
                .sessionAttr(ApplicationController.SESSION_ATTR, login)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(data)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath(ErrorData.CODE_ATTR).value(ErrorCode.INVALID_FIELD.toString()));
    }

    @Transactional
    @Test
    public void testChangeAccountNoChanges() throws Exception {

        final String password = generatePassword(true);
        final Account account = accountService.createAccount(
            generateLogin("initialName"),
            password,
            generateEmail(true));
        final AccountData data = new AccountData(null, null, null);
        Assert.assertNotNull(account);

        mvc
            .perform(post("/change/")
                .sessionAttr(ApplicationController.SESSION_ATTR, account.getLogin())
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(data)))
            .andExpect(status().isOk())
            .andExpect(jsonPath(AccountData.LOGIN_ATTR).value(account.getLogin()))
            .andExpect(jsonPath(AccountData.EMAIL_ATTR).value(account.getEmail()))
            .andExpect(jsonPath(AccountData.RATING_ATTR).value(0));

        assertAccountFields(accountService.findAccount(account.getLogin()), account.getLogin(), password, account.getEmail(), 0);
    }

    @Transactional
    @Test
    public void testChangeAccountSuccess() throws Exception {

        final String password = generatePassword(true);
        final Account account = accountService.createAccount(
            generateLogin("initialName"),
            generatePassword(true),
            generateEmail(true));
        final AccountData data = new AccountData(
            generateLogin("newName"),
            password,
            generateEmail(true));
        Assert.assertNotNull(account);

        mvc
            .perform(post("/change/")
                .sessionAttr(ApplicationController.SESSION_ATTR, account.getLogin())
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(data)))
            .andExpect(status().isOk())
            .andExpect(jsonPath(AccountData.LOGIN_ATTR).value(data.getLogin()))
            .andExpect(jsonPath(AccountData.EMAIL_ATTR).value(data.getEmail()))
            .andExpect(jsonPath(AccountData.RATING_ATTR).value(0));

        assertAccountFields(accountService.findAccount(data.getLogin()), data.getLogin(), password, data.getEmail(), 0);
    }

    @Transactional
    @Test
    public void testChangeAccountSameLogin() throws Exception {

        final String password = generatePassword(true);
        final Account account = accountService.createAccount(
            generateLogin("initialName"),
            generatePassword(true),
            generateEmail(true));
        Assert.assertNotNull(account);
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

        assertAccountFields(accountService.findAccount(account.getLogin()), account.getLogin(), password, data.getEmail(), 0);
    }

    ///////////////////////////////////
    //Logout tests

    @Transactional
    @Test
    public void testLogoutSuccess() throws Exception {

        mvc
            .perform(post("/logout/")
                .sessionAttr(ApplicationController.SESSION_ATTR, generateLogin("sessionName")))
            .andExpect(request().sessionAttribute(ApplicationController.SESSION_ATTR, ( Object ) null));
    }

    ///////////////////////////////////
    //Get account info tests

    @Transactional
    @Test
    public void testWhoAmINotLoggedIn() throws Exception {

        mvc
            .perform(get("/who-am-i/"))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath(ErrorData.CODE_ATTR).value(ErrorCode.LOG_IN.toString()));
    }

    @Transactional
    @Test
    public void testWhoAmINotFound() throws Exception {

        mvc
            .perform(get("/who-am-i/")
                .sessionAttr(ApplicationController.SESSION_ATTR, generateLogin("sessionName")))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath(ErrorData.CODE_ATTR).value(ErrorCode.NOT_FOUND.toString()));
    }

    @Transactional
    @Test
    public void testWhoAmISuccess() throws Exception {

        final Account account = accountService.createAccount(
            generateLogin("yourName"),
            generatePassword(true),
            generateEmail(true));
        Assert.assertNotNull(account);
        mvc
            .perform(get("/who-am-i/").sessionAttr(ApplicationController.SESSION_ATTR, account.getLogin()))
            .andExpect(status().isOk())
            .andExpect(jsonPath(AccountData.LOGIN_ATTR).value(account.getLogin()))
            .andExpect(jsonPath(AccountData.EMAIL_ATTR).value(account.getEmail()))
            .andExpect(jsonPath(AccountData.RATING_ATTR).value(0));
    }

    ///////////////////////////////////
    //Get best tests

    @Transactional
    @Test
    public void testGetBestEmpty() throws Exception {

        mvc
            .perform(get("/best/"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(0)));
    }

    @Transactional
    @Test
    public void testGetBestSuccess() throws Exception {

        final ArrayList<Account> createdAccounts = new ArrayList<>();

        for (int i = 0; i < AccountService.BEST_COUNT * 2; ++i) {
            Account account = accountService.createAccount(
                generateLogin("best#" + String.valueOf(i)),
                generateEmail(true),
                generatePassword(true));
            Assert.assertNotNull(account);
            account = accountService.updateAccount(account.getLogin(), null, null, null, i);
            Assert.assertNotNull(account);
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
