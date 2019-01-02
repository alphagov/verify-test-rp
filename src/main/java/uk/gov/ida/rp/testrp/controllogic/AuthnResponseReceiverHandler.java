package uk.gov.ida.rp.testrp.controllogic;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.saml.saml2.core.Attribute;
import uk.gov.ida.common.SessionId;
import uk.gov.ida.rp.testrp.domain.ResponseFromHub;
import uk.gov.ida.rp.testrp.repositories.Session;
import uk.gov.ida.rp.testrp.repositories.SessionRepository;
import uk.gov.ida.saml.core.domain.TransactionIdaStatus;
import uk.gov.ida.saml.core.extensions.StringValueSamlObject;
import uk.gov.ida.saml.core.extensions.Verified;
import uk.gov.ida.saml.core.extensions.impl.AddressImpl;
import uk.gov.ida.saml.core.extensions.impl.VerifiedImpl;
import uk.gov.ida.saml.idp.stub.domain.InboundResponseFromHub;

import javax.inject.Inject;
import java.net.URI;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.joining;

public class AuthnResponseReceiverHandler {

    private final SessionRepository sessionRepository;
    private final Function<String, InboundResponseFromHub> samlResponseDeserialiser;

    @Inject
    public AuthnResponseReceiverHandler(SessionRepository sessionRepository,
                                        Function<String, InboundResponseFromHub> samlResponseDeserialiser) {
        this.sessionRepository = sessionRepository;
        this.samlResponseDeserialiser = samlResponseDeserialiser;
    }

    public ResponseFromHub handleResponse(String samlResponse, Optional<SessionId> relayState) {
        InboundResponseFromHub idpResponse = samlResponseDeserialiser.apply(samlResponse);

        if(relayState.isPresent() && idpResponse.getStatus().equals(TransactionIdaStatus.Success)) {
            // note that the compliance-tool does not do matching for the RP tests - however
            // we currently only care that the user has started from a valid session
            // so continue on...

            // if the user has previously matched then their hashed pid is stored in the session repository
            Session session = sessionRepository.getSession(relayState.get()).get();

            if (idpResponse.getAttributes().isPresent()) {
                // display real attributes contined within the saml response
                List<String> attributes = getAttributes(idpResponse);
                return new ResponseFromHub(idpResponse.getStatus(), attributes, Optional.empty(), Optional.ofNullable(session), relayState, idpResponse.getAuthnContext());
            }

            URI location = session.getPathUserWasTryingToAccess();
            return new ResponseFromHub(idpResponse.getStatus(), ImmutableList.of(), Optional.ofNullable(location), Optional.ofNullable(session), relayState, idpResponse.getAuthnContext());
        }
        // not success, not a new user, no relay state
        return new ResponseFromHub(idpResponse.getStatus(), ImmutableList.of(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
    }

    private List<String> getAttributes(InboundResponseFromHub idpResponse) {
        final List<Attribute> attributes = idpResponse.getAttributes().get();
        List<String> attributeNamesAndValues = new ArrayList<>();
        for (Attribute attribute : attributes) {
            attributeNamesAndValues.add(MessageFormat.format("{0}:{1}", attribute.getFriendlyName(), attributeValuesToString(attribute.getAttributeValues())));
        }

        return attributeNamesAndValues;
    }

    private String attributeValuesToString(List<XMLObject> attributeValues) {
        XMLObject firstAttributeValue = attributeValues.get(0);
        if (firstAttributeValue instanceof AddressImpl) {
            List<String> addresses = attributeValues
                    .stream()
                    .map(attributeValue -> convertAddressToString((AddressImpl) attributeValue))
                    .collect(Collectors.toList());
            return Joiner.on(", ").join(addresses);
        } else if(firstAttributeValue instanceof VerifiedImpl) {
            return Boolean.toString(((Verified) firstAttributeValue).getValue());
        } else {
            return ((StringValueSamlObject) firstAttributeValue).getValue();
        }
    }

    private String convertAddressToString(AddressImpl address) {
        String joinedLines = address.getLines().stream()
            .map(StringValueSamlObject::getValue)
            .collect(joining(" "));
        return String.format("%s (verified=%s)", joinedLines, address.getVerified());
    }
}
