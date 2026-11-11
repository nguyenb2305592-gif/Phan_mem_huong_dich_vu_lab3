package vn.edu.bookstore.order.support;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

/** catalog-service giả lập bằng WireMock (chạy trong JVM kiểm thử). */
public final class CatalogStub {

    private CatalogStub() {
    }

    public static WireMockServer start() {
        WireMockServer server = new WireMockServer(WireMockConfiguration.options().dynamicPort());
        server.start();
        return server;
    }

    public static void book(WireMockServer s, String isbn, String title, String amount, String currency) {
        s.stubFor(get(urlEqualTo("/api/v1/books/" + isbn)).willReturn(aResponse()
                .withStatus(200).withHeader("Content-Type", "application/json")
                .withBody("""
                        {"isbn":"%s","title":"%s","author":"A","price":{"amount":%s,"currency":"%s"},"extraField":"bỏ qua"}
                        """.formatted(isbn, title, amount, currency))));
    }

    public static void notFound(WireMockServer s, String isbn) {
        s.stubFor(get(urlEqualTo("/api/v1/books/" + isbn)).willReturn(aResponse()
                .withStatus(404).withHeader("Content-Type", "application/problem+json")
                .withBody("{\"type\":\"https://api.bookstore.vn/problems/book-not-found\",\"status\":404}")));
    }
}
