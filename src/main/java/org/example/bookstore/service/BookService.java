package org.example.bookstore.service;

import org.example.bookstore.dto.BookDto;
import org.example.bookstore.dto.BookSearchFilter;
import org.example.bookstore.exception.ResourceNotFoundException;
import org.example.bookstore.repository.BookRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class BookService {

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public List<BookDto> searchBooks(BookSearchFilter filter) {
        return bookRepository.search(filter);
    }

    public BookDto getBookById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Книга", id));
    }

    @Transactional
    public BookDto createBook(BookDto dto) {
        return bookRepository.create(dto);
    }

    @Transactional
    public BookDto updateBook(Long id, BookDto dto) {
        return bookRepository.update(id, dto);
    }

    @Transactional
    public void deleteBook(Long id) {
        if (!bookRepository.deleteById(id)) {
            throw new ResourceNotFoundException("Книга", id);
        }
    }
}
