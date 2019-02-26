package uk.gov.ida.rp.testrp.tokenservice;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.ida.common.shared.configuration.DeserializablePublicKeyConfiguration;
import uk.gov.ida.common.shared.configuration.PrivateKeyConfiguration;
import uk.gov.ida.common.shared.security.PrivateKeyFactory;
import uk.gov.ida.common.shared.security.X509CertificateFactory;
import uk.gov.ida.rp.testrp.TestRpConfiguration;
import uk.gov.ida.rp.testrp.domain.AccessToken;
import uk.gov.ida.rp.testrp.exceptions.CouldNotParseTokenException;
import uk.gov.ida.rp.testrp.exceptions.CouldNotParseTokenPayloadException;
import uk.gov.ida.rp.testrp.exceptions.InvalidAccessTokenException;
import uk.gov.ida.rp.testrp.exceptions.TokenGenerationException;
import uk.gov.ida.rp.testrp.exceptions.TokenHasInvalidSignatureException;
import uk.gov.ida.saml.core.test.TestCertificateStrings;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_PRIVATE_SIGNING_KEY;
import static uk.gov.ida.saml.core.test.TestEntityIds.STUB_IDP_ONE;

@RunWith(MockitoJUnitRunner.class)
public class TokenServiceTest {

    private TokenService tokenService;

    @Mock
    private TestRpConfiguration configuration;
    @Mock
    private DeserializablePublicKeyConfiguration deserializablePublicKeyConfiguration;
    @Mock
    private PrivateKeyConfiguration privateKeyConfiguration;

    private static PrivateKey privateKey = new PrivateKeyFactory().createPrivateKey(Base64.getDecoder().decode(TEST_RP_PRIVATE_SIGNING_KEY));
    private static PublicKey publicKey = new X509CertificateFactory().createCertificate(TestCertificateStrings.TEST_RP_PUBLIC_SIGNING_CERT).getPublicKey();

    @Before
    public void setUp() {
        when(deserializablePublicKeyConfiguration.getPublicKey()).thenReturn(publicKey);
        when(privateKeyConfiguration.getPrivateKey()).thenReturn(privateKey);
        when(configuration.getPrivateSigningKeyConfiguration()).thenReturn(privateKeyConfiguration);
        when(configuration.getPublicSigningCert()).thenReturn(deserializablePublicKeyConfiguration);
        when(configuration.getTokenEpoch()).thenReturn(1);
        when(configuration.isPrivateBetaUserAccessRestrictionEnabled()).thenReturn(true);
        tokenService = new TokenService(configuration);
    }

    @Test
    public void shouldValidateWhenSignedProperlyAndInDateAndEpoch() {
        AccessToken accessToken = new AccessToken("eyJhbGciOiJSUzI1NiJ9.eyJlcG9jaCI6MSwidmFsaWRVbnRpbCI6NDEwMjQ0NDgwMDAwMCwiaXNzdWVkVG8iOiJodHRwOi8vc3R1Yl9pZHAuYWNtZS5vcmcvc3R1Yi1pZHAtb25lL1NTTy9QT1NUIn0.FKbhvdTRN8ZWtwEPEXtbF4YPf-_bDMK8U5nVJaeRK1ZCOQwCKR3UPP4uWXZ0CDYQSI5HTJrmz1dypSk7UhAU5dPPMexKjbif6QSUxpJbl24N0odiOlDzMTPXJD_bXwyJBxc4rccIhjDRYJ8qVRDAiE0jFksRXzRhjfb3HdYR5E3-hNlS1ZSnKXWBa4jFxgrWwJ6vPlDrlPjLs8Y1ByK8vOAYyKjOUKyjF0PqljiZlRgt5QHPZOX3yaO2EEPoLkwNejUi_WWM6O0OxVdpHjXcIL_F07K5UYDtyJDAMM2Z54xNHbk4qfATnDUWK2uXmHlb27X9KhLoToMSyqIFUDoMCQ");

        tokenService.validate(Optional.of(accessToken));
    }

    @Test
    public void shouldValidateWhenTokenIsEmptyAndPrivateBetaDisabled() {
        when(configuration.isPrivateBetaUserAccessRestrictionEnabled()).thenReturn(false);
        tokenService.validate(Optional.empty());
    }

