package uk.gov.ida.rp.testrp.resources;

import com.squarespace.jersey2.guice.JerseyGuiceUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.ida.rp.testrp.controllogic.AuthnRequestSenderHandler;
import uk.gov.ida.saml.core.domain.TransactionIdaStatus;
import uk.gov.ida.saml.idp.stub.domain.InboundResponseFromHub;

import javax.ws.rs.core.Response;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HeadlessRpResourceTest {

    @BeforeClass
    public static void doALittleHackToMakeGuicierHappyForSomeReason() {
        JerseyGuiceUtils.reset();
    }

    private HeadlessRpResource resource;

    @Mock
    private AuthnRequestSenderHandler authnRequestSenderHandler;
    @Mock
    private Function<String, InboundResponseFromHub> samlResponseDeserialiser;
    @Mock
    private InboundResponseFromHub inboundResponseFromHub;

    @Before
    public void setUp() {
        this.resource = new HeadlessRpResource(samlResponseDeserialiser, authnRequestSenderHandler);
    }

    @Test
    public void postingNoAuthContext_shouldReturnSadFace(){
        String xml = "Some Saml";
        when(samlResponseDeserialiser.apply(xml)).thenReturn(inboundResponseFromHub);
        when(inboundResponseFromHub.getStatus()).thenReturn(TransactionIdaStatus.NoAuthenticationContext);

        Response response = this.resource.doLogin(xml);

        assertThat(response.getEntity().toString()).contains("Headless Failed Log In");
    }
}
