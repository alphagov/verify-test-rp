package uk.gov.ida.rp.testrp.resources;

import org.apache.http.client.utils.URIBuilder;
import uk.gov.ida.common.SessionId;
import uk.gov.ida.rp.testrp.TestRpConfiguration;
import uk.gov.ida.rp.testrp.Urls;
import uk.gov.ida.rp.testrp.controllogic.AuthnResponseReceiverHandler;
import uk.gov.ida.rp.testrp.domain.ResponseFromHub;
import uk.gov.ida.rp.testrp.views.TestRpUserAccountCreatedView;
import uk.gov.ida.saml.core.domain.TransactionIdaStatus;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.Optional;

import static java.text.MessageFormat.format;
import static javax.ws.rs.core.UriBuilder.fromResource;
import static uk.gov.ida.rp.testrp.Urls.Cookies.TEST_RP_SESSION_COOKIE_NAME;
import static uk.gov.ida.rp.testrp.Urls.TestRpUrls.TEST_RP_ROOT;

@Path(Urls.TestRpUrls.LOGIN_RESOURCE)
@Produces(MediaType.TEXT_HTML)
@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
public class AuthnResponseReceiverResource {

    private final AuthnResponseReceiverHandler authnResponseReceiverHandler;
    private final TestRpConfiguration testRpConfiguration;

    @Inject
    public AuthnResponseReceiverResource(
            AuthnResponseReceiverHandler authnResponseReceiverHandler,
            TestRpConfiguration testRpConfiguration) {

        this.authnResponseReceiverHandler = authnResponseReceiverHandler;
        this.testRpConfiguration = testRpConfiguration;
    }

    @POST
    public Response doLogin(
            @FormParam(Urls.Params.SAML_RESPONSE_PARAM) @NotNull String samlResponse,
            @FormParam(Urls.Params.RELAY_STATE_PARAM) SessionId relayState,
            @QueryParam(Urls.Params.GA_CLIENT_ID) String gaClientId) {

        ResponseFromHub responseFromHub = authnResponseReceiverHandler.handleResponse(samlResponse, Optional.ofNullable(relayState));

        switch (responseFromHub.getTransactionIdaStatus()){
            case Success:
                return handleSuccess(responseFromHub, gaClientId);
            case NoMatchingServiceMatchFromHub:
            case NoAuthenticationContext:
            case AuthenticationFailed:
            case RequesterError:
                return errorResponse(responseFromHub.getTransactionIdaStatus(), gaClientId);
            default:
                throw new IllegalStateException(format("Unexpected status in hub response. {0}", responseFromHub));
        }
    }

    private Response handleSuccess(ResponseFromHub responseFromHub, String gaClientId) {
        if(responseFromHub.getAttributes().isEmpty()) {
            UriBuilder location = UriBuilder.fromPath(TEST_RP_ROOT);
            if (responseFromHub.getRedirectUri().isPresent()) {
                location = UriBuilder.fromUri(responseFromHub.getRedirectUri().get());
            }
            if (responseFromHub.getAuthnContext().isPresent()) {
                location.queryParam(Urls.Params.LOA_PARAM, responseFromHub.getAuthnContext().get().name());
            }
            if (gaClientId != null && !gaClientId.isEmpty()) {
                location.queryParam(Urls.Params.GA_CLIENT_ID, gaClientId);
            }
            final Response.ResponseBuilder responseBuilder = Response.seeOther(location.build());
            if(responseFromHub.getSessionId().isPresent()) {
                responseBuilder.cookie(new NewCookie(TEST_RP_SESSION_COOKIE_NAME, responseFromHub.getSessionId().get().getSessionId()));
            }
            return responseBuilder.build();

        } else {
            // user account creation
            final TestRpUserAccountCreatedView testRpUserAccountCreatedView = new TestRpUserAccountCreatedView(testRpConfiguration.getJavascriptPath(), testRpConfiguration.getStylesheetsPath(), testRpConfiguration.getImagesPath(), responseFromHub.getSession().get(), responseFromHub.getAttributes(), responseFromHub.getAuthnContext().get().name(), testRpConfiguration.getCrossGovGaTrackerId());
            return Response.ok(testRpUserAccountCreatedView)
                    .cookie(new NewCookie(TEST_RP_SESSION_COOKIE_NAME, responseFromHub.getSessionId().get().getSessionId()))
                    .build();
        }
    }

    private Response errorResponse(TransactionIdaStatus transactionIdaStatus, String gaClientId) {
        UriBuilder builder = fromResource(TestRpResource.class)
                .queryParam(Urls.Params.ERROR_CODE_PARAM, transactionIdaStatus);
        if ( gaClientId != null && !gaClientId.isEmpty() ) builder.queryParam(Urls.Params.GA_CLIENT_ID, gaClientId);
        URI location = builder.build();
        return Response.seeOther(location).build();
    }

}
