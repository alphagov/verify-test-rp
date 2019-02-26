package uk.gov.ida.rp.testrp.authentication;

import io.dropwizard.auth.Auth;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.TypeLiteral;
import org.glassfish.hk2.utilities.Binder;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.internal.inject.AbstractContainerRequestValueFactory;
import org.glassfish.jersey.server.internal.inject.AbstractValueFactoryProvider;
import org.glassfish.jersey.server.internal.inject.MultivaluedParameterExtractorProvider;
import org.glassfish.jersey.server.internal.inject.ParamInjectionResolver;
import org.glassfish.jersey.server.model.Parameter;
import org.glassfish.jersey.server.spi.internal.ValueFactoryProvider;
import uk.gov.ida.rp.testrp.TestRpConfiguration;
import uk.gov.ida.rp.testrp.controllogic.AuthnRequestSenderHandler;
import uk.gov.ida.rp.testrp.repositories.Session;
import uk.gov.ida.rp.testrp.tokenservice.TokenService;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class TestRpAuthProvider extends AbstractValueFactoryProvider {

    private final SimpleAuthenticator authenticator;
    private final TestRpConfiguration testRpConfiguration;
    private final AuthnRequestSenderHandler authnRequestManager;
    private final TokenService tokenService;

    @Inject
    private TestRpAuthProvider(
            MultivaluedParameterExtractorProvider mpep,
            ServiceLocator locator,
            SimpleAuthenticator authenticator,
            TestRpConfiguration testRpConfiguration,
            AuthnRequestSenderHandler authnRequestManager,
            TokenService tokenService) {
        super(mpep, locator, Parameter.Source.UNKNOWN);
        this.authenticator = authenticator;
        this.testRpConfiguration = testRpConfiguration;
        this.authnRequestManager = authnRequestManager;
        this.tokenService = tokenService;
    }

    @Singleton
    private static final class SessionInjectionResolver extends ParamInjectionResolver<Auth> {
        public SessionInjectionResolver() {
            super(TestRpAuthProvider.class);
        }

    }

    public static Binder createBinder() {
        return new AbstractBinder() {
            @Override
            protected void configure() {
                bind(TestRpAuthProvider.class).to(ValueFactoryProvider.class).in(Singleton.class);
                bind(SessionInjectionResolver.class).to(new TypeLiteral<SessionInjectionResolver>() {
                }).in(Singleton.class);
            }
        };
    }

    @Override
    protected AbstractContainerRequestValueFactory<?> createValueFactory(Parameter parameter) {
        if (Session.class.equals(parameter.getRawType())) {
            return new SessionFactory(authenticator, testRpConfiguration, authnRequestManager, tokenService);
        }
        return null;
    }

}
