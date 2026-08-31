package org.example.bookstore.repository;

import org.example.bookstore.dto.AuthorSalesStatsDto;
import org.example.bookstore.jooq.tables.Authors;
import org.example.bookstore.jooq.tables.Books;
import org.example.bookstore.jooq.tables.OrderItems;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;


@Repository
public class AnalyticsRepository {

    private static final OrderItems ORDER_ITEMS = OrderItems.ORDER_ITEMS;
    private static final Books BOOKS = Books.BOOKS;
    private static final Authors AUTHORS = Authors.AUTHORS;

    private final DSLContext dsl;

    public AnalyticsRepository(DSLContext dsl) {
        this.dsl = dsl;
    }


    public List<AuthorSalesStatsDto> getAuthorSalesStats() {
        return dsl
                .select(
                        AUTHORS.ID,
                        AUTHORS.NAME,
                        DSL.sum(ORDER_ITEMS.QUANTITY)                             .as("total_books_sold"),
                        DSL.sum(ORDER_ITEMS.PRICE_PER_ITEM.mul(ORDER_ITEMS.QUANTITY))      .as("total_revenue"),
                        DSL.countDistinct(ORDER_ITEMS.BOOK_ID)                    .as("distinct_books_sold")
                )
                .from(AUTHORS)
                .join(BOOKS).on(BOOKS.AUTHOR_ID.eq(AUTHORS.ID))
                .join(ORDER_ITEMS).on(ORDER_ITEMS.BOOK_ID.eq(BOOKS.ID))
                .groupBy(AUTHORS.ID, AUTHORS.NAME)
                .orderBy(DSL.sum(ORDER_ITEMS.PRICE_PER_ITEM.mul(ORDER_ITEMS.QUANTITY)).desc())
                .fetch()
                .map(r -> new AuthorSalesStatsDto(
                        r.get(AUTHORS.ID),
                        r.get(AUTHORS.NAME),
                        r.get("total_books_sold",    Long.class),
                        r.get("total_revenue",       BigDecimal.class),
                        r.get("distinct_books_sold", Long.class)
                ));
    }
}
