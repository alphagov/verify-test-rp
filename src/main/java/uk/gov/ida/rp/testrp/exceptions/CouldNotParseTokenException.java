package uk.gov.ida.rp.testrp.exceptions;

public class CouldNotParseTokenException extends InvalidAccessTokenException {
    public CouldNotParseTokenException(String message) {
        super(message);
    }
}
