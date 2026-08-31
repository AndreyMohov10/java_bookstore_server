package org.example.bookstore.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Статистика продаж по автору")
public record AuthorSalesStatsDto(

        @Schema(description = "ID автора", example = "2")
        Long authorId,

        @Schema(description = "Имя автора", example = "Фёдор Достоевский")
        String authorName,

        @Schema(description = "Количество проданных книг", example = "45")
        Long totalBooksSold,

        @Schema(description = "Общая выручка по автору", example = "32400.00")
        BigDecimal totalRevenue,

        @Schema(description = "Количество уникальных книг автора в продаже", example = "3")
        Long distinctBooksSold
) {}
