package uk.gov.ida.integrationTest;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

public class TestRpNoUserAccessControlPrivateBetaTrueAppRuleTests extends IntegrationTestHelper {

    private static Client client;

    private static final String AUTHORIZED_TOKEN_VALUE = "eyJhbGciOiJSUzI1NiJ9.eyJlcG9jaCI6MSwidmFsaWRVbnRpbCI6NDEwMjQ0NDgwMDAwMCwiaXNzdWVkVG8iOiJodHRwOi8vc3R1Yl9pZHAuYWNtZS5vcmcvc3R1Yi1pZHAtb25lL1NTTy9QT1NUIn0.Ad7qOHJ-ZJe3feUB9-Rq9nNsGal5wSqgmHWVNZpnASKdMfUZYHCBoz_TAZHZL9WeG9HkC_8INRw3o10Q8wBZyC662X0-Fif149p6eP5vld54nfIKJ1fRFxo7yTDaIw3sKyaXDIQ6IGGXyEsgg7kMwNyID1eTp9X5jV0EOIoEilyo-Lr5qhBAR7ZBEj5L7tM15b34UalUjaqz3_5i8ErSvumEAiU1DQWA66-uMP5bGn3dpwiSaP_8egU2IXXOkU78fGgUmckqkWFbRbgv4KA0nioYX7BS1GFh0QSzLiXjLXO33YYEapx8tNh3UetgZ1jmUBAqQFUireg3t8Onx_DKfQ";

    @ClassRule
    public static TestRpAppRule testRp = TestRpAppRule.newTestRpAppRule(
            ConfigOverride.config("clientTrustStoreConfiguration.path", ResourceHelpers.resourceFilePath("ida_truststore.ts")),
            ConfigOverride.config("privateBetaUserAccessRestrictionEnabled", "true")
    );

    @BeforeClass
    public static void beforeClass() {
        JerseyClientConfiguration jerseyClientConfiguration = JerseyClientConfigurationBuilder.aJerseyClientConfiguration().withTimeout(Duration.seconds(20)).build();
        client = new JerseyClientBuilder(testRp.getEnvironment()).using(jerseyClientConfiguration).build(TestRpNoUserAccessControlPrivateBetaTrueAppRuleTests.class.getSimpleName());
    }

    @Test
    public void getLandingPage_withValidToken_shouldReturnTestRpLandingPageView() {
        URI uri = testRp.uri(Urls.TestRpUrls.TEST_RP_ROOT);
        Response response = client.target(uri)
                .queryParam(Urls.Params.ACCESS_TOKEN_PARAM, AUTHORIZED_TOKEN_VALUE)
                .request(MediaType.TEXT_HTML)
                .get(Response.class);

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        assertThat(response.readEntity(String.class)).contains("Test GOV.UK Verify user journeys");
    }

    @Test
    public void getLandingPage_withInvalidToken_shouldReturnTestRpLandingPage() {
        String invalidToken = "some-invalid-value";

        Response response = client.target(testRp.uri(Urls.TestRpUrls.TEST_RP_ROOT))
                .queryParam(Urls.Params.ACCESS_TOKEN_PARAM, invalidToken)
                .request(MediaType.TEXT_HTML)
                .get(Response.class);

        assertThat(response.getStatus()).isEqualTo(Response.Status.FORBIDDEN.getStatusCode());
        assertThat(response.readEntity(String.class)).contains("The Identity Assurance Test Service is for testing purposes only, and is only open to invited participants.");
    }

    @Test
    public void getLandingPage_withMissingToken_shouldReturnTestRpLandingPage() throws JsonProcessingException {

        Response response = client.target(testRp.uri(Urls.TestRpUrls.TEST_RP_ROOT))
                .request()
                .get(Response.class);

        assertThat(response.getStatus()).isEqualTo(Response.Status.FORBIDDEN.getStatusCode());
        assertThat(response.readEntity(String.class)).contains("The Identity Assurance Test Service is for testing purposes only, and is only open to invited participants.");
    }
}
