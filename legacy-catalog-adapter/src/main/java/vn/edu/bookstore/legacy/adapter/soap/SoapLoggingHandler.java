package vn.edu.bookstore.legacy.adapter.soap;

import jakarta.xml.soap.SOAPException;
import jakarta.xml.ws.handler.MessageContext;
import jakarta.xml.ws.handler.soap.SOAPHandler;
import jakarta.xml.ws.handler.soap.SOAPMessageContext;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import javax.xml.namespace.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Ghi log toàn bộ thông điệp SOAP gửi/nhận để sinh viên "đọc thông điệp bằng log".
 * Lưu ý vận hành: ở môi trường thật cần che (mask) dữ liệu nhạy cảm trước khi ghi log.
 */
public class SoapLoggingHandler implements SOAPHandler<SOAPMessageContext> {

    private static final Logger LOG = LoggerFactory.getLogger(SoapLoggingHandler.class);

    @Override
    public boolean handleMessage(SOAPMessageContext context) {
        log(context, false);
        return true;
    }

    @Override
    public boolean handleFault(SOAPMessageContext context) {
        log(context, true);
        return true;
    }

    private void log(SOAPMessageContext context, boolean fault) {
        // TH1-5: phân biệt chiều gửi (outbound) và chiều nhận
        boolean outbound = Boolean.TRUE.equals(context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY));
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            context.getMessage().writeTo(out);
            LOG.info("SOAP {}{}:\n{}", outbound ? "→ gửi" : "← nhận", fault ? " (Fault)" : "",
                    out.toString(StandardCharsets.UTF_8));
        } catch (SOAPException | IOException e) {
            LOG.warn("Không ghi được log thông điệp SOAP: {}", e.getMessage());
        }
    }

    @Override
    public Set<QName> getHeaders() {
        return Set.of();
    }

    @Override
    public void close(MessageContext context) {
        // không cần giải phóng tài nguyên
    }
}
