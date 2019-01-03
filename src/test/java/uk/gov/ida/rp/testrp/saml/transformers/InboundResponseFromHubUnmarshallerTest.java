package uk.gov.ida.rp.testrp.saml.transformers;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.Status;
import uk.gov.ida.saml.core.domain.PassthroughAssertion;
import uk.gov.ida.saml.core.domain.TransactionIdaStatus;
import uk.gov.ida.saml.core.test.OpenSAMLMockitoRunner;
import uk.gov.ida.saml.hub.transformers.inbound.PassthroughAssertionUnmarshaller;
import uk.gov.ida.saml.hub.transformers.inbound.TransactionIdaStatusUnmarshaller;
import uk.gov.ida.saml.idp.stub.domain.InboundResponseFromHub;
import uk.gov.ida.saml.security.validators.ValidatedAssertions;
import uk.gov.ida.saml.security.validators.ValidatedResponse;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.ida.rp.testrp.builders.PassthroughAssertionBuilder.aPassthroughAssertion;
import static uk.gov.ida.saml.core.test.builders.AssertionBuilder.anAssertion;
import static uk.gov.ida.saml.core.test.builders.AttributeStatementBuilder.anAttributeStatement;
import static uk.gov.ida.saml.core.test.builders.PersistentIdBuilder.aPersistentId;
import static uk.gov.ida.saml.core.test.builders.PersonNameAttributeBuilder_1_1.aPersonName_1_1;
import static uk.gov.ida.saml.core.test.builders.ResponseBuilder.aResponse;

@RunWith(OpenSAMLMockitoRunner.class)
public class InboundResponseFromHubUnmarshallerTest {

    @Mock
    private TransactionIdaStatusUnmarshaller statusUnmarshaller;
    @Mock
    private PassthroughAssertionUnmarshaller assertionUnmarshaller;

    private InboundResponseFromHubUnmarshaller unmarshaller;

    @Before
    public void setup() {
        unmarshaller = new InboundResponseFromHubUnmarshaller(statusUnmarshaller, assertionUnmarshaller);
    }

    @Test
    public void transform_shouldTransformTheMatchingServiceAssertion() throws Exception {
        Assertion assertion = anAssertion().buildUnencrypted();
        Response originalResponse = aResponse()
                .addAssertion(assertion)
                .build();
        PassthroughAssertion transformedMatchingServiceAssertion =
                aPassthroughAssertion().withPersistentId(aPersistentId().withNameId("some-id").build()).buildMatchingServiceAssertion();
        TransactionIdaStatus transformedStatus = TransactionIdaStatus.Success;
        when(statusUnmarshaller.fromSaml(any(Status.class))).thenReturn(transformedStatus);
        when(assertionUnmarshaller.fromAssertion(any(Assertion.class)))
                .thenReturn(transformedMatchingServiceAssertion);

        InboundResponseFromHub transformedResponse = unmarshaller.fromSaml(new ValidatedResponse(originalResponse), new ValidatedAssertions(Collections.singletonList(assertion)));

        assertThat(transformedResponse.getStatus()).isEqualTo(transformedStatus);
        assertThat(transformedResponse.getPersistentId().get().getNameId()).isEqualTo("some-id");
        assertThat(transformedResponse.getAuthnContext()).isEqualTo(transformedMatchingServiceAssertion.getAuthnContext());
    }

    @Test
    public void transform_shouldPassThroughAttributeStatementsIfPresent() throws Exception {
        Attribute personNameAttribute = aPersonName_1_1().buildAsFirstname();
        Assertion assertionWithAttributes = anAssertion().addAttributeStatement(anAttributeStatement().addAttribute(personNameAttribute).build()).buildUnencrypted();
        Response originalResponse = aResponse().addAssertion(assertionWithAttributes).build();
        when(assertionUnmarshaller.fromAssertion(assertionWithAttributes)).thenReturn(aPassthroughAssertion().buildMatchingServiceAssertion());

        InboundResponseFromHub transformedResponse = unmarshaller.fromSaml(new ValidatedResponse(originalResponse), new ValidatedAssertions(Collections.singletonList(assertionWithAttributes)));

        assertThat(transformedResponse.getAttributes().get()).hasSize(1);
        assertThat(transformedResponse.getAttributes().get().get(0)).isEqualTo(personNameAttribute);
    }
}
