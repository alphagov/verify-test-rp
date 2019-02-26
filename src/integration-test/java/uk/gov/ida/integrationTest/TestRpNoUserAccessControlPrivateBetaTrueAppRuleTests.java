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

    private static final String AUTHORIZED_TOKEN_VALUE = "eyJhbGciOiJSUzI1NiJ9.eyJlcG9jaCI6MSwidmFsaWRVbnRpbCI6NDEwMjQ0NDgwMDAwMCwiaXNzdWVkVG8iOiJodHRwOi8vc3R1Yl9pZHAuYWNtZS5vcmcvc3R1Yi1pZHAtb25lL1NTTy9QT1NUIn0.FKbhvdTRN8ZWtwEPEXtbF4YPf-_bDMK8U5nVJaeRK1ZCOQwCKR3UPP4uWXZ0CDYQSI5HTJrmz1dypSk7UhAU5dPPMexKjbif6QSUxpJbl24N0odiOlDzMTPXJD_bXwyJBxc4rccIhjDRYJ8qVRDAiE0jFksRXzRhjfb3HdYR5E3-hNlS1ZSnKXWBa4jFxgrWwJ6vPlDrlPjLs8Y1ByK8vOAYyKjOUKyjF0PqljiZlRgt5QHPZOX3yaO2EEPoLkwNejUi_WWM6O0OxVdpHjXcIL_F07K5UYDtyJDAMM2Z54xNHbk4qfATnDUWK2uXmHlb27X9KhLoToMSyqIFUDoMCQ";

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
