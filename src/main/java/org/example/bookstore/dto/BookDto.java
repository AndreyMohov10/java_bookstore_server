package org.example.bookstore.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Книга в каталоге")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record BookDto(

        @Schema(description = "ID книги", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
        Long id,

        @NotBlank(message = "Название книги не может быть пустым")
        @Size(max = 255, message = "Название не должно превышать 255 символов")
        @Schema(description = "Название книги", example = "Война и мир")
        String title,

        @NotBlank(message = "ISBN не может быть пустым")
        @Size(max = 20, message = "ISBN не должен превышать 20 символов")
        @Schema(description = "ISBN-13", example = "978-5-17-090830-4")
        String isbn,

        @Size(max = 100, message = "Жанр не должен превышать 100 символов")
        @Schema(description = "Жанр книги", example = "Исторический роман")
        String genre,

        @NotNull(message = "Цена обязательна")
        @DecimalMin(value = "0.0", message = "Цена не может быть отрицательной")
        @Schema(description = "Цена книги в рублях", example = "850.00")
        BigDecimal price,

        @Min(value = 0, message = "Количество на складе не может быть отрицательным")
        @Schema(description = "Остаток на складе", example = "15")
        Integer stockQuantity,

        @Schema(description = "ID автора книги", example = "1")
        Long authorId,

        @Schema(description = "Имя автора книги", example = "Лев Толстой", accessMode = Schema.AccessMode.READ_ONLY)
        String authorName,

        @Schema(description = "Дата добавления книги", accessMode = Schema.AccessMode.READ_ONLY)
        LocalDateTime createdAt
) {}
