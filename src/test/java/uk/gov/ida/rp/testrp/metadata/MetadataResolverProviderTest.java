package uk.gov.ida.rp.testrp.metadata;

import com.google.common.base.Throwables;
import com.squarespace.jersey2.guice.JerseyGuiceUtils;
import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import org.apache.commons.io.FileUtils;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import uk.gov.ida.rp.testrp.MsaStubRule;
import uk.gov.ida.rp.testrp.TestRpConfiguration;
import uk.gov.ida.rp.testrp.exceptions.InsecureMetadataException;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import java.io.File;
import java.net.URI;
import java.security.KeyStore;

import static junit.framework.TestCase.assertNotNull;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MetadataResolverProviderTest {

    @BeforeClass
    public static void doALittleHackToMakeGuicierHappyForSomeReason() {
        JerseyGuiceUtils.reset();
    }

    public static MsaStubRule msaStubRule = new MsaStubRule("metadata.xml");

    @AfterClass
    public static void tearDown() {
        msaStubRule.stop();
    }

    @Mock
    private TestRpConfiguration configuration;

    private static Client client = ClientBuilder.newBuilder().hostnameVerifier(new NoopHostnameVerifier()).trustStore(createKeyStore()).build();

    @Before
    public void setUp() {
        System.setProperty("https.protocols", "TLSv1.2");
    }

    @Test
    public void shouldPerformHttpsRequestWhenInsecureMetadataFlagIsNotPresent() throws Exception {
        when(configuration.getMsaMetadataUri()).thenReturn(URI.create("https://localhost:"+msaStubRule.getSecurePort()+"/metadata"));

        MetadataResolverProvider provider = new MetadataResolverProvider(client, configuration);

        assertCanQueryMetadata(provider);
    }

    @Test
    public void shouldPerformHttpsRequestWhenInsecureMetadataFlagIsTrue() throws Exception {
        when(configuration.getMsaMetadataUri()).thenReturn(URI.create("https://localhost:"+msaStubRule.getSecurePort()+"/metadata"));
        when(configuration.getAllowInsecureMetadataLocation()).thenReturn(true);

        MetadataResolverProvider provider = new MetadataResolverProvider(client, configuration);

        assertCanQueryMetadata(provider);
    }

    @Test(expected = InsecureMetadataException.class)
    public void shouldThrowExceptionWhenPerformingHttpRequestWhenInsecureMetadataFlagIsFalse() {
        when(configuration.getMsaMetadataUri()).thenReturn(URI.create("http://localhost:"+msaStubRule.getPort()+"/metadata"));
        when(configuration.getAllowInsecureMetadataLocation()).thenReturn(false);

        MetadataResolverProvider provider = new MetadataResolverProvider(client, configuration);

        provider.get();
    }

    @Test
    public void shouldPerformHttpRequestWhenInsecureMetadataFlagIsTrue() throws Exception {
        when(configuration.getMsaMetadataUri()).thenReturn(URI.create("http://localhost:"+msaStubRule.getPort()+"/metadata"));
        when(configuration.getAllowInsecureMetadataLocation()).thenReturn(true);

        MetadataResolverProvider provider = new MetadataResolverProvider(client, configuration);

        assertCanQueryMetadata(provider);
    }

    private void assertCanQueryMetadata(MetadataResolverProvider provider) throws net.shibboleth.utilities.java.support.resolver.ResolverException {
        CriteriaSet criteria = new CriteriaSet(new EntityIdCriterion("http://www.test-rp-ms.gov.uk/SAML2/MD"));
        EntityDescriptor entityDescriptor = provider.get().resolveSingle(criteria);
        assertNotNull(entityDescriptor);
    }


    private static KeyStore createKeyStore() {
        KeyStore ks = null;
        try {
            ks = KeyStore.getInstance("JKS");
            ks.load(FileUtils.openInputStream(new File("test_keys/dev_service_ssl.ks")), "marshmallow".toCharArray());
        } catch (Exception e) {
            Throwables.propagate(e);
        }
        return ks;
    }
}
