package uk.gov.ida.rp.testrp.views;

import java.util.Optional;

@SuppressWarnings("unused")
public class TestRpPrivateBetaPageView extends TestRpView {

    private final Optional<String> errorHeader;
    private final Optional<String> errorMessage;

    public TestRpPrivateBetaPageView(String javascriptBase, String stylesheetsBase, String imagesBase, String crossGovGaTrackerId) {
        super(javascriptBase, stylesheetsBase, imagesBase, null, "privateBetaPage.jade", crossGovGaTrackerId);
        errorHeader = Optional.empty();
        errorMessage = Optional.of("The Identity Assurance Test Service is for testing purposes only, and is only open to invited participants.");
    }

    public Optional<String> getErrorHeader() {
        return errorHeader;
    }

    public Optional<String> getErrorMessage() {
        return errorMessage;
    }
}
