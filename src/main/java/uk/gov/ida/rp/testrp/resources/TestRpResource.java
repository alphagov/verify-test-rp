package uk.gov.ida.rp.testrp.resources;

import io.dropwizard.auth.Auth;
import uk.gov.ida.common.SessionId;
import uk.gov.ida.rp.testrp.TestRpConfiguration;
import uk.gov.ida.rp.testrp.Urls;
import uk.gov.ida.rp.testrp.contract.LevelOfAssuranceDto;
import uk.gov.ida.rp.testrp.domain.AccessToken;
import uk.gov.ida.rp.testrp.domain.PageErrorMessageDetails;
import uk.gov.ida.rp.testrp.domain.PageErrorMessageDetailsFactory;
import uk.gov.ida.rp.testrp.repositories.Session;
import uk.gov.ida.rp.testrp.repositories.SessionRepository;
import uk.gov.ida.rp.testrp.tokenservice.TokenService;
import uk.gov.ida.rp.testrp.views.TestRpLandingPageView;
import uk.gov.ida.rp.testrp.views.TestRpSuccessPageView;
import uk.gov.ida.saml.core.domain.TransactionIdaStatus;

import javax.inject.Inject;
import javax.ws.rs.CookieParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.util.Optional;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static uk.gov.ida.rp.testrp.Urls.Cookies.TEST_RP_SESSION_COOKIE_NAME;
import static uk.gov.ida.rp.testrp.tokenservice.AccessTokenCookieName.ACCESS_TOKEN_COOKIE_NAME;

@Path(Urls.TestRpUrls.TEST_RP_ROOT)
@Produces(MediaType.TEXT_HTML)
public class TestRpResource {

    private final SessionRepository sessionRepository;
    private final TestRpConfiguration configuration;
    private final PageErrorMessageDetailsFactory pageErrorMessageDetailsFactory;
    private final TokenService tokenService;

    @Inject
    public TestRpResource(
            SessionRepository sessionRepository,
            TestRpConfiguration configuration,
            PageErrorMessageDetailsFactory pageErrorMessageDetailsFactory,
            TokenService tokenService) {
        this.sessionRepository = sessionRepository;
        this.configuration = configuration;
        this.pageErrorMessageDetailsFactory = pageErrorMessageDetailsFactory;
        this.tokenService = tokenService;
    }

    @GET
    public Response getMain(
            @QueryParam(Urls.Params.ERROR_CODE_PARAM) Optional<TransactionIdaStatus> errorCode,
            @QueryParam(Urls.Params.ACCESS_TOKEN_PARAM) Optional<AccessToken> queryParamToken,
            @CookieParam(ACCESS_TOKEN_COOKIE_NAME) AccessToken cookieToken
    ) {

        Optional<AccessToken> token = empty();
        if (queryParamToken.isPresent()) {
            token = queryParamToken;
        } else if (cookieToken != null) {
            token = of(cookieToken);
        }

        tokenService.validate(token);

        final PageErrorMessageDetails errorMessageDetails = pageErrorMessageDetailsFactory.getErrorMessage(errorCode);
        final TestRpLandingPageView testRpLandingPageView = new TestRpLandingPageView(configuration.getJavascriptPath(), configuration.getStylesheetsPath(), configuration.getImagesPath(), null, errorMessageDetails.getHeader(), errorMessageDetails.getMessage(), configuration.getShouldShowStartWithEidasButton(), configuration.getCrossGovGaTrackerId());

        final Response.ResponseBuilder builder = Response
                .status(Response.Status.OK)
                .entity(testRpLandingPageView);

        token.ifPresent(accessToken -> builder.cookie(new NewCookie(ACCESS_TOKEN_COOKIE_NAME, accessToken.toString())));

        return builder.build();
    }

    @GET
    @Path(Urls.SUCCESSFUL_REGISTER_PATH)
    public TestRpSuccessPageView getSuccessfulRegister(
            @Auth Session session,
            @QueryParam(Urls.Params.ERROR_CODE_PARAM) Optional<TransactionIdaStatus> errorCode,
            @QueryParam(Urls.Params.RP_NAME_PARAM) Optional<String> rpName,
            @QueryParam(Urls.Params.LOA_PARAM) Optional<LevelOfAssuranceDto> loa) {
        final PageErrorMessageDetails errorMessage = pageErrorMessageDetailsFactory.getErrorMessage(errorCode);
        return new TestRpSuccessPageView(configuration.getJavascriptPath(), configuration.getStylesheetsPath(), configuration.getImagesPath(), session, errorMessage.getHeader(), errorMessage.getMessage(), rpName, loa, configuration.getCrossGovGaTrackerId());
    }

    @POST
    @Path(Urls.LOGOUT_PATH)
    @SuppressWarnings("unused")// The user (with @Auth annotation) is required to force authentication
    public Response postLogUserOut(@Auth Session session,
                                   @CookieParam(TEST_RP_SESSION_COOKIE_NAME) SessionId sessionId) {
        sessionRepository.delete(sessionId);
        return Response.seeOther(UriBuilder.fromResource(TestRpResource.class).build()).build();
    }
}
