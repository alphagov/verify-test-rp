package uk.gov.ida.rp.testrp.resources;

import uk.gov.ida.rp.testrp.Urls;
import uk.gov.ida.rp.testrp.TestRpConfiguration;
import uk.gov.ida.rp.testrp.views.CookiesInfoView;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path(Urls.TestRpUrls.COOKIES_INFO_RESOURCE)
@Produces(MediaType.TEXT_HTML)
public class CookiesInfoResource {

    private final TestRpConfiguration testRpConfiguration;

    @Inject
    public CookiesInfoResource(TestRpConfiguration testRpConfiguration) {
        this.testRpConfiguration = testRpConfiguration;
    }

    @GET
    public CookiesInfoView getCookieInfo() {
        return new CookiesInfoView(testRpConfiguration.getJavascriptPath(), testRpConfiguration.getStylesheetsPath(), testRpConfiguration.getImagesPath(), testRpConfiguration.getCrossGovGaTrackerId());
    }

}
