package uk.gov.ida.rp.testrp.views;

import java.util.Optional;

@SuppressWarnings("unused")
public class CookiesInfoView extends TestRpView {

    private final Optional<String> errorHeader;
    private final Optional<String> errorMessage;

    public CookiesInfoView(String javascriptBase, String stylesheetsBase, String imagesBase) {
        super(javascriptBase, stylesheetsBase, imagesBase, null, "cookiesInfo.jade");
        errorHeader = Optional.empty();
        errorMessage = Optional.empty();
    }

    public Optional<String> getErrorHeader() {
        return errorHeader;
    }

    public Optional<String> getErrorMessage() {
        return errorMessage;
    }
}
