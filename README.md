## verify-test-rp

[![Build Status](https://travis-ci.org/alphagov/verify-test-rp.svg?branch=master)](https://travis-ci.org/alphagov/verify-test-rp)

A stub relying party used when testing the GOV.UK Verify hub.

* This service is only for internal testing and is not representative of how a service should connect to Verify. For an authorative implementation of how a service can connect to Verify please see the [Technical Onboarding Guide](https://alphagov.github.io/rp-onboarding-tech-docs/) and the [Verify Service Provider](https://www.github.com/alphagov/verify-service-provider).

## Tokens

Test RP can use JWT to control access if the `privateBetaUserAccessRestrictionEnabled` flag is set to true. The following request can be used to generate a token value.

```HTTP
POST /private/generate-token HTTP/1.1
Content-Type: application/json; charset=utf-8
{
    "validUntil":"2019-01-03T10:00:54.118Z",
    "issueTo":"http://stub_idp.acme.org/stub-idp-one/SSO/POST"
}
```

The following response contains a token value.
```
{
  "tokenValue": "eyJhbGciOiJSUzI1NiJ9..."
}
```

## Issues and responsible disclosure

If you think you have discovered a security issue in this code please email [disclosure@digital.cabinet-office.gov.uk](mailto:disclosure@digital.cabinet-office.gov.uk) with details.

For non-security related bugs and feature requests please [raise an issue](https://github.com/alphagov/verify-test-rp/issues/new) in the GitHub issue tracker.

## Code of Conduct
This project is developed under the [Alphagov Code of Conduct](https://github.com/alphagov/code-of-conduct)

## Licence

[LICENCE](LICENCE)
