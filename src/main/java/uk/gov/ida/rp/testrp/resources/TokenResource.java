package uk.gov.ida.rp.testrp.resources;

import uk.gov.ida.rp.testrp.Urls;
import uk.gov.ida.rp.testrp.tokenservice.GenerateTokenRequestDto;
import uk.gov.ida.rp.testrp.tokenservice.TokenService;

import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path(Urls.PrivateUrls.GENERATE_TOKEN_RESOURCE)
@Produces(MediaType.APPLICATION_JSON)
public class TokenResource {

    private final TokenService tokenService;

    @Inject
    public TokenResource(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @POST
    public Response generateToken(GenerateTokenRequestDto generateTokenDto) {
        return Response.ok(tokenService.generate(generateTokenDto)).build();
    }
}
