package uk.gov.ida.rp.testrp.tokenservice;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.ida.rp.testrp.TestRpConfiguration;
import uk.gov.ida.rp.testrp.domain.AccessToken;
import uk.gov.ida.rp.testrp.exceptions.InvalidAccessTokenException;
import uk.gov.ida.rp.testrp.exceptions.TokenServiceUnavailableException;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TokenServiceClientTest {

    TokenServiceClient tokenServiceClient;
    @Mock
    private TestRpConfiguration configuration;
    @Mock
    Client client;

    @Before
    public void setUp() throws Exception {
        tokenServiceClient = new TokenServiceClient(configuration,client);
        when(configuration.getTokenServiceUser()).thenReturn("user");
        when(configuration.getTokenServicePassword()).thenReturn("password");
        when(configuration.getTokenServiceUrl()).thenReturn("http://bla");
    }

    @Test(expected = TokenServiceUnavailableException.class)
    public void shouldThrowTokenServiceUnavailableException_whenTokenServiceIsDown() throws Exception {
        AccessToken accessToken = new AccessToken("nicetoken");
        doThrow(ProcessingException.class).when(client).target((any(String.class)));

        tokenServiceClient.validateToken(accessToken);
    }

    @Test
    public void shouldThrowInvalidAccessTokenException_whenTokenFailsValidation() throws Exception {
        String tokenValue = "invalid-token";
        AccessToken accessToken = new AccessToken(tokenValue);
        doThrow(WebApplicationException.class).when(client).target(any(String.class));
        try {
            tokenServiceClient.validateToken(accessToken);
        }
        catch(InvalidAccessTokenException e){
            assertThat(e.getMessage()).isEqualTo("Token service failed validation.");
        }
    }
}
