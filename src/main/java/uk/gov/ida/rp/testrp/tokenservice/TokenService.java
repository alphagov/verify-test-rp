package uk.gov.ida.rp.testrp.tokenservice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.SignedJWT;
import io.dropwizard.jackson.Jackson;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import uk.gov.ida.rp.testrp.TestRpConfiguration;
import uk.gov.ida.rp.testrp.domain.AccessToken;
import uk.gov.ida.rp.testrp.exceptions.CouldNotInstantiateVerifierException;
import uk.gov.ida.rp.testrp.exceptions.CouldNotParseTokenException;
import uk.gov.ida.rp.testrp.exceptions.CouldNotParseTokenPayloadException;
import uk.gov.ida.rp.testrp.exceptions.InvalidAccessTokenException;
import uk.gov.ida.rp.testrp.exceptions.PublicSigningKeyIsNotRSAException;
import uk.gov.ida.rp.testrp.exceptions.TokenGenerationException;
import uk.gov.ida.rp.testrp.exceptions.TokenHasInvalidSignatureException;

import javax.inject.Inject;
import java.io.IOException;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.util.Optional;

import static java.text.MessageFormat.format;

public class TokenService {

    private static final Logger LOG = Logger.getLogger(TokenService.class);

    private final TestRpConfiguration configuration;

    private final ObjectMapper objectMapper = Jackson.newObjectMapper();

    @Inject
    public TokenService(TestRpConfiguration configuration) {
        this.configuration = configuration;
    }

    public void validate(Optional<AccessToken> token) {
        if (configuration.isPrivateBetaUserAccessRestrictionEnabled()) {
            TokenValidationResponse tokenValidationResponse =
                    validateToken(token.orElseThrow(() -> new InvalidAccessTokenException("Token must be provided.")));

            if(!tokenValidationResponse.isValid()){
                throw new InvalidAccessTokenException("Token is invalid.");
            }
        }
    }

    public AccessToken generate(GenerateTokenRequestDto generateTokenDto) {
        if (generateTokenDto.getValidUntil().isBefore(DateTime.now())) {
            throw new TokenGenerationException(format("Cannot create token with validUntil in past: %s", generateTokenDto.getValidUntil()));
        }

        try {
            TokenDto tokenDto = new TokenDto(configuration.getTokenEpoch(), generateTokenDto.getValidUntil(), generateTokenDto.getIssueTo());
            JWSObject jwsObject = new JWSObject(new JWSHeader(JWSAlgorithm.RS256),
                    new Payload(objectMapper.writeValueAsBytes(tokenDto)));
            jwsObject.sign(new RSASSASigner(configuration.getPrivateSigningKeyConfiguration().getPrivateKey()));
            return new AccessToken(jwsObject.serialize());
        } catch (JsonProcessingException | JOSEException e) {
            throw new TokenGenerationException(e.getMessage());
        }
    }

    private TokenValidationResponse validateToken(AccessToken token) {
        if(!(configuration.getPublicSigningCert().getPublicKey() instanceof RSAPublicKey)) {
            throw new PublicSigningKeyIsNotRSAException();
        }

        RSAKey rsaPublicJWK = new RSAKey.Builder((RSAPublicKey)configuration.getPublicSigningCert().getPublicKey()).build();

        JWSVerifier verifier;
        try {
            verifier = new RSASSAVerifier(rsaPublicJWK);
        } catch (JOSEException e) {
            throw new CouldNotInstantiateVerifierException();
        }
        SignedJWT jwsObject;
        try {
            jwsObject = SignedJWT.parse(token.toString());
        } catch (ParseException e) {
            throw new CouldNotParseTokenException("could not parse token");
        }
        try {
            if(!verifier.verify(jwsObject.getHeader(), jwsObject.getSigningInput(), jwsObject.getSignature())) {
                throw new TokenHasInvalidSignatureException("invalid signature");
            }
        } catch (JOSEException e) {
            throw new TokenHasInvalidSignatureException("invalid signature");
        }

        TokenDto tokenDto;
        try {
            tokenDto = objectMapper.readValue(jwsObject.getPayload().toJSONObject().toJSONString(), TokenDto.class);
        } catch (IOException e) {
            throw new CouldNotParseTokenPayloadException("possibly missing required fields");
        }

        if(tokenDto.getEpoch()<configuration.getTokenEpoch()) {
            LOG.warn(format("Attempt to use token issued at expired epoch issued to {0}, valid until {1}, epoch {2}", tokenDto.getIssuedTo(), tokenDto.getValidUntil(), tokenDto.getEpoch()));
            return new TokenValidationResponse(false);
        }

        if(tokenDto.getValidUntil().isAfterNow()) {
            return new TokenValidationResponse(true);
        } else {
            LOG.warn(format("Attempt to use expired token issued to {0}, valid until {1}, epoch {2}", tokenDto.getIssuedTo(), tokenDto.getValidUntil(), tokenDto.getEpoch()));
            return new TokenValidationResponse(false);
        }

    }
}
