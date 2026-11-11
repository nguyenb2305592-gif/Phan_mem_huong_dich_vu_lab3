package vn.edu.bookstore.legacy.soap;

import jakarta.xml.soap.Detail;
import jakarta.xml.soap.DetailEntry;
import jakarta.xml.soap.SOAPConstants;
import jakarta.xml.soap.SOAPException;
import jakarta.xml.soap.SOAPFactory;
import jakarta.xml.soap.SOAPFault;
import jakarta.xml.ws.soap.SOAPFaultException;
import javax.xml.namespace.QName;

/**
 * Tạo soap:Fault (SOAP 1.1) với faultcode soap:Client / soap:Server và
 * detail chứa bs:ErrorDetail(code, message) như mục 9.5 của đặc tả.
 */
final class SoapFaults {

    static final String BS_NS = "http://bookstore.edu.vn/catalog/v1";

    private SoapFaults() {
    }

    static SOAPFaultException client(String code, String message) {
        return build("Client", code, message);
    }

    static SOAPFaultException server(String code, String message) {
        return build("Server", code, message);
    }

    private static SOAPFaultException build(String faultCode, String code, String message) {
        try {
            SOAPFactory factory = SOAPFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL);
            SOAPFault fault = factory.createFault(message,
                    new QName(SOAPConstants.URI_NS_SOAP_1_1_ENVELOPE, faultCode, "soap"));
            Detail detail = fault.addDetail();
            DetailEntry entry = detail.addDetailEntry(new QName(BS_NS, "ErrorDetail", "bs"));
            entry.addChildElement(new QName(BS_NS, "code", "bs")).addTextNode(code);
            entry.addChildElement(new QName(BS_NS, "message", "bs")).addTextNode(message);
            return new SOAPFaultException(fault);
        } catch (SOAPException e) {
            throw new IllegalStateException(e);
        }
    }
}
