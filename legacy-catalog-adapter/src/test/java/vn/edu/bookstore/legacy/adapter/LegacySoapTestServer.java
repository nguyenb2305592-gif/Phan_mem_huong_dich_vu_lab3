package vn.edu.bookstore.legacy.adapter;

import jakarta.xml.ws.Endpoint;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.ServerSocket;
import vn.edu.bookstore.legacy.soap.LegacySoapServer;

/** Khởi động BookCatalogService thật trên cổng ngẫu nhiên cho kiểm thử tích hợp. */
public final class LegacySoapTestServer implements AutoCloseable {

    private final Endpoint endpoint;
    private final String url;

    private LegacySoapTestServer(long delayMillis) {
        int port = freePort();
        this.endpoint = LegacySoapServer.start("localhost", port, delayMillis);
        this.url = "http://localhost:" + port + "/ws/catalog";
    }

    public static LegacySoapTestServer start(long delayMillis) {
        return new LegacySoapTestServer(delayMillis);
    }

    public static int freePort() {
        try (ServerSocket s = new ServerSocket(0)) {
            return s.getLocalPort();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public String url() {
        return url;
    }

    @Override
    public void close() {
        endpoint.stop();
    }
}
