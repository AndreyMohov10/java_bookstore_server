package org.example.bookstore.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.bookstore.dto.OrderRequestDto;
import org.example.bookstore.dto.OrderResponseDto;
import org.example.bookstore.exception.InsufficientStockException;
import org.example.bookstore.exception.ResourceNotFoundException;
import org.example.bookstore.service.OrderService;
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


@WebMvcTest(OrderController.class)
@DisplayName("OrderController — MockMvc тесты")
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderService orderService;

    //тестовые данные
    private OrderResponseDto sampleOrderResponse() {
        return new OrderResponseDto(
                1L, 1L, "Иван Иванов", "CONFIRMED",
                new BigDecimal("1700.00"),
                List.of(new OrderResponseDto.OrderItemDto(
                        1L, 1L, "Война и мир", 2,
                        new BigDecimal("850.00"),
                        new BigDecimal("1700.00"))),
                LocalDateTime.now());
    }

    @Nested
    @DisplayName("POST /api/orders")
    class PlaceOrder {

        @Test
        @DisplayName("201 Created — успешное оформление заказа")
        void shouldReturn201OnSuccessfulOrder() throws Exception {
            OrderRequestDto request = new OrderRequestDto(
                    1L,
                    List.of(new OrderRequestDto.OrderItemRequestDto(1L, 2)));

            when(orderService.placeOrder(any())).thenReturn(sampleOrderResponse());

            mockMvc.perform(post("/api/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.status").value("CONFIRMED"))
                    .andExpect(jsonPath("$.totalAmount").value(1700.00))
                    .andExpect(jsonPath("$.customerName").value("Иван Иванов"))
                    .andExpect(jsonPath("$.items", hasSize(1)))
                    .andExpect(jsonPath("$.items[0].bookTitle").value("Война и мир"))
                    .andExpect(jsonPath("$.items[0].subtotal").value(1700.00));
        }

        @Test
        @DisplayName("400 Bad Request — customerId отсутствует")
        void shouldReturn400WhenCustomerIdMissing() throws Exception {
            // Создаём JSON без customerId
            String json = """
                    {
                        "items": [{"bookId": 1, "quantity": 1}]
                    }
                    """;

            mockMvc.perform(post("/api/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.fieldErrors.customerId").exists());
        }

        @Test
        @DisplayName("400 Bad Request — список позиций пуст")
        void shouldReturn400WhenItemsIsEmpty() throws Exception {
            OrderRequestDto request = new OrderRequestDto(1L, List.of());

            mockMvc.perform(post("/api/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.fieldErrors.items").exists());
        }

        @Test
        @DisplayName("400 Bad Request — quantity = 0 (не положительное)")
        void shouldReturn400WhenQuantityIsZero() throws Exception {
            OrderRequestDto request = new OrderRequestDto(
                    1L,
                    List.of(new OrderRequestDto.OrderItemRequestDto(1L, 0)));

            mockMvc.perform(post("/api/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("404 Not Found — книга не найдена")
        void shouldReturn404WhenBookNotFound() throws Exception {
            OrderRequestDto request = new OrderRequestDto(
                    1L,
                    List.of(new OrderRequestDto.OrderItemRequestDto(999L, 1)));

            when(orderService.placeOrder(any()))
                    .thenThrow(new ResourceNotFoundException("Книга", 999L));

            mockMvc.perform(post("/api/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.title").value("Ресурс не найден"));
        }

        @Test
        @DisplayName("422 Unprocessable Entity — недостаточно книг на складе")
        void shouldReturn422WhenInsufficientStock() throws Exception {
            OrderRequestDto request = new OrderRequestDto(
                    1L,
                    List.of(new OrderRequestDto.OrderItemRequestDto(3L, 100)));

            when(orderService.placeOrder(any()))
                    .thenThrow(new InsufficientStockException(3L, "Преступление и наказание", 100, 2));

            mockMvc.perform(post("/api/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.title").value("Недостаточно товара на складе"))
                    .andExpect(jsonPath("$.bookTitle").value("Преступление и наказание"))
                    .andExpect(jsonPath("$.requested").value(100))
                    .andExpect(jsonPath("$.available").value(2));
        }
    }

    @Nested
    @DisplayName("GET /api/orders/{id}")
    class GetOrder {

        @Test
        @DisplayName("200 OK — возвращает заказ с позициями")
        void shouldReturnOrderById() throws Exception {
            when(orderService.getOrderById(1L)).thenReturn(sampleOrderResponse());

            mockMvc.perform(get("/api/orders/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.items", hasSize(1)));
        }

        @Test
        @DisplayName("404 Not Found — заказ не найден")
        void shouldReturn404WhenOrderNotFound() throws Exception {
            when(orderService.getOrderById(999L))
                    .thenThrow(new ResourceNotFoundException("Заказ", 999L));

            mockMvc.perform(get("/api/orders/999"))
                    .andExpect(status().isNotFound());
        }
    }
}