    @Test(expected = InvalidAccessTokenException.class)
    public void shouldNotValidateWhenTokenIsEmptyAndPrivateBetaEnabled() {
        tokenService.validate(Optional.empty());
    }

    @Test(expected = InvalidAccessTokenException.class)
    public void shouldNotValidateWhenEpochIsTooOld() {
        AccessToken accessToken = new AccessToken("eyJhbGciOiJSUzI1NiJ9.eyJlcG9jaCI6MCwidmFsaWRVbnRpbCI6IjIxMDAtMDEtMDFUMDA6MDA6MDAuMDAwWiIsImlzc3VlZFRvIjoiaHR0cDovL3N0dWJfaWRwLmFjbWUub3JnL3N0dWItaWRwLW9uZS9TU08vUE9TVCJ9.sWUh005abGbPHJ9xJwCytGeuNZCKtRyHVLL3_MMSXSo8gZKnRRO19TqcfFwsE9pbhbcFqXzrxyc5hkI93JmeCfIafVhV8vBvSELAgs9prZlujSv_BD1ggOGj1d5uUpD_5I8K5ppGG1IrGsVh-FwdNtFmQr0fIGK6AbWLl-hf6ffFia17E-sXwRBJy139pZlMVqVtW9B-s1qfkdsTo5X7MuL9IuiOHhy24Y4YsgzFL7EoqZK3sBstMwX7AhlycNlLnqKMvmnlYOIBEMuHep3hCve6EkD47z9jO2UtiYhv3XuiI9C7izWPc6dTlLM-y8C6K2_WJ9yrLSXaIUC-vL2opg");

        tokenService.validate(Optional.of(accessToken));
    }

    @Test(expected = InvalidAccessTokenException.class)
    public void shouldNotValidateWhenTokenIsExpired() {
        AccessToken accessToken = new AccessToken("eyJhbGciOiJSUzI1NiJ9.eyJlcG9jaCI6MSwidmFsaWRVbnRpbCI6IjIwMDAtMDEtMDFUMDA6MDA6MDAuMDAwWiIsImlzc3VlZFRvIjoiaHR0cDovL3N0dWJfaWRwLmFjbWUub3JnL3N0dWItaWRwLW9uZS9TU08vUE9TVCJ9.lQ2dwUcNCTtLwsXnkjMpsXPonWmDpAJxXgGgbuDvSbD8uiSASptrUpfIUsKO33PcK2i2yPnE5Uxg-Js0-L-d5fUeH901dBGA3Cr5aRc8stIndMyOSvGcnaiVrRSDtiB5WUvEMYhEW30AS6n18ygLvUzykfKPhrje735YNNPCeUD0enXG1ljiq23TdTukPNNvWuzz2eOJGTZeHEVLOpHQOwfqkin3OkzeGzwDpuozDSBs4jS2rQt86BD1RPnYFFOcsvkFXzwrTStR7ghONfoisJbEobxITeCAc3mf5M9ZBNs4iTg5dvTd-0dQbUawLo7p0XD_thB2g6feOJMXdXHwUg");

        tokenService.validate(Optional.of(accessToken));
    }

    @Test(expected = TokenHasInvalidSignatureException.class)
    public void shouldNotValidateWhenSignedByTheWrongKey() {
        AccessToken accessToken = new AccessToken("eyJhbGciOiJSUzI1NiJ9.eyJlcG9jaCI6MSwidmFsaWRVbnRpbCI6IjIxMDAtMDEtMDFUMDA6MDA6MDAuMDAwWiIsImlzc3VlZFRvIjoiaHR0cDovL3N0dWJfaWRwLmFjbWUub3JnL3N0dWItaWRwLW9uZS9TU08vUE9TVCJ9.AMtcyxhzpyzAJ_tMxPZx9mDnwCP8_vXCjgRUEXhIu-VGWWCoJgxxPR4C9vWeDaKFfE25nYQ_ifwQG8u6W3eJ7tT5-DRpBTwcjQgBUnQ6k0zy2fTY9MQKxaZL6NiZHnRLXJRAagXR9yrntTrenxhEo3I9HZFt_gnG3CP-YeGx-hUC6aXlXWQ5D0dhlhHVAUSHPDpt5cBX8bKET1DVqvlvQeq7FmC9zr03M-_f_AhVw5MSm03wyB7Y94vhOibgu9eP-zzke5agrMQZJecZMUMqLtqRx1TCmIUeohD6vM3hArXw3gtkiKxZuPMgzBXsPy7WMEms0N32Pdq8YjD66hqfyw");

        tokenService.validate(Optional.of(accessToken));
    }

