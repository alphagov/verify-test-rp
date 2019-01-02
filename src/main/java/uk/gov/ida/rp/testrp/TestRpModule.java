package uk.gov.ida.rp.testrp;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import io.dropwizard.views.ViewRenderer;
import io.dropwizard.views.freemarker.FreemarkerViewRenderer;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.xmlsec.algorithm.DigestAlgorithm;
import org.opensaml.xmlsec.algorithm.SignatureAlgorithm;
import org.opensaml.xmlsec.algorithm.descriptors.DigestSHA256;
import org.opensaml.xmlsec.algorithm.descriptors.SignatureRSASHA256;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.ida.common.shared.security.PublicKeyFileInputStreamFactory;
import uk.gov.ida.common.shared.security.PublicKeyInputStreamFactory;
import uk.gov.ida.jerseyclient.DefaultClientProvider;
import uk.gov.ida.restclient.RestfulClientConfiguration;
import uk.gov.ida.rp.testrp.authentication.SimpleAuthenticator;
import uk.gov.ida.rp.testrp.controllogic.AuthnRequestSenderHandler;
import uk.gov.ida.rp.testrp.controllogic.AuthnResponseReceiverHandler;
import uk.gov.ida.rp.testrp.controllogic.MatchingServiceRequestHandler;
import uk.gov.ida.rp.testrp.domain.PageErrorMessageDetailsFactory;
import uk.gov.ida.rp.testrp.metadata.MetadataResolverProvider;
import uk.gov.ida.rp.testrp.metadata.SpMetadataPublicKeyStore;
import uk.gov.ida.rp.testrp.providers.KeyStoreProvider;
import uk.gov.ida.rp.testrp.repositories.SessionRepository;
import uk.gov.ida.rp.testrp.saml.locators.TransactionHardCodedEntityToEncryptForLocator;
import uk.gov.ida.rp.testrp.saml.transformers.IdaAuthnRequestFromTransactionToAuthnRequestTransformer;
import uk.gov.ida.rp.testrp.saml.transformers.InboundResponseFromHubUnmarshaller;
import uk.gov.ida.rp.testrp.saml.transformers.SamlResponseToIdaResponseTransformer;
import uk.gov.ida.rp.testrp.saml.validators.NoOpStringSizeValidator;
import uk.gov.ida.rp.testrp.tokenservice.TokenService;
import uk.gov.ida.rp.testrp.views.NonCachingFreemarkerViewRenderer;
import uk.gov.ida.rp.testrp.views.SamlAuthnRequestRedirectViewFactory;
import uk.gov.ida.saml.core.OpenSamlXmlObjectFactory;
import uk.gov.ida.saml.core.api.CoreTransformersFactory;
import uk.gov.ida.saml.core.transformers.AuthnContextFactory;
import uk.gov.ida.saml.hub.api.HubTransformersFactory;
import uk.gov.ida.saml.hub.domain.AuthnRequestFromTransaction;
import uk.gov.ida.saml.hub.transformers.inbound.PassthroughAssertionUnmarshaller;
import uk.gov.ida.saml.hub.transformers.inbound.TransactionIdaStatusUnmarshaller;
import uk.gov.ida.saml.hub.transformers.outbound.RequestAbstractTypeToStringTransformer;
import uk.gov.ida.saml.hub.validators.response.common.ResponseSizeValidator;
import uk.gov.ida.saml.idp.stub.domain.InboundResponseFromHub;
import uk.gov.ida.saml.metadata.IdpMetadataPublicKeyStore;
import uk.gov.ida.saml.metadata.MetadataHealthCheck;
import uk.gov.ida.saml.metadata.MetadataRefreshTask;
import uk.gov.ida.saml.metadata.TrustStoreConfiguration;
import uk.gov.ida.saml.security.AssertionDecrypter;
import uk.gov.ida.saml.security.DecrypterFactory;
import uk.gov.ida.saml.security.EntityToEncryptForLocator;
import uk.gov.ida.saml.security.IdaKeyStore;
import uk.gov.ida.saml.security.IdaKeyStoreCredentialRetriever;
import uk.gov.ida.saml.security.SamlAssertionsSignatureValidator;
import uk.gov.ida.saml.security.SamlMessageSignatureValidator;
import uk.gov.ida.saml.security.SignatureFactory;
import uk.gov.ida.saml.security.SignatureValidator;
import uk.gov.ida.saml.security.validators.encryptedelementtype.EncryptionAlgorithmValidator;
import uk.gov.ida.saml.security.validators.signature.SamlResponseSignatureValidator;
import uk.gov.ida.saml.serializers.XmlObjectToBase64EncodedStringTransformer;

