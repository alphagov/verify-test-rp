package uk.gov.ida.rp.testrp;

import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.hubspot.dropwizard.guicier.DropwizardModule;
import com.hubspot.dropwizard.guicier.GuiceBundle;
import com.squarespace.jersey2.guice.JerseyGuiceUtils;
import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.views.ViewBundle;
import io.dropwizard.views.freemarker.FreemarkerViewRenderer;
import uk.gov.ida.bundles.LoggingBundle;
import uk.gov.ida.bundles.MonitoringBundle;
import uk.gov.ida.bundles.ServiceStatusBundle;
import uk.gov.ida.filters.AcceptLanguageFilter;
import uk.gov.ida.rp.testrp.authentication.TestRpAuthProvider;
import uk.gov.ida.rp.testrp.exceptions.InvalidAccessTokenExceptionMapper;
import uk.gov.ida.rp.testrp.exceptions.TokenServiceUnavailableExceptionMapper;
import uk.gov.ida.rp.testrp.filters.NoCacheResponseFilter;
import uk.gov.ida.rp.testrp.filters.SampleRpCacheControlFilter;
import uk.gov.ida.rp.testrp.filters.SecurityHeadersFilter;
import uk.gov.ida.rp.testrp.resources.AuthnResponseReceiverResource;
import uk.gov.ida.rp.testrp.resources.CookiesInfoResource;
import uk.gov.ida.rp.testrp.resources.HeadlessRpResource;
import uk.gov.ida.rp.testrp.resources.LocalMatchingServiceResource;
import uk.gov.ida.rp.testrp.resources.TestRpResource;
import uk.gov.ida.rp.testrp.resources.TokenResource;
import uk.gov.ida.saml.core.IdaSamlBootstrap;
import uk.gov.ida.saml.metadata.MetadataHealthCheck;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import java.util.EnumSet;
import java.util.Map;

public class TestRpApplication extends Application<TestRpConfiguration> {

    private GuiceBundle<TestRpConfiguration> guiceBundle;

    public static void main(String[] args) {
        // running this method here stops the odd exceptions/double-initialisation that happens without it
        // - it's the same fix that was required in the tests...
        JerseyGuiceUtils.reset();

        try {
            if (args == null || args.length == 0) {
                String configFile = System.getenv("CONFIG_FILE");

                if (configFile == null) {
                    throw new RuntimeException("CONFIG_FILE environment variable should be set with path to configuration file");
                }

                new TestRpApplication().run("server", configFile);
            } else {
                new TestRpApplication().run(args);
            }
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public void initialize(Bootstrap<TestRpConfiguration> bootstrap) {

        // Enable variable substitution with environment variables
        bootstrap.setConfigurationSourceProvider(
                new SubstitutingSourceProvider(bootstrap.getConfigurationSourceProvider(),
                        new EnvironmentVariableSubstitutor(false)
                        )
                );

        guiceBundle = GuiceBundle
                .defaultBuilder(getConfigurationClass())
                .modules(
                        new DropwizardModule(),
                        new TestRpModule()
                )
                .build();
        bootstrap.addBundle(guiceBundle);

        bootstrap.addBundle(new ServiceStatusBundle());
        bootstrap.addBundle(new MonitoringBundle());
        bootstrap.addBundle(new ViewBundle<TestRpConfiguration>() {
            @Override
            public Map<String, Map<String, String>> getViewConfiguration(TestRpConfiguration config) {
                // beware: this is to force enable escaping of unsanitised user input
                return ImmutableMap.of(new FreemarkerViewRenderer().getConfigurationKey(),
                        ImmutableMap.of(
                                "output_format", "HTMLOutputFormat"
                        ));
            }
        });
        bootstrap.addBundle(new LoggingBundle());
        bootstrap.addBundle(new AssetsBundle("/assets/", "/assets/"));
        bootstrap.setConfigurationSourceProvider(new SubstitutingSourceProvider(bootstrap.getConfigurationSourceProvider(), new EnvironmentVariableSubstitutor(false)));

    }

    @Override
    public String getName() {
        return "Identity Assurance Test Service";
    }

    @Override
    public void run(TestRpConfiguration configuration, Environment environment) {
        IdaSamlBootstrap.bootstrap();
        FilterRegistration.Dynamic cacheControlFilter =
                environment.servlets().addFilter("CacheControlFilter", new SampleRpCacheControlFilter(configuration));
        cacheControlFilter.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, "/test-rp", "/headless-rp");
        environment.servlets().addFilter("Remove Accept-Language headers", AcceptLanguageFilter.class).addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, "/*");

        environment.jersey().register(TestRpAuthProvider.createBinder());

        environment.getObjectMapper().setDateFormat(new StdDateFormat());

        //resources
        environment.jersey().register(HeadlessRpResource.class);
        environment.jersey().register(TestRpResource.class);
        environment.jersey().register(AuthnResponseReceiverResource.class);
        environment.jersey().register(LocalMatchingServiceResource.class);
        environment.jersey().register(CookiesInfoResource.class);
        environment.jersey().register(TokenResource.class);

        //exception mappers
        environment.jersey().register(InvalidAccessTokenExceptionMapper.class);
        environment.jersey().register(TokenServiceUnavailableExceptionMapper.class);

        //filters
        environment.jersey().register(NoCacheResponseFilter.class);
        environment.jersey().register(SecurityHeadersFilter.class);

        //health checks
        environment.healthChecks().register("metadata", guiceBundle.getInjector().getInstance(MetadataHealthCheck.class));
    }

}
