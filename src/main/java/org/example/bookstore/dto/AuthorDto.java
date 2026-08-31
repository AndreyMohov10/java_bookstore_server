package org.example.bookstore.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "Автор книги")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AuthorDto(

        @Schema(description = "ID автора", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
        Long id,

        @NotBlank(message = "Имя автора не может быть пустым")
        @Size(max = 255, message = "Имя автора не должно превышать 255 символов")
        @Schema(description = "Полное имя автора", example = "Лев Толстой")
        String name,

        @Size(max = 2000, message = "Биография не должна превышать 2000 символов")
        @Schema(description = "Биография автора")
        String bio,

        @Past(message = "Дата рождения должна быть в прошлом")
        @Schema(description = "Дата рождения", example = "1828-09-09")
        LocalDate birthDate,

        @Schema(description = "Дата создания записи", accessMode = Schema.AccessMode.READ_ONLY)
        LocalDateTime createdAt
) {}
