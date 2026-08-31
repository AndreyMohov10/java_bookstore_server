package org.example.bookstore.repository;

import org.example.bookstore.dto.OrderResponseDto;
import org.example.bookstore.jooq.tables.Books;
import org.example.bookstore.jooq.tables.Customers;
import org.example.bookstore.jooq.tables.OrderItems;
import org.example.bookstore.jooq.tables.Orders;
import org.example.bookstore.jooq.tables.records.OrderItemsRecord;
import org.example.bookstore.jooq.tables.records.OrdersRecord;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public class OrderRepository {

    private static final Orders     O  = Orders.ORDERS;
    private static final OrderItems OI = OrderItems.ORDER_ITEMS;
    private static final Customers  C  = Customers.CUSTOMERS;
    private static final Books      B  = Books.BOOKS;

    private final DSLContext dsl;

    public OrderRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    public Long createOrder(Long customerId, BigDecimal totalAmount) {
        OrdersRecord record = dsl.insertInto(O)
                .set(O.CUSTOMER_ID,  customerId)
                .set(O.STATUS,       "CONFIRMED")
                .set(O.TOTAL_AMOUNT, totalAmount)
                .returning(O.ID)
                .fetchOne();

        if (record == null) throw new RuntimeException("Ошибка при создании заказа");
        return record.getId();
    }


    public void createOrderItems(Long orderId, List<OrderItemData> items) {
        List<OrderItemsRecord> records = items.stream()
                .map(item -> {
                    OrderItemsRecord r = dsl.newRecord(OI);
                    r.setOrderId(orderId);
                    r.setBookId(item.bookId());
                    r.setQuantity(item.quantity());
                    r.setPricePerItem(item.pricePerItem());
                    return r;
                })
                .toList();

        dsl.batchInsert(records).execute();
    }


    public Optional<OrderResponseDto> findById(Long orderId) {
        var orderRow = dsl.select(O.fields())
                .select(C.NAME.as("customer_name"))
                .from(O)
                .join(C).on(O.CUSTOMER_ID.eq(C.ID))
                .where(O.ID.eq(orderId))
                .fetchOne();

        if (orderRow == null) return Optional.empty();

        List<OrderResponseDto.OrderItemDto> items = dsl
                .select(OI.fields())
                .select(B.TITLE.as("book_title"))
                .from(OI)
                .join(B).on(OI.BOOK_ID.eq(B.ID))
                .where(OI.ORDER_ID.eq(orderId))
                .fetch()
                .map(r -> new OrderResponseDto.OrderItemDto(
                        r.get(OI.ID),
                        r.get(OI.BOOK_ID),
                        r.get(B.TITLE.as("book_title"), String.class),
                        r.get(OI.QUANTITY),
                        r.get(OI.PRICE_PER_ITEM),
                        r.get(OI.PRICE_PER_ITEM)
                          .multiply(BigDecimal.valueOf(r.get(OI.QUANTITY)))
                ));

        return Optional.of(new OrderResponseDto(
                orderRow.get(O.ID),
                orderRow.get(O.CUSTOMER_ID),
                orderRow.get(C.NAME.as("customer_name"), String.class),
                orderRow.get(O.STATUS),
                orderRow.get(O.TOTAL_AMOUNT),
                items,
                orderRow.get(O.CREATED_AT) != null
                        ? orderRow.get(O.CREATED_AT) : null
        ));
    }

    public record OrderItemData(Long bookId, int quantity, BigDecimal pricePerItem) {}
}
