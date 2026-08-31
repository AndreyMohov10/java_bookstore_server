package org.example.bookstore.service;

import org.example.bookstore.dto.BookDto;
import org.example.bookstore.dto.OrderRequestDto;
import org.example.bookstore.dto.OrderResponseDto;
import org.example.bookstore.exception.InsufficientStockException;
import org.example.bookstore.exception.ResourceNotFoundException;
import org.example.bookstore.repository.BookRepository;
import org.example.bookstore.repository.OrderRepository;
import org.example.bookstore.repository.OrderRepository.OrderItemData;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final BookRepository  bookRepository;

    public OrderService(OrderRepository orderRepository, BookRepository bookRepository) {
        this.orderRepository = orderRepository;
        this.bookRepository  = bookRepository;
    }

    @Transactional
    public OrderResponseDto placeOrder(OrderRequestDto request) {
        List<OrderItemData> itemsData = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (OrderRequestDto.OrderItemRequestDto item : request.items()) {
            BookDto book = bookRepository.findById(item.bookId())
                    .orElseThrow(() -> new ResourceNotFoundException("Книга", item.bookId()));

            int currentStock = bookRepository.getStockQuantity(item.bookId())
                    .orElseThrow(() -> new ResourceNotFoundException("Книга", item.bookId()));

            if (currentStock < item.quantity()) {
                throw new InsufficientStockException(
                        book.id(), book.title(), item.quantity(), currentStock);
            }

            totalAmount = totalAmount.add(book.price().multiply(BigDecimal.valueOf(item.quantity())));
            itemsData.add(new OrderItemData(item.bookId(), item.quantity(), book.price()));
        }

        Long orderId = orderRepository.createOrder(request.customerId(), totalAmount);

        for (OrderItemData item : itemsData) {
            boolean success = bookRepository.decrementStock(item.bookId(), item.quantity());
            if (!success) {
                BookDto book = bookRepository.findById(item.bookId()).orElseThrow();
                int remaining = bookRepository.getStockQuantity(item.bookId()).orElse(0);
                throw new InsufficientStockException(
                        item.bookId(), book.title(), item.quantity(), remaining);
            }
        }

        orderRepository.createOrderItems(orderId, itemsData);

        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Ошибка при получении созданного заказа"));
    }

    @Transactional(readOnly = true)
    public OrderResponseDto getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Заказ", id));
    }
}