    @Test(expected = CouldNotParseTokenException.class)
    public void shouldNotValidateWhenTokenIsJunk() {
        AccessToken accessToken = new AccessToken("foo");

        tokenService.validate(Optional.of(accessToken));
    }

    @Test(expected = CouldNotParseTokenPayloadException.class)
    public void shouldNotValidateWhenTokenPayloadDoesNotHaveRequiredEntries() {
        AccessToken accessToken = new AccessToken("eyJhbGciOiJSUzI1NiJ9.eyJlcG9jaCI6MSwiaXNzdWVkVG8iOiJodHRwOi8vc3R1Yl9pZHAuYWNtZS5vcmcvc3R1Yi1pZHAtb25lL1NTTy9QT1NUIn0.iEygFt_bExigpX_QJNkaeSbKa6ua3JxPVFPxCW2aHlPIsjUob_K0upK7zdDeunuU0KY16mcmgxzAbCNIGPHOSP0uY_Kmvfn2oVQiPaDJgtqVdJuxXoYEKhb_tEmVcZcTwyvx3HE2emyBFslwgP0qPHCsadOmSmqczw4QrV6TDsLn8ysZyYmovGUlOs4g6lhMHAijzj045_OY3rEoXu8LTaMZVa2i9M0LB3b7Rmfs_Gg5bvoSEW2BiinyfpMlSWPd_Mi38NE3yFx4sLrPIXuF5Drx0QfLWikA4uk8zpIfMsUijRtV5SuufisV5s1jk6-1tsTCsheoq8hjTkjP4w_8Mw");

        tokenService.validate(Optional.of(accessToken));
    }

    @Test
    public void shouldGenerateAValidToken() {
        AccessToken expectedAccessToken = new AccessToken("eyJhbGciOiJSUzI1NiJ9.eyJlcG9jaCI6MSwidmFsaWRVbnRpbCI6NDEwMjQ0NDgwMDAwMCwiaXNzdWVkVG8iOiJodHRwOi8vc3R1Yl9pZHAuYWNtZS5vcmcvc3R1Yi1pZHAtb25lL1NTTy9QT1NUIn0.FKbhvdTRN8ZWtwEPEXtbF4YPf-_bDMK8U5nVJaeRK1ZCOQwCKR3UPP4uWXZ0CDYQSI5HTJrmz1dypSk7UhAU5dPPMexKjbif6QSUxpJbl24N0odiOlDzMTPXJD_bXwyJBxc4rccIhjDRYJ8qVRDAiE0jFksRXzRhjfb3HdYR5E3-hNlS1ZSnKXWBa4jFxgrWwJ6vPlDrlPjLs8Y1ByK8vOAYyKjOUKyjF0PqljiZlRgt5QHPZOX3yaO2EEPoLkwNejUi_WWM6O0OxVdpHjXcIL_F07K5UYDtyJDAMM2Z54xNHbk4qfATnDUWK2uXmHlb27X9KhLoToMSyqIFUDoMCQ");
        GenerateTokenRequestDto tokenDto = new GenerateTokenRequestDto(DateTime.parse("2100-01-01"), STUB_IDP_ONE);

        AccessToken accessToken = tokenService.generate(tokenDto);
        assertThat(accessToken).isEqualTo(expectedAccessToken);
    }

    @Test(expected = TokenGenerationException.class)
    public void shouldNotGenerateATokenThatExpiresInThePast() {
        GenerateTokenRequestDto tokenDto = new GenerateTokenRequestDto(DateTime.now().minusDays(1), STUB_IDP_ONE);

        tokenService.generate(tokenDto);
    }

    @Test
    public void shouldGenerateTokensThatAreValidatedCorrectly() {
        GenerateTokenRequestDto tokenDto = new GenerateTokenRequestDto(DateTime.now().plusDays(1), STUB_IDP_ONE);

        AccessToken accessToken = tokenService.generate(tokenDto);
        tokenService.validate(Optional.of(accessToken));
    }
}
