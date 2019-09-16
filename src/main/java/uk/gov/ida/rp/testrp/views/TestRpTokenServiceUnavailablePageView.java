package uk.gov.ida.rp.testrp.views;

import java.util.Optional;

public class TestRpTokenServiceUnavailablePageView extends TestRpView {

    private final Optional<String> errorHeader;
    private final Optional<String> errorMessage;

    public TestRpTokenServiceUnavailablePageView(String javascriptBase, String stylesheetsBase, String imagesBase, String crossGovGaTrackerId) {
        super(javascriptBase, stylesheetsBase, imagesBase, null, "tokenServiceUnavailablePage.jade", crossGovGaTrackerId);
        errorHeader = Optional.empty();
        errorMessage = Optional.of("We are unable to process your request right now. Please try again soon");
    }

    @SuppressWarnings("unused") // required by jade master layout template
    public Optional<String> getErrorHeader() {
        return errorHeader;
    }

    public Optional<String> getErrorMessage() {
        return errorMessage;
    }
}
