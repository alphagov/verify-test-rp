package uk.gov.ida.rp.testrp.exceptions;

public class TokenHasInvalidSignatureException extends InvalidAccessTokenException {
    public TokenHasInvalidSignatureException(String message) {
        super(message);
    }
}
