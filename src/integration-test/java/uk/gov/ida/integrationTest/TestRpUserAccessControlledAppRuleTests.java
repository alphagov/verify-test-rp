package uk.gov.ida.integrationTest;

import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.util.Duration;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import uk.gov.ida.integrationTest.support.IntegrationTestHelper;
import uk.gov.ida.integrationTest.support.TestRpAppRule;
import uk.gov.ida.jerseyclient.JerseyClientConfigurationBuilder;
import uk.gov.ida.rp.testrp.Urls;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.ida.rp.testrp.tokenservice.AccessTokenCookieName.ACCESS_TOKEN_COOKIE_NAME;

public class TestRpUserAccessControlledAppRuleTests extends IntegrationTestHelper {

    private static final String SUCCESS_PATH = "/test-rp/success";
    private static final String landingPageContent = "Identity Assurance Test Service - GOV.UK";
    private static final String AUTHORIZED_TOKEN_VALUE = "eyJhbGciOiJSUzI1NiJ9.eyJlcG9jaCI6MSwidmFsaWRVbnRpbCI6NDEwMjQ0NDgwMDAwMCwiaXNzdWVkVG8iOiJodHRwOi8vc3R1Yl9pZHAuYWNtZS5vcmcvc3R1Yi1pZHAtb25lL1NTTy9QT1NUIn0.Ad7qOHJ-ZJe3feUB9-Rq9nNsGal5wSqgmHWVNZpnASKdMfUZYHCBoz_TAZHZL9WeG9HkC_8INRw3o10Q8wBZyC662X0-Fif149p6eP5vld54nfIKJ1fRFxo7yTDaIw3sKyaXDIQ6IGGXyEsgg7kMwNyID1eTp9X5jV0EOIoEilyo-Lr5qhBAR7ZBEj5L7tM15b34UalUjaqz3_5i8ErSvumEAiU1DQWA66-uMP5bGn3dpwiSaP_8egU2IXXOkU78fGgUmckqkWFbRbgv4KA0nioYX7BS1GFh0QSzLiXjLXO33YYEapx8tNh3UetgZ1jmUBAqQFUireg3t8Onx_DKfQ";

    private static Client client;

    @ClassRule
    public static TestRpAppRule testRp = TestRpAppRule.newTestRpAppRule(
            ConfigOverride.config("privateBetaUserAccessRestrictionEnabled", "true"),
            ConfigOverride.config("clientTrustStoreConfiguration.path", ResourceHelpers.resourceFilePath("ida_truststore.ts"))
    );

    @BeforeClass
    public static void beforeClass() {
        JerseyClientConfiguration jerseyClientConfiguration = JerseyClientConfigurationBuilder.aJerseyClientConfiguration().withTimeout(Duration.seconds(20)).build();
        client = new JerseyClientBuilder(testRp.getEnvironment()).using(jerseyClientConfiguration).build(TestRpUserAccessControlledAppRuleTests.class.getSimpleName());
    }

    @Test
    public void getSuccessPage_withIncorrectAccessTokenCookie_shouldReturnLandingPage() {
        String invalidTokenValue = "some-invalid-token";

        final URI uri = testRp.uri(SUCCESS_PATH);

        Response response = client
                .target(uri)
                .request()
                .cookie(new Cookie(ACCESS_TOKEN_COOKIE_NAME, invalidTokenValue))
                .get(Response.class);

        assertThat(response.getStatus()).isEqualTo(Response.Status.FORBIDDEN.getStatusCode());
        assertThat(response.readEntity(String.class)).contains(landingPageContent);
    }

    @Test
    public void getSuccessPage_withNoAccessTokenCookie_shouldReturnPrivateBetaPage() {
        final URI uri = testRp.uri(SUCCESS_PATH);

        Response response = client
                .target(uri)
                .request()
                .get(Response.class);

        assertThat(response.getStatus()).isEqualTo(Response.Status.FORBIDDEN.getStatusCode());
        assertThat(response.readEntity(String.class)).contains(landingPageContent);
    }
    
    @Test
    public void getLandingPage_visitWithQueryParamShouldSetTokenCookieSoHubCanRedirectToLandingPageWithoutQueryParam() {

        Response response = requestLandingPageWithToken();
        assertThat(response.getCookies().values()).contains(new NewCookie(ACCESS_TOKEN_COOKIE_NAME, AUTHORIZED_TOKEN_VALUE));
        response = requestedLandingPageWithCookieValue();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        assertThat(response.readEntity(String.class)).contains("Test GOV.UK Verify user journeys");
    }

    private Response requestedLandingPageWithCookieValue() {
        URI uri = testRp.uri(Urls.TestRpUrls.TEST_RP_ROOT);
        return client.target(uri)
                .request()
                .cookie(new NewCookie(ACCESS_TOKEN_COOKIE_NAME, AUTHORIZED_TOKEN_VALUE))
                .get(Response.class);
    }

    private Response requestLandingPageWithToken() {
        URI uri = testRp.uri(Urls.TestRpUrls.TEST_RP_ROOT);
        return client.target(uri)
                .queryParam(Urls.Params.ACCESS_TOKEN_PARAM, AUTHORIZED_TOKEN_VALUE)
                .request(MediaType.TEXT_HTML)
                .get(Response.class);
    }
}
