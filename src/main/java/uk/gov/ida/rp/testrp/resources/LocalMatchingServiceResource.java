package uk.gov.ida.rp.testrp.resources;

import uk.gov.ida.rp.testrp.Urls;
import uk.gov.ida.rp.testrp.contract.MatchingServiceRequestDto;
import uk.gov.ida.rp.testrp.contract.MatchingServiceResponseDto;
import uk.gov.ida.rp.testrp.contract.UnknownUserCreationRequestDto;
import uk.gov.ida.rp.testrp.contract.UnknownUserCreationResponseDto;
import uk.gov.ida.rp.testrp.controllogic.MatchingServiceRequestHandler;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path(Urls.TestRpUrls.TEST_RP_ROOT)
public class LocalMatchingServiceResource {

    private final MatchingServiceRequestHandler matchingServiceRequestHandler;

    @Inject
    public LocalMatchingServiceResource(MatchingServiceRequestHandler matchingServiceRequestHandler) {
        this.matchingServiceRequestHandler = matchingServiceRequestHandler;
    }

    @POST
    @Path(Urls.TestRpUrls.LOCAL_MATCHING_SERVICE_PATH)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public MatchingServiceResponseDto post(@NotNull MatchingServiceRequestDto matchingServiceRequestDto) {
        return matchingServiceRequestHandler.handleMatchingRequest(matchingServiceRequestDto);
    }

    @POST
    @Path(Urls.TestRpUrls.UNKNOWN_USER_CREATION_SERVICE_PATH)
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public UnknownUserCreationResponseDto post(@NotNull UnknownUserCreationRequestDto request) {
        return matchingServiceRequestHandler.handleUserAccountCreationRequest(request);
    }
}
