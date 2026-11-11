package vn.edu.bookstore.legacy.soap;

import jakarta.xml.ws.Endpoint;
import java.util.logging.Logger;

/**
 * Khởi động BookCatalogService tại http://0.0.0.0:8091/ws/catalog (WSDL: ...?wsdl).
 * Biến môi trường:
 *   LEGACY_PORT      cổng lắng nghe (mặc định 8091)
 *   LEGACY_DELAY_MS  độ trễ giả lập mỗi lời gọi, dùng cho bài sự cố/timeout (mặc định 0)
 *   LEGACY_DUMP      true để in toàn bộ thông điệp SOAP vào/ra
 */
public final class LegacySoapServer {

    private static final Logger LOG = Logger.getLogger(LegacySoapServer.class.getName());

    private LegacySoapServer() {
    }

    public static Endpoint start(String host, int port, long delayMillis) {
        String address = "http://" + host + ":" + port + "/ws/catalog";
        Endpoint endpoint = Endpoint.publish(address, new BookCatalogServiceImpl(new LegacyBookStore(), delayMillis));
        LOG.info("BookCatalogService sẵn sàng tại " + address + "?wsdl");
        return endpoint;
    }

    public static void main(String[] args) {
        int port = Integer.parseInt(System.getenv().getOrDefault("LEGACY_PORT", "8091"));
        long delay = Long.parseLong(System.getenv().getOrDefault("LEGACY_DELAY_MS", "0"));
        if (Boolean.parseBoolean(System.getenv().getOrDefault("LEGACY_DUMP", "false"))) {
            System.setProperty("com.sun.xml.ws.transport.http.HttpAdapter.dump", "true");
        }
        start("0.0.0.0", port, delay);
    }
}
