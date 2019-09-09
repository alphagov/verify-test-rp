package uk.gov.ida.rp.testrp;

public interface Urls {

    String LOGIN_PATH = "/login";
    String SUCCESSFUL_REGISTER_PATH = "/success";
    String LOGOUT_PATH = "/logout";
    String SUCCESSFUL_IDP_PATH = "/success-idp" ;
    String GENERATE_TOKEN_PATH = "/generate-token";

    interface PrivateUrls {
        String PRIVATE_ROOT = "/private";
        String GENERATE_TOKEN_RESOURCE = PRIVATE_ROOT + Urls.GENERATE_TOKEN_PATH;
    }

    interface TestRpUrls {
        String TEST_RP_ROOT = "/test-rp";
        String LOGIN_RESOURCE = TEST_RP_ROOT + Urls.LOGIN_PATH;
        String COOKIES_INFO_RESOURCE = TEST_RP_ROOT + "/cookies";

        String LOCAL_MATCHING_SERVICE_PATH = "/matching-service/POST";
        String LOCAL_MATCHING_SERVICE_RESOURCE = TEST_RP_ROOT + LOCAL_MATCHING_SERVICE_PATH;
        String UNKNOWN_USER_CREATION_SERVICE_PATH = "/unknown-user/POST";
        String UNKNOWN_USER_CREATION_SERVICE_RESOURCE = TEST_RP_ROOT + UNKNOWN_USER_CREATION_SERVICE_PATH;

        String SUCCESSFUL_REGISTER_RESOURCE = TEST_RP_ROOT + SUCCESSFUL_REGISTER_PATH;
    }

    interface HeadlessUrls {
        String HEADLESS_ROOT = "/headless-rp";
        String LOGIN_PATH = Urls.LOGIN_PATH;
        String SUCCESS_PATH = HEADLESS_ROOT + SUCCESSFUL_REGISTER_PATH;
        String SUCCESS_PATH_IDP = HEADLESS_ROOT + SUCCESSFUL_IDP_PATH;
    }

    interface Cookies {
        String TEST_RP_SESSION_COOKIE_NAME = "test-rp-session";
    }

    interface Params {
        String SAML_RESPONSE_PARAM = "SAMLResponse";
        String RELAY_STATE_PARAM = "RelayState";

        String ERROR_CODE_PARAM = "errorCode";
        String ACCESS_TOKEN_PARAM = "token";
        String RP_NAME_PARAM = "rp-name";
        String JOURNEY_HINT_PARAM = "journey_hint";
        String LOA_PARAM = "loa";
        String ATTRIBUTES_PARAM = "attributes";
        String EIDAS_PARAM = "eidas";
        String NO_MATCH = "no-match";
        String FAIL_ACCOUNT_CREATION = "fail-account-creation";

        String GA_CLIENT_ID = "_ga";
    }

}
