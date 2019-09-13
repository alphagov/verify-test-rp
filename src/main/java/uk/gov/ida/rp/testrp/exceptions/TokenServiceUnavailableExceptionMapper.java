package uk.gov.ida.rp.testrp.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.ida.rp.testrp.TestRpConfiguration;
import uk.gov.ida.rp.testrp.views.TestRpTokenServiceUnavailablePageView;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import java.text.MessageFormat;
import java.util.UUID;

public class TokenServiceUnavailableExceptionMapper implements ExceptionMapper<TokenServiceUnavailableException> {

    private static final Logger LOG = LoggerFactory.getLogger(TokenServiceUnavailableExceptionMapper.class);
    private final TestRpConfiguration configuration;

    @Inject
    public TokenServiceUnavailableExceptionMapper(TestRpConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public Response toResponse(TokenServiceUnavailableException exception) {
        return getTokenServiceUnavailableResponse(exception, configuration);
    }

    public static Response getTokenServiceUnavailableResponse(TokenServiceUnavailableException exception, TestRpConfiguration configuration) {
        UUID eventId = UUID.randomUUID();
        LOG.error(MessageFormat.format("{0} - Exception while processing request.", eventId), exception);

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new TestRpTokenServiceUnavailablePageView(configuration.getJavascriptPath(), configuration.getStylesheetsPath(), configuration.getImagesPath(), configuration.getCrossGovGaTrackerId())).build();
    }
}
