package uk.gov.ida.rp.testrp.views;

import io.dropwizard.views.View;
import uk.gov.ida.common.SessionId;
import uk.gov.ida.rp.testrp.domain.JourneyHint;

import java.net.URI;
import java.util.Optional;

public class SamlRedirectView extends View {
    private URI targetUri;
    private String responseBody;
    private SessionId relayState;
    private final Optional<JourneyHint> journeyHint;
    private final String crossGovGaTrackerId;

    public SamlRedirectView(URI targetUri,
                            String base64EncodedResponseBody,
                            SessionId relayState,
                            Optional<JourneyHint> journeyHint,
                            String crossGovGaTrackerId) {
        super("samlRedirectView.ftl");
        this.targetUri = targetUri;
        this.responseBody = base64EncodedResponseBody;
        this.relayState = relayState;
        this.journeyHint = journeyHint;
        this.crossGovGaTrackerId = crossGovGaTrackerId;
    }

    public URI getTargetUri() {
        return targetUri;
    }

    public String getBody() {
        return responseBody;
    }

    public String getRelayState() {
        return relayState.getSessionId();
    }

    public boolean getShowJourneyHint(){
        return journeyHint.isPresent();
    }

    public String getJourneyHint() {
        return journeyHint.isPresent()?journeyHint.get().name():"";
    }

    public String getCrossGovGaTrackerId() {
        return crossGovGaTrackerId;
    }
}
