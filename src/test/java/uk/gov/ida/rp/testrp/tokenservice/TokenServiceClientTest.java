package uk.gov.ida.rp.testrp.tokenservice;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.ida.common.shared.configuration.DeserializablePublicKeyConfiguration;
import uk.gov.ida.common.shared.security.X509CertificateFactory;
import uk.gov.ida.rp.testrp.TestRpConfiguration;
import uk.gov.ida.rp.testrp.domain.AccessToken;
import uk.gov.ida.rp.testrp.exceptions.CouldNotParseTokenException;
import uk.gov.ida.rp.testrp.exceptions.CouldNotParseTokenPayloadException;
import uk.gov.ida.rp.testrp.exceptions.TokenHasInvalidSignatureException;
import uk.gov.ida.saml.core.test.TestCertificateStrings;

import java.security.PublicKey;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TokenServiceClientTest {

    TokenServiceClient tokenServiceClient;

    @Mock
    private TestRpConfiguration configuration;
    @Mock
    private DeserializablePublicKeyConfiguration deserializablePublicKeyConfiguration;

    private static PublicKey publicKey = new X509CertificateFactory().createCertificate(TestCertificateStrings.TEST_RP_PUBLIC_SIGNING_CERT).getPublicKey();

    @Before
    public void setUp() {
        when(deserializablePublicKeyConfiguration.getPublicKey()).thenReturn(publicKey);
        when(configuration.getPublicSigningCert()).thenReturn(deserializablePublicKeyConfiguration);
        when(configuration.getTokenEpoch()).thenReturn(1);
        tokenServiceClient = new TokenServiceClient(configuration);
    }

    @Test
    public void shouldValidateWhenSignedProperlyAndInDateAndEpoch() {
        AccessToken accessToken = new AccessToken("eyJhbGciOiJSUzUxMiJ9.eyJlcG9jaCI6MSwidmFsaWRfdW50aWwiOiIyMTE4LTExLTI3VDExOjMwOjAwLjAwMFoiLCJpc3N1ZWRfdG8iOiJ3aWxscC1ibCJ9.rFS6Gx3kb8OTniEHXtWBttoqu-dY_GhwWsWyQcA9wQAGC0EpuRy_EaGYljvhFbXsKKJ1mQsps4pQg6E5QP1g9GFxX_FRWyxKW0GkBe_eT5aCtm6Z9Xzi4VyfyeJEVqUk__fPNwACBpJRsYqL53i3T9S1pegWG16rx6eCykQ_jFLDJnPo6n5QMSp6e0dI4gxbYpntNCFbDh5nD4TpHFG405fs43e4DfJXlPhJ_sThHiZXfKWW4AQbQ7HSAfLC8COs-p8UnaohRCkiDShlzoHII6NUAXbAq6_EEsigPEm3i3dLzpWf9FYstLuu99iUoWRPzK3JadJ4_6snMwATvTnD9Q");

        assertThat(tokenServiceClient.validateToken(accessToken).isValid()).isTrue();
    }

    @Test
    public void shouldNotValidateWhenEpochIsTooOld() {
        AccessToken accessToken = new AccessToken("eyJhbGciOiJSUzUxMiJ9.eyJlcG9jaCI6MCwidmFsaWRfdW50aWwiOiIyMDE4LTExLTI3VDExOjMwOjAwLjAwMFoiLCJpc3N1ZWRfdG8iOiJ3aWxscC1ibCJ9.Vw_AxRt6lAQa1UkEOSeZDEb9RXpDmLs0Byr50omM-2nJzu4qL9RMiS-HTzk4Es4r63BReV1mpAe1B3xgTYHTHphyxduDUVQWNfro-D28EmcmojjFa5OTvic1NV1fEh78v2AaW4kTvneEBDVs5Inx7UGzQZcbFW-lRO8V-fEpuAxJvZL6zRXdVVhbnaBbvvWR6ZF7T5Rpfb39qaMVHJZJFn25tZIYqt6-A86e5NS1Sa4L_NfFlddtr9ngGZAwOsuimi9X_WMU2Pd4IuiVHtnhPOsriBsbgvw-tRLp-ikD2D8CXFMYh8m2k1JODBfICZAv3r_AIr72earnvGuFy3G4lw");

        assertThat(tokenServiceClient.validateToken(accessToken).isValid()).isFalse();
    }

    @Test
    public void shouldNotValidateWhenTokenIsExpired() {
        when(configuration.getTokenEpoch()).thenReturn(0);
        AccessToken accessToken = new AccessToken("eyJhbGciOiJSUzUxMiJ9.eyJlcG9jaCI6MCwidmFsaWRfdW50aWwiOiIyMDE4LTExLTI3VDExOjMwOjAwLjAwMFoiLCJpc3N1ZWRfdG8iOiJ3aWxscC1ibCJ9.Vw_AxRt6lAQa1UkEOSeZDEb9RXpDmLs0Byr50omM-2nJzu4qL9RMiS-HTzk4Es4r63BReV1mpAe1B3xgTYHTHphyxduDUVQWNfro-D28EmcmojjFa5OTvic1NV1fEh78v2AaW4kTvneEBDVs5Inx7UGzQZcbFW-lRO8V-fEpuAxJvZL6zRXdVVhbnaBbvvWR6ZF7T5Rpfb39qaMVHJZJFn25tZIYqt6-A86e5NS1Sa4L_NfFlddtr9ngGZAwOsuimi9X_WMU2Pd4IuiVHtnhPOsriBsbgvw-tRLp-ikD2D8CXFMYh8m2k1JODBfICZAv3r_AIr72earnvGuFy3G4lw");

        assertThat(tokenServiceClient.validateToken(accessToken).isValid()).isFalse();
    }

    @Test(expected = TokenHasInvalidSignatureException.class)
    public void shouldNotValidateWhenSignedByTheWrongKey() {
        AccessToken accessToken = new AccessToken("eyJhbGciOiJSUzUxMiJ9.eyJlcG9jaCI6MSwidmFsaWRfdW50aWwiOiIyMDE4LTExLTI3VDExOjMwOjAwLjAwMFoiLCJpc3N1ZWRfdG8iOiJ3aWxscC1ibCJ9.fnfgiw3eFwAqmF0sDwX5NJHaOz5vhfuGJfRQNvxPV6luDOXSoLQ7Ykjukc_l49QUBsxgCSJgP49ATT-f_X0nBmJ9etR4iDwSnCWQehif7ObEobWHnyQMsGroz0uz6qD01evRYoYMN_EbIGdYw0NJqTdHztxpcCvB7laT3lkNQFO2PrKxcPezHj60LmR8Q__0HE-wjB181hZfKZV2bqDR9qXIrAS4z1wJLWR-QPtgBGuF0F5Crye7NY3UyJ5gGnCsahCIxVHYeGXZia60AJcWYphKzu3r2ko-P4TbW_BCZlHogU1VCLo4Yg4QJ5i46-gJUpI-ZMXS4NA5M-tay6VBDQ");

        tokenServiceClient.validateToken(accessToken);
    }

    @Test(expected = CouldNotParseTokenException.class)
    public void shouldNotValidateWhenTokenIsJunk() {
        AccessToken accessToken = new AccessToken("foo");

        tokenServiceClient.validateToken(accessToken);
    }

    @Test(expected = CouldNotParseTokenPayloadException.class)
    public void shouldNotValidateWhenTokenPayloadDoesNotHaveRequiredEntries() {
        AccessToken accessToken = new AccessToken("eyJhbGciOiJSUzUxMiJ9.e30.Jbd7n6o-6acFcicvb6dxpEmtAF9xj7m_QRrymPXzdjFBh1MzJz_Y2AKQFX3y8F7AY2-jHqX8fRMcginOFogCSa0duUGkjoYmlQ5KvXYCtvC6d4xy0nYPJmEefsfKexUEyNG5wrW8sALuq0F6n5dOHBe6dLmU29fFO6lwGoy36PkQh9uoMN8ApLKY-RSedNN1Wola5rmpiqROseAQSkXSyeTGtGJHQqmyBja2H92wyMJ26x3Cv4RbvabEeXpzDldBjhYA5MxShhlayZ36NG10uPbMPGoQghBqgGkaZtl3xlPgflg5pOKT2X63PPK5_PaTs8TIupGiJIIihRY5xjt9jw");

        tokenServiceClient.validateToken(accessToken);
    }
}
