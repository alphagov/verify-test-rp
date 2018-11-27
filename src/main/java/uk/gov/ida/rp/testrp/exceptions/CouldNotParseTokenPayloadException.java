package uk.gov.ida.rp.testrp.exceptions;

public class CouldNotParseTokenPayloadException extends InvalidAccessTokenException {
    public CouldNotParseTokenPayloadException(String message) {
        super(message);
    }
}
