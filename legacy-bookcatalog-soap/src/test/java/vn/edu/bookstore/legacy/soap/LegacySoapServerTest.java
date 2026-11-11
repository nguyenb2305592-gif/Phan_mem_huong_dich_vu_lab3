package vn.edu.bookstore.legacy.soap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.xml.ws.Endpoint;
import java.net.ServerSocket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class LegacySoapServerTest {

    private static Endpoint endpoint;
    private static String url;
    private static final HttpClient HTTP = HttpClient.newHttpClient();

    @BeforeAll
    static void start() throws Exception {
        int port;
        try (ServerSocket s = new ServerSocket(0)) {
            port = s.getLocalPort();
        }
        endpoint = LegacySoapServer.start("localhost", port, 0);
        url = "http://localhost:" + port + "/ws/catalog";
    }

    @AfterAll
    static void stop() {
        endpoint.stop();
    }

    private static HttpResponse<String> call(String action, String body) throws Exception {
        String envelope = """
                <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/"
                               xmlns:msg="http://bookstore.edu.vn/catalog/messages/v1">
                  <soap:Body>%s</soap:Body>
                </soap:Envelope>""".formatted(body);
        HttpRequest req = HttpRequest.newBuilder(URI.create(url))
                .header("Content-Type", "text/xml; charset=utf-8")
                .header("SOAPAction", "\"http://bookstore.edu.vn/catalog/" + action + "\"")
                .POST(HttpRequest.BodyPublishers.ofString(envelope)).build();
        return HTTP.send(req, HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void wsdlDuocCongBo() throws Exception {
        HttpResponse<String> r = HTTP.send(HttpRequest.newBuilder(URI.create(url + "?wsdl")).build(),
                HttpResponse.BodyHandlers.ofString());
        assertEquals(200, r.statusCode());
        assertTrue(r.body().contains("BookCatalogPortType"));
    }

    @Test
    void getBookTraVeSach() throws Exception {
        HttpResponse<String> r = call("GetBook",
                "<msg:GetBookRequest><msg:isbn>9780134685991</msg:isbn></msg:GetBookRequest>");
        assertEquals(200, r.statusCode());
        assertTrue(r.body().contains("Effective Java"), r.body());
        assertTrue(r.body().contains("285000"), r.body());
    }

    @Test
    void sachKhongTonTaiTraVeFaultClient() throws Exception {
        HttpResponse<String> r = call("GetBook",
                "<msg:GetBookRequest><msg:isbn>9789999999999</msg:isbn></msg:GetBookRequest>");
        assertEquals(500, r.statusCode());
        assertTrue(r.body().contains(":Client</faultcode>"), r.body());
        assertTrue(r.body().contains("BOOK_NOT_FOUND"), r.body());
    }

    @Test
    void isbnSaiTraVeFaultClient() throws Exception {
        HttpResponse<String> r = call("GetBook",
                "<msg:GetBookRequest><msg:isbn>12345</msg:isbn></msg:GetBookRequest>");
        assertTrue(r.body().contains("INVALID_ISBN"), r.body());
    }

    @Test
    void loiKhoDuLieuTraVeFaultServer() throws Exception {
        HttpResponse<String> r = call("GetBook",
                "<msg:GetBookRequest><msg:isbn>9790000000000</msg:isbn></msg:GetBookRequest>");
        assertTrue(r.body().contains(":Server</faultcode>"), r.body());
        assertTrue(r.body().contains("BACKEND_ERROR"), r.body());
    }

    @Test
    void searchBooksTimTheoTuKhoa() throws Exception {
        HttpResponse<String> r = call("SearchBooks",
                "<msg:SearchBooksRequest><msg:keyword>micro</msg:keyword></msg:SearchBooksRequest>");
        assertEquals(200, r.statusCode());
        assertTrue(r.body().contains("Microservices Patterns"));
        assertTrue(r.body().contains("Building Microservices"));
    }
}
