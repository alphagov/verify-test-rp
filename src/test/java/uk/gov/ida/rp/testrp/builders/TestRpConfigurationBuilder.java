package uk.gov.ida.rp.testrp.builders;

import io.dropwizard.client.JerseyClientConfiguration;
import uk.gov.ida.common.ServiceInfoConfiguration;
import uk.gov.ida.rp.testrp.EntityId;
import uk.gov.ida.rp.testrp.TestRpConfiguration;
import uk.gov.ida.rp.testrp.saml.configuration.SamlConfiguration;
import uk.gov.ida.saml.metadata.TrustStoreConfiguration;

import static org.mockito.Mockito.mock;
import static uk.gov.ida.common.ServiceInfoConfigurationBuilder.aServiceInfo;
import static uk.gov.ida.jerseyclient.JerseyClientConfigurationBuilder.aJerseyClientConfiguration;

public class TestRpConfigurationBuilder {

    public class TestSamlConfiguration extends SamlConfiguration {
        public TestSamlConfiguration(String issuer) {
            this.entityId = issuer;
        }
    }

    private SamlConfiguration samlConfiguration = new TestSamlConfiguration(EntityId.TEST_RP);
    private Boolean privateBetaUserAccessRestrictionEnabled = true;

    public static TestRpConfigurationBuilder aTestRpConfiguration() {
        return new TestRpConfigurationBuilder();
    }

    public TestRpConfiguration build() {

        return new TestTestRpConfiguration(
                aJerseyClientConfiguration().build(),
                aServiceInfo().withName("MatchingService").build(),
                mock(TrustStoreConfiguration.class),
                samlConfiguration,
                "cookie-name",
                false,
                false,
                "/javascript",
                "/stylesheets",
                "/images",
                privateBetaUserAccessRestrictionEnabled
        );
    }

    public TestRpConfigurationBuilder withAPrivateBetaUserAccessRestrictionEnabledValue(Boolean testRpUserAccessRestrictionEnabled) {
        this.privateBetaUserAccessRestrictionEnabled = testRpUserAccessRestrictionEnabled;
        return this;
    }

    private static class TestTestRpConfiguration extends TestRpConfiguration {

        private TestTestRpConfiguration(
                JerseyClientConfiguration httpClient,
                ServiceInfoConfiguration serviceInfo,
                TrustStoreConfiguration clientTrustStoreConfiguration,
                SamlConfiguration saml,
                String cookieName,
                Boolean dontCacheFreemarkerTemplates,
                Boolean forceAuthentication,
                String javascriptPath,
                String stylesheetsPath,
                String imagesPath,
                Boolean privateBetaUserAccessRestrictionEnabled
        ) {

            this.httpClient = httpClient;
            this.serviceInfo = serviceInfo;
            this.clientTrustStoreConfiguration = clientTrustStoreConfiguration;

            this.saml = saml;
            this.cookieName = cookieName;
            this.dontCacheFreemarkerTemplates = dontCacheFreemarkerTemplates;
            this.forceAuthentication = forceAuthentication;
            this.javascriptPath = javascriptPath;
            this.stylesheetsPath = stylesheetsPath;
            this.imagesPath = imagesPath;
            this.privateBetaUserAccessRestrictionEnabled = privateBetaUserAccessRestrictionEnabled;
        }
    }
}
