package vn.edu.bookstore.contracts;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Tiện ích kiểm chứng (validate) và ánh xạ XML ↔ Java theo lược đồ BookStore.
 * Lớp Java trong các gói vn.edu.bookstore.contracts.* được sinh từ XSD bởi jaxb-maven-plugin.
 */
public final class XmlContracts {

    private static final Schema SCHEMA = loadSchema();
    private static final JAXBContext CONTEXT = createContext();

    private XmlContracts() {
    }

    private static Schema loadSchema() {
        try {
            SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            // Chặn tải lược đồ/DTD từ mạng (phòng XXE)
            sf.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            sf.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "file,jar:file");
            URL order = XmlContracts.class.getResource("/xsd/bookstore-order-v1.xsd");
            URL catalog = XmlContracts.class.getResource("/xsd/bookstore-catalog-v1.xsd");
            return sf.newSchema(new Source[] {
                    new javax.xml.transform.stream.StreamSource(catalog.toExternalForm()),
                    new javax.xml.transform.stream.StreamSource(order.toExternalForm())});
        } catch (SAXException e) {
            throw new IllegalStateException("Không nạp được lược đồ BookStore", e);
        }
    }

    private static JAXBContext createContext() {
        try {
            return JAXBContext.newInstance(
                    vn.edu.bookstore.contracts.catalog.ObjectFactory.class,
                    vn.edu.bookstore.contracts.order.ObjectFactory.class);
        } catch (JAXBException e) {
            throw new IllegalStateException(e);
        }
    }

    public static Schema schema() {
        return SCHEMA;
    }

    /** Kiểm chứng một tài liệu XML, trả về danh sách lỗi (rỗng nếu hợp lệ). */
    public static List<String> validate(Source xml) {
        List<String> errors = new ArrayList<>();
        Validator validator = SCHEMA.newValidator();
        validator.setErrorHandler(new ErrorHandler() {
            @Override public void warning(SAXParseException e) { }
            @Override public void error(SAXParseException e) { errors.add(format(e)); }
            @Override public void fatalError(SAXParseException e) { errors.add(format(e)); }
        });
        try {
            validator.validate(xml);
        } catch (SAXException e) {
            // lỗi đúng dạng (well-formed) đã được ghi trong fatalError
            if (errors.isEmpty()) {
                errors.add(e.getMessage());
            }
        } catch (java.io.IOException e) {
            errors.add("IO: " + e.getMessage());
        }
        return errors;
    }

    private static String format(SAXParseException e) {
        return "dòng " + e.getLineNumber() + ", cột " + e.getColumnNumber() + ": " + e.getMessage();
    }

    /** Đọc XML thành đối tượng, có bật kiểm chứng theo XSD. */
    public static <T> T unmarshal(Source xml, Class<T> type) throws JAXBException {
        Unmarshaller u = CONTEXT.createUnmarshaller();
        u.setSchema(SCHEMA);
        JAXBElement<T> element = u.unmarshal(xml, type);
        return element.getValue();
    }

    /** Ghi đối tượng thành XML, có bật kiểm chứng theo XSD. */
    public static String marshal(Object jaxbElementOrRoot) throws JAXBException {
        Marshaller m = CONTEXT.createMarshaller();
        m.setSchema(SCHEMA);
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        StringWriter out = new StringWriter();
        m.marshal(jaxbElementOrRoot, out);
        return out.toString();
    }
}
