package uk.gov.ida.rp.testrp.views;

import uk.gov.ida.rp.testrp.repositories.Session;

import java.util.List;
import java.util.Optional;

public class TestRpUserAccountCreatedView extends TestRpView {

    private final Optional<String> errorMessage = Optional.empty();

    private final List<String> attributes;
    private final String loa;

    public TestRpUserAccountCreatedView(String javascriptBase, String stylesheetsBase, String imagesBase, Session session, List<String> attributes, String loa, String crossGovGaTrackerId) {
        super(javascriptBase, stylesheetsBase, imagesBase, session, "userAccountCreated.jade", crossGovGaTrackerId);
        this.attributes = attributes;
        this.loa = loa;
    }

    public List<String> getAttributes() {
        return attributes;
    }

    public String getLoa(){ return loa; }

    // this is required for the layout
    public Optional<String> getErrorMessage() {
        return errorMessage;
    }
}
