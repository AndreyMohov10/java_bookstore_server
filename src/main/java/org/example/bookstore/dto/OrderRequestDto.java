package org.example.bookstore.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

@Schema(description = "Запрос на создание нового заказа")
public record OrderRequestDto(

        @NotNull(message = "ID покупателя обязателен")
        @Schema(description = "ID покупателя", example = "1")
        Long customerId,

        @NotEmpty(message = "Заказ должен содержать хотя бы одну позицию")
        @Valid
        @Schema(description = "Список позиций заказа")
        List<OrderItemRequestDto> items
) {

    @Schema(description = "Позиция заказа")
    public record OrderItemRequestDto(

            @NotNull(message = "ID книги обязателен")
            @Schema(description = "ID книги", example = "1")
            Long bookId,

            @NotNull(message = "Количество обязательно")
            @Positive(message = "Количество должно быть положительным")
            @Schema(description = "Количество экземпляров", example = "2")
            Integer quantity
    ) {}
}
