package uk.gov.ida.rp.testrp.views;

import uk.gov.ida.rp.testrp.contract.LevelOfAssuranceDto;
import uk.gov.ida.rp.testrp.repositories.Session;

import java.util.Optional;

@SuppressWarnings("unused")
public class TestRpSuccessPageView extends TestRpView {

    private final Optional<String> errorHeader;
    private final Optional<String> errorMessage;
    private final Optional<String> rpName;
    private final Optional<LevelOfAssuranceDto> loa;

    public TestRpSuccessPageView(
            final String javascriptBase,
            final String stylesheetsBase,
            final String imagesBase,
            final Session session,
            final Optional<String> errorHeader,
            final Optional<String> errorMessage,
            final Optional<String> rpName,
            final Optional<LevelOfAssuranceDto> loa,
            final String crossGovGaTrackerId) {

        super(javascriptBase, stylesheetsBase, imagesBase, session, "successPage.jade", crossGovGaTrackerId);
        this.errorHeader = errorHeader;
        this.errorMessage = errorMessage;
        this.rpName = rpName;
        this.loa = loa;
    }

    public Optional<String> getErrorHeader() {
        return errorHeader;
    }

    public Optional<String> getErrorMessage() {
        return errorMessage;
    }

    public String getRpName() {
        return rpName.orElse("");
    }

    public String getLoa() {
        return loa.isPresent()?loa.get().name():null;
    }
}
