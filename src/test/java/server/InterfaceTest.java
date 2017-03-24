package server;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;


@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
@WebAppConfiguration
public class InterfaceTest {

    @SuppressWarnings("unused")
    @Mock
    private MockHttpSession session;

    @SuppressWarnings("unused")
    @MockBean
    private AccountService accountService;

    @SuppressWarnings("SpringJavaAutowiredMembersInspection")
    @Autowired
    private TestRestTemplate restTemplate;


    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testLoginSuccess() {
    }

    @Test
    public void testWhoAmINotLoggedIn() {

        final ResponseEntity response = restTemplate.getForEntity("/who-am-i/", Object.class);
        Assert.assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    /*@Test
    public void testWhoAmINotFound() {

        when( session.getAttribute( ApplicationController.ID_ATTR ) ).thenReturn( "1" );
        when( accountService.find( anyInt() ) ).thenReturn( null );
        final ResponseEntity response = restTemplate.getForEntity( "/who-am-i/", Object.class );
        Assert.assertEquals( HttpStatus.NOT_FOUND, response.getStatusCode() );
    }*/
}
