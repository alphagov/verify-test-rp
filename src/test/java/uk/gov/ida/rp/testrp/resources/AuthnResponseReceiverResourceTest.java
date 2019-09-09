package uk.gov.ida.rp.testrp.resources;

import com.google.common.collect.ImmutableList;
import com.squarespace.jersey2.guice.JerseyGuiceUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.ida.common.SessionId;
import uk.gov.ida.rp.testrp.TestRpConfiguration;
import uk.gov.ida.rp.testrp.controllogic.AuthnResponseReceiverHandler;
import uk.gov.ida.rp.testrp.domain.ResponseFromHub;
import uk.gov.ida.saml.core.domain.TransactionIdaStatus;

import javax.ws.rs.core.Response;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AuthnResponseReceiverResourceTest {

    @BeforeClass
    public static void doALittleHackToMakeGuicierHappyForSomeReason() {
        JerseyGuiceUtils.reset();
    }

    @Mock
    private AuthnResponseReceiverHandler authnResponseReceiverHandler;
    @Mock
    private TestRpConfiguration testRpConfiguration;

    @Test
    public void assertCorrectRedirectUrlWhenReceivingNoAuthnContextResponseFromHub() {
        String samlResponse = "no-authn-context-saml-response";
        ResponseFromHub responseFromHub = new ResponseFromHub(
                TransactionIdaStatus.NoAuthenticationContext,
                ImmutableList.of(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty());
        when(authnResponseReceiverHandler.handleResponse(eq(samlResponse), any())).thenReturn(responseFromHub);

        AuthnResponseReceiverResource resource = new AuthnResponseReceiverResource(authnResponseReceiverHandler, testRpConfiguration);
        Response response = resource.doLogin(samlResponse, SessionId.createNewSessionId(), null);
        assertThat(response.getLocation().toString()).isEqualTo("/test-rp?errorCode=NoAuthenticationContext");
    }
}
