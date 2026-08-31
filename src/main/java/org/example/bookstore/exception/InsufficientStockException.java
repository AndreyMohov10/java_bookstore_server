package org.example.bookstore.exception;

/**
 * Выбрасывается при нехватке книг на складе. HTTP 422.
 */
public class InsufficientStockException extends RuntimeException {

    private final Long bookId;
    private final String bookTitle;
    private final int requested;
    private final int available;

    public InsufficientStockException(Long bookId, String bookTitle, int requested, int available) {
        super(String.format(
                "Недостаточно экземпляров книги \"%s\" (ID=%d): запрошено %d, доступно %d",
                bookTitle, bookId, requested, available));
        this.bookId    = bookId;
        this.bookTitle = bookTitle;
        this.requested = requested;
        this.available = available;
    }

    public Long   getBookId()    { return bookId; }
    public String getBookTitle() { return bookTitle; }
    public int    getRequested() { return requested; }
    public int    getAvailable() { return available; }
}
