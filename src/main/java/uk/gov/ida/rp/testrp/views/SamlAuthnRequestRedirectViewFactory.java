package uk.gov.ida.rp.testrp.views;

import com.google.common.net.HttpHeaders;
import uk.gov.ida.common.SessionId;
import uk.gov.ida.rp.testrp.TestRpConfiguration;
import uk.gov.ida.rp.testrp.domain.JourneyHint;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.Optional;

/*
 * Generate the HTML for a SAML Redirect
 */
public class SamlAuthnRequestRedirectViewFactory {

    private final TestRpConfiguration testRpConfiguration;

    @Inject
    public SamlAuthnRequestRedirectViewFactory(TestRpConfiguration testRpConfiguration){
        this.testRpConfiguration = testRpConfiguration;
    }

    public Response sendSamlMessage(String messageToSend, SessionId relayState, URI targetUriFromSamlEndpoint, Optional<JourneyHint> journeyHint) {
        return getResponse(messageToSend, relayState, targetUriFromSamlEndpoint, journeyHint);
    }

    private Response getResponse(
            String samlMessage,
            SessionId relayState,
            URI targetUriFromSamlEndpoint,
            Optional<JourneyHint> journeyHint) {

        SamlRedirectView samlFormPostingView = new SamlRedirectView(targetUriFromSamlEndpoint, samlMessage, relayState, journeyHint, testRpConfiguration.getCrossGovGaTrackerId());
        return Response.ok(samlFormPostingView)
            .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store")
            .header(HttpHeaders.PRAGMA, "no-cache")
            .build();
    }
}
