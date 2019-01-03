package uk.gov.ida.rp.testrp.authentication;

import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml.saml2.metadata.SingleSignOnService;
import uk.gov.ida.common.SessionId;
import uk.gov.ida.rp.testrp.TestRpConfiguration;
import uk.gov.ida.rp.testrp.controllogic.AuthnRequestSenderHandler;
import uk.gov.ida.rp.testrp.repositories.SessionRepository;
import uk.gov.ida.rp.testrp.saml.configuration.SamlConfiguration;
import uk.gov.ida.rp.testrp.views.SamlAuthnRequestRedirectViewFactory;
import uk.gov.ida.saml.hub.domain.AuthnRequestFromTransaction;

import java.net.URI;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AuthnRequestSenderHandlerTest {

    private AuthnRequestSenderHandler manager;

    @Mock
    private TestRpConfiguration configuration;
    @Mock
    private SamlAuthnRequestRedirectViewFactory samlRequestFactory;
    @Mock
    private Function<AuthnRequestFromTransaction, String> authnRequestToStringTransformer;
    @Mock
    private SessionRepository sessionRepository;
    @Mock
    private MetadataResolver metadataResolver;
    @Mock
    private EntityDescriptor entityDescriptor;
    @Mock
    private IDPSSODescriptor idpSsoDescriptor;
    @Mock
    private SingleSignOnService ssoService;
    @Mock
    private SamlConfiguration samlConfiguration;

    private String hubSsoEndpoint = "http://samlendpoint.com";
    private URI requestUri;
    private String rpName = "some-rp-name";
    private boolean forceAuthentication = false;

    @Before
    public void setUp() throws Exception {
        this.requestUri = new URI("http://request.uri");
        this.manager = new AuthnRequestSenderHandler(
                configuration,
                samlRequestFactory,
                sessionRepository,
                metadataResolver,
                authnRequestToStringTransformer);
        when(configuration.getMsaEntityId()).thenReturn("msa-entity-id");
        when(metadataResolver.resolveSingle(any(CriteriaSet.class))).thenReturn(entityDescriptor);
        when(entityDescriptor.getIDPSSODescriptor(anyString())).thenReturn(idpSsoDescriptor);
        when(idpSsoDescriptor.getSingleSignOnServices()).thenReturn(Collections.singletonList(ssoService));
        when(ssoService.getLocation()).thenReturn(hubSsoEndpoint);
    }

    @Test
    public void sendAuthnRequest_shouldUseDefaultTargetUriWhenTargetUriNotSpecified() throws Exception {
        final SessionId relayState = SessionId.createNewSessionId();

        when(sessionRepository.newSession(any(), eq(requestUri), any(), any(), any(), eq(forceAuthentication), eq(false), eq(false))).thenReturn(relayState);
        when(configuration.getSamlConfiguration()).thenReturn(samlConfiguration);
        when(samlConfiguration.getEntityId()).thenReturn("entity id");

        this.manager.sendAuthnRequest(requestUri, Optional.empty(), "test-rp",
            Optional.empty(), Optional.empty(),
            false, false, false);

        verify(samlRequestFactory).sendSamlMessage(
            eq(null), eq(relayState), eq(new URI(hubSsoEndpoint)), eq(Optional.empty()));
    }

    @Test
    public void sendAuthnRequest_shouldFormatIssuerIdWithRpName() throws Exception {
        final String issuerId = "entity-id-with-format-param-%s";
        final String expectedIssuerId = "entity-id-with-format-param-some-rp-name";
        ArgumentCaptor<AuthnRequestFromTransaction> captor = ArgumentCaptor.forClass(AuthnRequestFromTransaction.class);
        when(configuration.getSamlConfiguration()).thenReturn(new TestSamlConfiguration(issuerId));

        this.manager.sendAuthnRequest(requestUri, Optional.empty(), rpName, Optional.empty(), Optional.empty(), forceAuthentication, false, false);

        verify(authnRequestToStringTransformer).apply(captor.capture());
        assertThat(captor.getValue().getForceAuthentication().get()).isEqualTo(forceAuthentication);
        assertThat(captor.getValue().getIssuer()).isEqualTo(expectedIssuerId);
    }

    @Test
    public void sendAuthnRequest_shouldForceAuthenticationIfRequired() throws Exception {
        final String issuerId = "entity-id-with-format-param-%s";
        ArgumentCaptor<AuthnRequestFromTransaction> captor = ArgumentCaptor.forClass(AuthnRequestFromTransaction.class);
        when(configuration.getSamlConfiguration()).thenReturn(new TestSamlConfiguration(issuerId));
        boolean forceAuthn = true;
        this.manager.sendAuthnRequest(requestUri, Optional.empty(), rpName, Optional.empty(), Optional.empty(), forceAuthn, false, false);

        verify(authnRequestToStringTransformer).apply(captor.capture());
        assertThat(captor.getValue().getForceAuthentication().get()).isEqualTo(forceAuthn);
    }

    public class TestSamlConfiguration extends SamlConfiguration {
        public TestSamlConfiguration(String issuer) {
            this.entityId = issuer;
        }
    }

}
