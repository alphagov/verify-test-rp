package uk.gov.ida.rp.testrp.views;

import io.dropwizard.views.View;
import uk.gov.ida.rp.testrp.repositories.Session;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;

@SuppressWarnings("unused")
public class TestRpView extends View {

    private final Session session;
    private final URI javascriptBase;
    private final URI stylesheetsBase;
    private final URI imagesBase;
    private final boolean isUserAuthenticated;

    private final String crossGovGaTrackerId;

    public TestRpView(
            final String javascriptBase,
            final String stylesheetsBase,
            final String imagesBase,
            final Session session,
            final String templateName,
            final String crossGovGaTrackerId) {

        super(templateName);

        this.session = session;

        this.javascriptBase = UriBuilder.fromPath(javascriptBase).build();
        this.stylesheetsBase = UriBuilder.fromPath(stylesheetsBase).build();
        this.imagesBase = UriBuilder.fromPath(imagesBase).build();
        this.isUserAuthenticated = isUserAuthenticated();
        this.crossGovGaTrackerId = crossGovGaTrackerId;
    }

    public URI getJavascriptBase() {
        return javascriptBase;
    }

    public URI getStylesheetsBase() {
        return stylesheetsBase;
    }

    public URI getImagesBase() {
        return imagesBase;
    }

    public boolean getIsUserAuthenticated() {
        return isUserAuthenticated;
    }

    public boolean isUserAuthenticated(){
        return session != null;
    }

    public String getCrossGovGaTrackerId() {
        return crossGovGaTrackerId;
    }

}
