package vn.edu.bookstore.inventory.api;

import vn.edu.bookstore.inventory.domain.StockItem;

public record StockView(String isbn, int available, int reserved) {

    static StockView from(StockItem s) {
        return new StockView(s.getIsbn(), s.getAvailable(), s.getReserved());
    }
}
