package org.example.bookstore.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.bookstore.dto.AuthorDto;
import org.example.bookstore.service.AuthorService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/authors")
@Tag(name = "Authors", description = "API для управления авторами книг")
public class AuthorController {

    private final AuthorService authorService;

    public AuthorController(AuthorService authorService) {
        this.authorService = authorService;
    }

    @GetMapping
    @Operation(summary = "Получить список всех авторов")
    public List<AuthorDto> getAllAuthors() {
        return authorService.getAllAuthors();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить автора по ID",
               responses = {@ApiResponse(responseCode = "200"), @ApiResponse(responseCode = "404")})
    public AuthorDto getAuthorById(@PathVariable Long id) {
        return authorService.getAuthorById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Создать нового автора")
    public AuthorDto createAuthor(@Valid @RequestBody AuthorDto dto) {
        return authorService.createAuthor(dto);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Обновить данные автора",
               responses = {@ApiResponse(responseCode = "200"), @ApiResponse(responseCode = "404")})
    public AuthorDto updateAuthor(@PathVariable Long id, @Valid @RequestBody AuthorDto dto) {
        return authorService.updateAuthor(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Удалить автора",
               responses = {@ApiResponse(responseCode = "204"), @ApiResponse(responseCode = "404")})
    public void deleteAuthor(@PathVariable Long id) {
        authorService.deleteAuthor(id);
    }
}
