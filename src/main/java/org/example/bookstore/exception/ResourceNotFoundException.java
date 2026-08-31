package org.example.bookstore.exception;

/**
 * Выбрасывается, когда запрашиваемый ресурс не найден в БД. HTTP 404.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String resourceType, Long id) {
        super(resourceType + " с ID " + id + " не найден(а)");
    }

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
