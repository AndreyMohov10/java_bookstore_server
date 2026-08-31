package org.example.bookstore.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.bookstore.dto.BookDto;
import org.example.bookstore.dto.BookSearchFilter;
import org.example.bookstore.service.BookService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/books")
@Tag(name = "Books", description = "API для управления каталогом книг")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    @Operation(summary = "Поиск книг с фильтрами и пагинацией")
    public List<BookDto> searchBooks(
            @Parameter(description = "Поиск по части названия") @RequestParam(required = false) String titleLike,
            @Parameter(description = "Фильтр по жанру")         @RequestParam(required = false) String genre,
            @Parameter(description = "Минимальная цена")        @RequestParam(required = false) BigDecimal minPrice,
            @Parameter(description = "Максимальная цена")       @RequestParam(required = false) BigDecimal maxPrice,
            @Parameter(description = "ID автора")               @RequestParam(required = false) Long authorId,
            @Parameter(description = "Только в наличии")        @RequestParam(required = false) Boolean inStockOnly,
            @Parameter(description = "Номер страницы")          @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Размер страницы")         @RequestParam(defaultValue = "10") int size
    ) {
        return bookService.searchBooks(
                new BookSearchFilter(titleLike, genre, minPrice, maxPrice, authorId, inStockOnly, page, size));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить книгу по ID",
               responses = {@ApiResponse(responseCode = "200"), @ApiResponse(responseCode = "404")})
    public BookDto getBookById(@PathVariable Long id) {
        return bookService.getBookById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Добавить новую книгу в каталог")
    public BookDto createBook(@Valid @RequestBody BookDto dto) {
        return bookService.createBook(dto);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Обновить данные книги",
               responses = {@ApiResponse(responseCode = "200"), @ApiResponse(responseCode = "404")})
    public BookDto updateBook(@PathVariable Long id, @Valid @RequestBody BookDto dto) {
        return bookService.updateBook(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Удалить книгу из каталога",
               responses = {@ApiResponse(responseCode = "204"), @ApiResponse(responseCode = "404")})
    public void deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
    }
}
