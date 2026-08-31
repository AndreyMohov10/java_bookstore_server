package org.example.bookstore.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Информация о заказе")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record OrderResponseDto(

        @Schema(description = "ID заказа", example = "1")
        Long id,

        @Schema(description = "ID покупателя", example = "1")
        Long customerId,

        @Schema(description = "Имя покупателя", example = "Иван Иванов")
        String customerName,

        @Schema(description = "Статус заказа", example = "CONFIRMED",
                allowableValues = {"PENDING", "CONFIRMED", "CANCELLED"})
        String status,

        @Schema(description = "Итоговая сумма заказа", example = "1700.00")
        BigDecimal totalAmount,

        @Schema(description = "Позиции заказа")
        List<OrderItemDto> items,

        @Schema(description = "Дата создания заказа")
        LocalDateTime createdAt
) {

    @Schema(description = "Позиция заказа")
    public record OrderItemDto(

            @Schema(description = "ID позиции", example = "1")
            Long id,

            @Schema(description = "ID книги", example = "3")
            Long bookId,

            @Schema(description = "Название книги", example = "Преступление и наказание")
            String bookTitle,

            @Schema(description = "Количество экземпляров", example = "2")
            Integer quantity,

            @Schema(description = "Цена за единицу на момент заказа", example = "680.00")
            BigDecimal pricePerItem,

            @Schema(description = "Сумма по позиции", example = "1360.00")
            BigDecimal subtotal
    ) {}
}
