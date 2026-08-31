package org.example.bookstore.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.bookstore.dto.OrderRequestDto;
import org.example.bookstore.dto.OrderResponseDto;
import org.example.bookstore.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "Orders", description = "API для управления заказами")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Оформить новый заказ",
               description = "Атомарно проверяет наличие книг, списывает остатки и создаёт заказ",
               responses = {
                   @ApiResponse(responseCode = "201", description = "Заказ успешно оформлен"),
                   @ApiResponse(responseCode = "400", description = "Ошибка валидации"),
                   @ApiResponse(responseCode = "404", description = "Книга или покупатель не найдены"),
                   @ApiResponse(responseCode = "422", description = "Недостаточно книг на складе")
               })
    public OrderResponseDto placeOrder(@Valid @RequestBody OrderRequestDto request) {
        return orderService.placeOrder(request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить заказ по ID",
               responses = {@ApiResponse(responseCode = "200"), @ApiResponse(responseCode = "404")})
    public OrderResponseDto getOrderById(@PathVariable Long id) {
        return orderService.getOrderById(id);
    }
}
