package uk.gov.ida.rp.testrp.tokenservice;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.ida.rp.testrp.domain.AccessToken;
import uk.gov.ida.rp.testrp.exceptions.InvalidAccessTokenException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TokenServiceAccessValidatorTest {

    TokenServiceAccessValidator tokenServiceAccessValidator;

    @Mock
    private TokenServiceClient tokenClient;

    @Before
    public void setUp() throws Exception {
        tokenServiceAccessValidator = new TokenServiceAccessValidator(tokenClient);
    }

    @Test
    public void shouldThrowInvalidAccessTokenException_whenTokenServiceFailsValidation() throws Exception {
        String tokenValue = "abc";
        AccessToken accessToken = new AccessToken(tokenValue);
        when(tokenClient.validateToken(accessToken)).thenReturn(new TokenValidationResponse(false));

        Optional<AccessToken> token = Optional.ofNullable(new AccessToken(tokenValue));
        try {
            tokenServiceAccessValidator.validate(token);
            fail();
        }
        catch(InvalidAccessTokenException e){
            assertThat(e.getMessage()).isEqualTo("Token is invalid.");
            verify(tokenClient, times(1)).validateToken(accessToken);
        }

    }

    @Test(expected = InvalidAccessTokenException.class)
    public void shouldThrowInvalidAccessTokenException_whenTokenDoesntExist() throws Exception {
        String tokenValue = "abc";
        AccessToken accessToken = new AccessToken(tokenValue);
        when(tokenClient.validateToken(accessToken)).thenThrow(mock(InvalidAccessTokenException.class));

        Optional<AccessToken> token = Optional.ofNullable(new AccessToken(tokenValue));
        tokenServiceAccessValidator.validate(token);

        verify(tokenClient, times(1)).validateToken(accessToken);
    }

    @Test
    public void shouldThrowInvalidAccessTokenException_whenTokenIsAbsent() throws Exception {
        Optional<AccessToken> absentToken = Optional.empty();
        try {
            tokenServiceAccessValidator.validate(absentToken);
            fail();
        }
        catch(InvalidAccessTokenException e){
            assertThat(e.getMessage()).isEqualTo("Token must be provided.");
            verify(tokenClient, times(0)).validateToken(any(AccessToken.class));
        }
    }

    @Test
    public void shouldNotThrowException_whenTokenServicePassesValidation() throws Exception {
        String tokenValue = "abc";
        AccessToken accessToken = new AccessToken(tokenValue);
        when(tokenClient.validateToken(accessToken)).thenReturn(new TokenValidationResponse(true));

        tokenServiceAccessValidator.validate(Optional.ofNullable(accessToken));
    }
}
