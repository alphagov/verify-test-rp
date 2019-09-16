package uk.gov.ida.rp.testrp.views;

import uk.gov.ida.rp.testrp.repositories.Session;

import java.util.Optional;

@SuppressWarnings("unused")
public class TestRpLandingPageView extends TestRpView {
    
    private final Optional<String> errorHeader;
    private final Optional<String> errorMessage;
    private final boolean shouldShowStartWithEidasButton;

    public TestRpLandingPageView(
            final String javascriptBase,
            final String stylesheetsBase,
            final String imagesBase,
            final Session session,
            final Optional<String> errorHeader,
            final Optional<String> errorMessage,
            boolean shouldShowStartWithEidasButton,
            String crossGovGaTrackerId) {

        super(javascriptBase, stylesheetsBase, imagesBase, session, "landingPage.jade", crossGovGaTrackerId);

        this.errorHeader = errorHeader;
        this.errorMessage = errorMessage;
        this.shouldShowStartWithEidasButton = shouldShowStartWithEidasButton;
    }

    public Optional<String> getErrorHeader() {
        return errorHeader;
    }

    public Optional<String> getErrorMessage() {
        return errorMessage;
    }

    public boolean getShouldShowStartWithEidasButton() {
        return shouldShowStartWithEidasButton;
    }
}

