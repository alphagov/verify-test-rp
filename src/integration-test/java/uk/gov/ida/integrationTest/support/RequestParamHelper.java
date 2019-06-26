package uk.gov.ida.integrationTest.support;

import org.apache.commons.codec.binary.Base64;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Optional;

public class RequestParamHelper {

    private RequestParamHelper() {}

    public static class RequestParams {

        private final Optional<String> relayState;
        private final Optional<String> requestId;

        public RequestParams(String relayState, String requestId) {

            this.relayState = Optional.ofNullable(relayState);
            this.requestId = Optional.ofNullable(requestId);
        }

        public Optional<String> getRelayState() {
            return relayState;
        }

        public Optional<String> getRequestId() {
            return requestId;
        }
    }

    public static RequestParams getParamsFromSamlForm(Document samlForm) {
        String relayState = null;
        String requestId = null;
        final NodeList inputs = samlForm.getElementsByTagName("input");
        for(int i=0;i<inputs.getLength();i++) {
            if(inputs.item(i).hasAttributes() && inputs.item(i).getAttributes().getNamedItem("name").getNodeValue().equals("RelayState")) {
                relayState = inputs.item(i).getAttributes().getNamedItem("value").getNodeValue();
            }
            if(inputs.item(i).hasAttributes() && inputs.item(i).getAttributes().getNamedItem("name").getNodeValue().equals("SAMLRequest")) {
                requestId = extractRequestId(inputs.item(i).getAttributes().getNamedItem("value").getNodeValue());
            }
        }
        return new RequestParams(relayState, requestId);
    }

    private static String extractRequestId(String value) {
        try {
            final Document request = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(Base64.decodeBase64(value)));
            return request.getElementsByTagName("saml2p:AuthnRequest").item(0).getAttributes().getNamedItem("ID").getNodeValue();
        } catch (SAXException | ParserConfigurationException | IOException e) {
            return null;
        }
    }

}
