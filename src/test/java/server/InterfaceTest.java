package server;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.After;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


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

    ///////////////////////////////////
    //Registration tests
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
    public void testRegisterExists() throws Exception {

        final ObjectMapper mapper = new ObjectMapper();
        final String username = generateLogin();
        final AccountData data = new AccountData(username, generatePassword(true), generateEmail(true));

        accountService.createAccount(username, generatePassword(true), generateEmail(true));

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
            .andExpect(jsonPath(AccountData.RATING_ATTR).value(0));
    }

    ///////////////////////////////////
    //Signup tests


    ///////////////////////////////////
    //Get current user tests

    @Test
    public void testWhoAmINotLoggedIn() throws Exception {

        mvc
            .perform(get("/who-am-i/"))
            .andExpect(status().isForbidden());
    }

    @Test
    public void testWhoAmINotFound() throws Exception {

        final String username = "a";
        mvc
            .perform(get("/who-am-i/").sessionAttr(ApplicationController.SESSION_ATTR, username))
            .andExpect(status().isNotFound());
    }

    @Test
    public void testWhoAmISuccess() throws Exception {

        final String username = "a";
        accountService.createAccount(username, "b", "c");
        mvc
            .perform(get("/who-am-i/").sessionAttr(ApplicationController.SESSION_ATTR, username))
            .andExpect(status().isOk())
            .andExpect(jsonPath("login").value(username));
    }

    @After
    public void clear() {
        accountService.clear();
    }
}
