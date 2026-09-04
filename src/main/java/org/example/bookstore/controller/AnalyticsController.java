package org.example.bookstore.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.bookstore.dto.AuthorSalesStatsDto;
import org.example.bookstore.repository.AnalyticsRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@Tag(name = "Analytics", description = "Аналитические отчёты по продажам")
public class AnalyticsController {

    private final AnalyticsRepository analyticsRepository;

    public AnalyticsController(AnalyticsRepository analyticsRepository) {
        this.analyticsRepository = analyticsRepository;
    }

    @GetMapping("/authors-sales")
    @Operation(summary = "Статистика продаж по авторам",
               description = "Выручка, кол-во проданных книг и уникальных позиций по каждому автору")
    public List<AuthorSalesStatsDto> getAuthorsSalesStats() {
        return analyticsRepository.getAuthorSalesStats();
    }
}
