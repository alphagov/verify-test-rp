package uk.gov.ida.rp.testrp.resources;

import com.squarespace.jersey2.guice.JerseyGuiceUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.ida.common.SessionId;
import uk.gov.ida.rp.testrp.builders.AccessTokenBuilder;
import uk.gov.ida.rp.testrp.contract.LevelOfAssuranceDto;
import uk.gov.ida.rp.testrp.domain.AccessToken;
import uk.gov.ida.rp.testrp.domain.PageErrorMessageDetails;
import uk.gov.ida.rp.testrp.domain.PageErrorMessageDetailsFactory;
import uk.gov.ida.rp.testrp.repositories.Session;
import uk.gov.ida.rp.testrp.repositories.SessionRepository;
import uk.gov.ida.rp.testrp.tokenservice.AccessTokenValidator;
import uk.gov.ida.rp.testrp.views.TestRpSuccessPageView;
import uk.gov.ida.saml.core.domain.TransactionIdaStatus;

import java.net.URI;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.ida.rp.testrp.builders.TestRpConfigurationBuilder.aTestRpConfiguration;

@RunWith(MockitoJUnitRunner.class)
public class TestRpResourceTest {

    @BeforeClass
    public static void doALittleHackToMakeGuicierHappyForSomeReason() {
        JerseyGuiceUtils.reset();
    }

    @Mock
    private SessionRepository sessionRepository;
    @Mock
    private PageErrorMessageDetailsFactory pageErrorMessageDetailsFactory;
    @Mock
    private AccessTokenValidator tokenValidator;

    private TestRpResource resource;

    @Before
    public void setErrorMessageGeneratorDummyErrorMessage() {
        PageErrorMessageDetails nullErrorMessage = new PageErrorMessageDetails(Optional.empty(), Optional.empty());
        when(pageErrorMessageDetailsFactory.getErrorMessage(ArgumentMatchers.<Optional<TransactionIdaStatus>>any())).thenReturn(nullErrorMessage);

        resource = new TestRpResource(sessionRepository, aTestRpConfiguration().build(), pageErrorMessageDetailsFactory, tokenValidator);
    }

    @Test
    public void getMain_withQueryParamTokenAndCookie_shouldValidateQueryParamToken() {
        AccessToken queryParamToken = AccessTokenBuilder.anAccessToken().withValue("query-param-token").build();
        AccessToken cookieToken = AccessTokenBuilder.anAccessToken().withValue("cookie-token").build();
        resource.getMain(Optional.empty(), Optional.of(queryParamToken), cookieToken);

        verify(tokenValidator).validate(Optional.of(queryParamToken));
    }

    @Test
    public void getMain_withNoQueryParamButWithCookie_shouldValidateCookieToken() {
        AccessToken cookieToken = AccessTokenBuilder.anAccessToken().withValue("cookie-token").build();
        resource.getMain(Optional.empty(), Optional.empty(), cookieToken);

        verify(tokenValidator).validate(Optional.of(cookieToken));
    }

    @Test
    public void getMain_withNoQueryParamOrCookie_shouldValidateAbsentToken() {
        resource.getMain(Optional.empty(), Optional.empty(), null);

        verify(tokenValidator).validate(Optional.<AccessToken>empty());
    }

    @Test
    public void getSuccessfulRegister_shouldReturnSucessfulPageView(){
        Session session = new Session(
                SessionId.createNewSessionId(),
                "requestId",
                URI.create("pathUserWasTryingToAccess"),
                "issuerId",
                Optional.ofNullable(1),
                Optional.empty(),
                false,
                false,
                false);

        TestRpSuccessPageView successPageView = resource.getSuccessfulRegister(session, Optional.empty(), Optional.of("unit-test-rp"), Optional.of(LevelOfAssuranceDto.LEVEL_1));

        assertThat(successPageView.getLoa()).isEqualTo(LevelOfAssuranceDto.LEVEL_1.name());
        assertThat(successPageView.getRpName()).isEqualTo("unit-test-rp");

    }

}