import javax.inject.Named;
import javax.ws.rs.client.Client;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.Collections;
import java.util.function.Function;

public class TestRpModule extends AbstractModule {
    private static final Logger LOG = LoggerFactory.getLogger(TestRpModule.class);
    private static final String PRIVATE_BETA_ACCESS_RESTRICTION = "Private Beta (token) access restriction";
    private static final SignatureAlgorithm signatureAlgorithm = new SignatureRSASHA256();
    private static final DigestAlgorithm digestAlgorithm = new DigestSHA256();

    private final HubTransformersFactory hubTransformersFactory = new HubTransformersFactory();
    private final CoreTransformersFactory coreTransformersFactory = new CoreTransformersFactory();

    @Override
    public void configure() {
        bind(java.security.KeyStore.class).toProvider(KeyStoreProvider.class);
        bind(PublicKeyInputStreamFactory.class).to(PublicKeyFileInputStreamFactory.class);
        bind(SimpleAuthenticator.class);

        bind(Client.class).toProvider(DefaultClientProvider.class).in(Singleton.class);
        bind(RestfulClientConfiguration.class).to(TestRpConfiguration.class);

        bind(EntityToEncryptForLocator.class).to(TransactionHardCodedEntityToEncryptForLocator.class);

        bind(SessionRepository.class).in(Singleton.class);

        bind(TokenService.class);
        bind(MetadataResolver.class).toProvider(MetadataResolverProvider.class).in(Singleton.class);

        bind(AuthnRequestSenderHandler.class);
        bind(SamlAuthnRequestRedirectViewFactory.class);
        bind(PageErrorMessageDetailsFactory.class);
        bind(MatchingServiceRequestHandler.class);
        bind(AuthnResponseReceiverHandler.class);

        //must be eager singletons to be auto injected
        bind(MetadataRefreshTask.class).asEagerSingleton();
    }

    @Provides
    @Singleton
    public IdaKeyStoreCredentialRetriever getIdaKeyStoreCredentialRetriever(IdaKeyStore keyStore){
        return new IdaKeyStoreCredentialRetriever(keyStore);
    }

    @Provides
    @Singleton
    public SignatureFactory getSignatureFactory(IdaKeyStoreCredentialRetriever keyStoreCredentialRetriever, SignatureAlgorithm signatureAlgorithm, DigestAlgorithm digestAlgorithm){
        return new SignatureFactory(keyStoreCredentialRetriever, signatureAlgorithm, digestAlgorithm);
    }

    @Provides
    @Singleton
    public MetadataHealthCheck getMetadataHealthCheck(MetadataResolver metadataProvider, @Named("expectedEntityId") String expectedEntityId) {
        return new MetadataHealthCheck(metadataProvider, expectedEntityId);
    }

    @Provides
    @Singleton
    @Named("HubEntityId")
    public String getHubEntityId(TestRpConfiguration configuration) {
        return configuration.getHubEntityId();
    }

    @Provides
    private SignatureAlgorithm provideSignatureAlgorithm() {
        return signatureAlgorithm;
    }

    @Provides
    private DigestAlgorithm provideDigestAlgorithm() {
        return digestAlgorithm;
    }

    @Provides
    @Singleton
    private IdaKeyStore getKeyStore(TestRpConfiguration configuration) {
        KeyPair encryptionKeyPair = new KeyPair(configuration.getPublicEncryptionCert().getPublicKey(), configuration.getPrivateEncryptionKeyConfiguration().getPrivateKey());

        PublicKey publicKey = configuration.getPublicSigningCert().getPublicKey();
        KeyPair signingKeyPair = new KeyPair(publicKey, configuration.getPrivateSigningKeyConfiguration().getPrivateKey());
        return new IdaKeyStore(signingKeyPair,
                Collections.singletonList(encryptionKeyPair)
        );
    }

    @Provides
    @Singleton
    private TrustStoreConfiguration getClientTrustStoreConfiguration(TestRpConfiguration configuration) {
        return configuration.getClientTrustStoreConfiguration();
    }

