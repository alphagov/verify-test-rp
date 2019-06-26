package uk.gov.ida.rp.testrp.tokenservice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.RSASSASigner;
import io.dropwizard.jackson.Jackson;
import uk.gov.ida.rp.testrp.TestRpConfiguration;
import uk.gov.ida.rp.testrp.domain.AccessToken;
import uk.gov.ida.rp.testrp.exceptions.TokenGenerationException;

import javax.inject.Inject;

public class BadTokenService {

    private final TestRpConfiguration configuration;

    private final ObjectMapper objectMapper = Jackson.newObjectMapper();

    @Inject
    public BadTokenService(TestRpConfiguration configuration) {
        this.configuration = configuration;
    }

    public AccessToken generate(String issueTo) {
        try {
            BadTokenDto tokenDto = new BadTokenDto(configuration.getTokenEpoch(), issueTo);
            JWSObject jwsObject = new JWSObject(new JWSHeader(JWSAlgorithm.RS256),
                    new Payload(objectMapper.writeValueAsBytes(tokenDto)));
            jwsObject.sign(new RSASSASigner(configuration.getPrivateSigningKeyConfiguration().getPrivateKey()));
            return new AccessToken(jwsObject.serialize());
        } catch (JsonProcessingException | JOSEException e) {
            throw new TokenGenerationException(e.getMessage());
        }
    }
}
