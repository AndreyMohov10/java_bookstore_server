package org.example.bookstore.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.bookstore.dto.BookDto;
import org.example.bookstore.exception.ResourceNotFoundException;
import org.example.bookstore.service.BookService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookController.class)
@DisplayName("BookController — MockMvc тесты")
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookService bookService;

    //тестовые данные
    private BookDto sampleBook() {
        return new BookDto(
                1L, "Война и мир", "978-5-17-090830-4", "Исторический роман",
                new BigDecimal("850.00"), 15, 1L, "Лев Толстой", LocalDateTime.now());
    }

    @Nested
    @DisplayName("GET /api/books/{id}")
    class GetById {

        @Test
        @DisplayName("200 OK — возвращает книгу с данными автора")
        void shouldReturnBookById() throws Exception {
            when(bookService.getBookById(1L)).thenReturn(sampleBook());

            mockMvc.perform(get("/api/books/1"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.title").value("Война и мир"))
                    .andExpect(jsonPath("$.authorName").value("Лев Толстой"))
                    .andExpect(jsonPath("$.price").value(850.00))
                    .andExpect(jsonPath("$.isbn").value("978-5-17-090830-4"));
        }

        @Test
        @DisplayName("404 Not Found — возвращает ProblemDetail при отсутствии книги")
        void shouldReturn404WhenBookNotFound() throws Exception {
            when(bookService.getBookById(999L))
                    .thenThrow(new ResourceNotFoundException("Книга", 999L));

            mockMvc.perform(get("/api/books/999"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.title").value("Ресурс не найден"))
                    .andExpect(jsonPath("$.detail").value(containsString("999")));
        }
    }

    @Nested
    @DisplayName("GET /api/books (поиск)")
    class Search {

        @Test
        @DisplayName("200 OK — возвращает список книг")
        void shouldReturnBookList() throws Exception {
            when(bookService.searchBooks(any())).thenReturn(List.of(sampleBook()));

            mockMvc.perform(get("/api/books"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].title").value("Война и мир"));
        }

        @Test
        @DisplayName("200 OK — принимает query-параметры фильтрации")
        void shouldAcceptFilterParams() throws Exception {
            when(bookService.searchBooks(any())).thenReturn(List.of());

            mockMvc.perform(get("/api/books")
                            .param("titleLike", "война")
                            .param("genre", "Роман")
                            .param("minPrice", "500")
                            .param("maxPrice", "1000")
                            .param("inStockOnly", "true")
                            .param("page", "0")
                            .param("size", "5"))
                    .andExpect(status().isOk());

            verify(bookService, times(1)).searchBooks(any());
        }
    }

    @Nested
    @DisplayName("POST /api/books")
    class CreateBook {

        @Test
        @DisplayName("201 Created — создаёт книгу при корректных данных")
        void shouldCreateBookAndReturn201() throws Exception {
            BookDto input = new BookDto(
                    null, "Новая книга", "123-4-56-789012-3", "Роман",
                    new BigDecimal("450.00"), 10, 1L, null, null);
            BookDto created = new BookDto(
                    42L, "Новая книга", "123-4-56-789012-3", "Роман",
                    new BigDecimal("450.00"), 10, 1L, "Лев Толстой", LocalDateTime.now());

            when(bookService.createBook(any())).thenReturn(created);

            mockMvc.perform(post("/api/books")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(input)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(42))
                    .andExpect(jsonPath("$.title").value("Новая книга"));
        }

        @Test
        @DisplayName("400 Bad Request — возвращает ошибки валидации если title пустой")
        void shouldReturn400WhenTitleIsBlank() throws Exception {
            BookDto invalid = new BookDto(
                    null, "",
                    "123-4-56-789012-3", "Роман",
                    new BigDecimal("450.00"), 10, 1L, null, null);

            mockMvc.perform(post("/api/books")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalid)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.title").value("Ошибка валидации"))
                    .andExpect(jsonPath("$.fieldErrors.title").exists());
        }

        @Test
        @DisplayName("400 Bad Request — возвращает ошибку если цена отрицательная")
        void shouldReturn400WhenPriceIsNegative() throws Exception {
            BookDto invalid = new BookDto(
                    null, "Книга", "123-4-56-789012-3", "Роман",
                    new BigDecimal("-100.00"),
                    10, 1L, null, null);

            mockMvc.perform(post("/api/books")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalid)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.fieldErrors.price").exists());
        }

        @Test
        @DisplayName("400 Bad Request — возвращает ошибку если isbn не указан")
        void shouldReturn400WhenIsbnMissing() throws Exception {
            BookDto invalid = new BookDto(
                    null, "Книга", null,
                    "Роман", new BigDecimal("450.00"), 10, 1L, null, null);

            mockMvc.perform(post("/api/books")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalid)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.fieldErrors.isbn").exists());
        }
    }

    @Nested
    @DisplayName("DELETE /api/books/{id}")
    class DeleteBook {

        @Test
        @DisplayName("204 No Content — успешное удаление")
        void shouldReturn204OnDelete() throws Exception {
            doNothing().when(bookService).deleteBook(1L);

            mockMvc.perform(delete("/api/books/1"))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("404 Not Found — возвращает ProblemDetail если книга не найдена")
        void shouldReturn404WhenDeletingNonExistent() throws Exception {
            doThrow(new ResourceNotFoundException("Книга", 999L))
                    .when(bookService).deleteBook(999L);

            mockMvc.perform(delete("/api/books/999"))
                    .andExpect(status().isNotFound());
        }
    }
}