    @SuppressWarnings("UnusedPrivateMethod")
    @Provides
    @Singleton
    private Function<AuthnRequestFromTransaction, String> getAuthnRequestFromTransactionToStringTransformer(Injector injector){
        RequestAbstractTypeToStringTransformer<AuthnRequest> requestAbstractTypeToStringTransformer = hubTransformersFactory.getRequestAbstractTypeToStringTransformer(false, injector.getInstance(IdaKeyStore.class), signatureAlgorithm, digestAlgorithm);
        IdaAuthnRequestFromTransactionToAuthnRequestTransformer authenRequestFromTransactionTransformer = new IdaAuthnRequestFromTransactionToAuthnRequestTransformer(new OpenSamlXmlObjectFactory());

        return requestAbstractTypeToStringTransformer.compose(authenRequestFromTransactionTransformer);
    }

    @SuppressWarnings("UnusedPrivateMethod")
    @Provides
    @Singleton
    private Function<String, InboundResponseFromHub> getStringToInboundResponseFromHubTransformer(
            TestRpConfiguration configuration,
            MetadataResolver metadataProvider,
            IdaKeyStore keyStore){

        Function<Response, InboundResponseFromHub> samlResponseToIdaResponseTransformer = getSamlResponseToIdaResponseTransformer(configuration, metadataProvider, keyStore);

        if(configuration.isHubExpectedToSignAuthnResponse()) {
            return samlResponseToIdaResponseTransformer.compose(hubTransformersFactory.getStringToResponseTransformer());
        }

        return samlResponseToIdaResponseTransformer.compose(hubTransformersFactory.getStringToResponseTransformer(new ResponseSizeValidator(new NoOpStringSizeValidator())));
    }

    @Provides
    @Singleton
    @Named("sessionCacheTimeoutInMinutes")
    public Integer getSessionCacheTimeoutInMinutes() {
        return 180;
    }

    @Provides
    @Singleton
    @Named("expectedEntityId")
    public String getExpectedEntityId(TestRpConfiguration configuration) {
        return configuration.getMsaEntityId();
    }

    @Provides
    private ViewRenderer getViewRenderer(TestRpConfiguration configuration) {
        if (configuration.getDontCacheFreemarkerTemplates()) {
            return new NonCachingFreemarkerViewRenderer();
        } else {
            return new FreemarkerViewRenderer();
        }
    }

    private SamlResponseToIdaResponseTransformer getSamlResponseToIdaResponseTransformer(
            TestRpConfiguration configuration,
            MetadataResolver metadataResolver,
            IdaKeyStore keyStore) {
        InboundResponseFromHubUnmarshaller inboundResponseFromHubUnmarshaller = new InboundResponseFromHubUnmarshaller(
                new TransactionIdaStatusUnmarshaller(),
                new PassthroughAssertionUnmarshaller(
                        new XmlObjectToBase64EncodedStringTransformer<>(),
                        new AuthnContextFactory())

        );
        IdaKeyStoreCredentialRetriever idaKeyStoreCredentialRetriever = new IdaKeyStoreCredentialRetriever(keyStore);
        return new SamlResponseToIdaResponseTransformer(
                inboundResponseFromHubUnmarshaller,
                new SamlResponseSignatureValidator(getHubMessageSignatureValidator(metadataResolver)),
                new AssertionDecrypter(
                        new EncryptionAlgorithmValidator(),
                        new DecrypterFactory().createDecrypter(idaKeyStoreCredentialRetriever.getDecryptingCredentials())
                ),
                new SamlAssertionsSignatureValidator(getMsaMessageSignatureValidator(metadataResolver)),
                configuration.isHubExpectedToSignAuthnResponse()
        );
    }

    private SamlMessageSignatureValidator getHubMessageSignatureValidator(MetadataResolver metadataResolver) {
        final SignatureValidator hubSignatureValidator = coreTransformersFactory.getSignatureValidator(new SpMetadataPublicKeyStore(metadataResolver));
        return new SamlMessageSignatureValidator(hubSignatureValidator);
    }

    private SamlMessageSignatureValidator getMsaMessageSignatureValidator(MetadataResolver metadataResolver) {
        final SignatureValidator msaSignatureValidator = coreTransformersFactory.getSignatureValidator(new IdpMetadataPublicKeyStore(metadataResolver));
        return new SamlMessageSignatureValidator(msaSignatureValidator);
    }
}
