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

    private static final String AUTHORIZED_TOKEN_VALUE = "eyJhbGciOiJSUzUxMiJ9.eyJlcG9jaCI6MSwidmFsaWRfdW50aWwiOiIyMTE4LTExLTI3VDExOjMwOjAwLjAwMFoiLCJpc3N1ZWRfdG8iOiJ3aWxscC1ibCJ9.rFS6Gx3kb8OTniEHXtWBttoqu-dY_GhwWsWyQcA9wQAGC0EpuRy_EaGYljvhFbXsKKJ1mQsps4pQg6E5QP1g9GFxX_FRWyxKW0GkBe_eT5aCtm6Z9Xzi4VyfyeJEVqUk__fPNwACBpJRsYqL53i3T9S1pegWG16rx6eCykQ_jFLDJnPo6n5QMSp6e0dI4gxbYpntNCFbDh5nD4TpHFG405fs43e4DfJXlPhJ_sThHiZXfKWW4AQbQ7HSAfLC8COs-p8UnaohRCkiDShlzoHII6NUAXbAq6_EEsigPEm3i3dLzpWf9FYstLuu99iUoWRPzK3JadJ4_6snMwATvTnD9Q";

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
