package org.example.bookstore.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Фильтр для поиска книг в каталоге")
public record BookSearchFilter(

        @Schema(description = "Поиск по части названия (регистронезависимый)", example = "война")
        String titleLike,

        @Schema(description = "Фильтр по жанру (точное совпадение)", example = "Роман")
        String genre,

        @Schema(description = "Минимальная цена", example = "300.00")
        BigDecimal minPrice,

        @Schema(description = "Максимальная цена", example = "900.00")
        BigDecimal maxPrice,

        @Schema(description = "Фильтр по ID автора", example = "1")
        Long authorId,

        @Schema(description = "Только книги в наличии (stock_quantity > 0)", example = "true")
        Boolean inStockOnly,

        @Schema(description = "Номер страницы (0-based)", example = "0")
        Integer page,

        @Schema(description = "Размер страницы", example = "10")
        Integer size
) {
    public static final int DEFAULT_PAGE = 0;
    public static final int DEFAULT_SIZE = 10;

    public int pageOrDefault() {
        return page != null && page >= 0 ? page : DEFAULT_PAGE;
    }

    public int sizeOrDefault() {
        return size != null && size > 0 ? size : DEFAULT_SIZE;
    }
}
