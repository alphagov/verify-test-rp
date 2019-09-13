package uk.gov.ida.rp.testrp.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.ida.rp.testrp.TestRpConfiguration;
import uk.gov.ida.rp.testrp.views.TestRpPrivateBetaPageView;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import java.text.MessageFormat;
import java.util.UUID;

public class InvalidAccessTokenExceptionMapper implements ExceptionMapper<InvalidAccessTokenException> {

    private static final Logger LOG = LoggerFactory.getLogger(InvalidAccessTokenExceptionMapper.class);
    private final TestRpConfiguration configuration;

    @Inject
    public InvalidAccessTokenExceptionMapper(TestRpConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public Response toResponse(InvalidAccessTokenException exception) {
        return getRestrictedPrivateBetaResponse(exception, configuration);
    }

    public static Response getRestrictedPrivateBetaResponse(InvalidAccessTokenException exception, TestRpConfiguration configuration) {
        UUID eventId = UUID.randomUUID();
        LOG.error(MessageFormat.format("{0} - Exception while processing request.", eventId), exception);

        return Response.status(Response.Status.FORBIDDEN).entity(new TestRpPrivateBetaPageView(configuration.getJavascriptPath(), configuration.getStylesheetsPath(), configuration.getImagesPath(), configuration.getCrossGovGaTrackerId())).build();
    }
}
