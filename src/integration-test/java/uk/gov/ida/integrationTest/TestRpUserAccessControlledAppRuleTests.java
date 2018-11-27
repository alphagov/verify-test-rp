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
    private static final String AUTHORIZED_TOKEN_VALUE = "eyJhbGciOiJSUzUxMiJ9.eyJlcG9jaCI6MSwidmFsaWRfdW50aWwiOiIyMTE4LTExLTI3VDExOjMwOjAwLjAwMFoiLCJpc3N1ZWRfdG8iOiJ3aWxscC1ibCJ9.rFS6Gx3kb8OTniEHXtWBttoqu-dY_GhwWsWyQcA9wQAGC0EpuRy_EaGYljvhFbXsKKJ1mQsps4pQg6E5QP1g9GFxX_FRWyxKW0GkBe_eT5aCtm6Z9Xzi4VyfyeJEVqUk__fPNwACBpJRsYqL53i3T9S1pegWG16rx6eCykQ_jFLDJnPo6n5QMSp6e0dI4gxbYpntNCFbDh5nD4TpHFG405fs43e4DfJXlPhJ_sThHiZXfKWW4AQbQ7HSAfLC8COs-p8UnaohRCkiDShlzoHII6NUAXbAq6_EEsigPEm3i3dLzpWf9FYstLuu99iUoWRPzK3JadJ4_6snMwATvTnD9Q";

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
