package org.example.bookstore.repository;

import org.example.bookstore.dto.BookDto;
import org.example.bookstore.dto.BookSearchFilter;
import org.example.bookstore.exception.ResourceNotFoundException;
import org.example.bookstore.jooq.tables.Authors;
import org.example.bookstore.jooq.tables.Books;
import org.example.bookstore.jooq.tables.records.BooksRecord;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Repository
public class BookRepository {

    private static final Books BOOKS = Books.BOOKS;
    private static final Authors AUTHORS = Authors.AUTHORS;

    private final DSLContext dsl;

    public BookRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    public Optional<BookDto> findById(Long id) {
        return dsl.select(BOOKS.fields())
                .select(AUTHORS.NAME.as("author_name"))
                .from(BOOKS)
                .leftJoin(AUTHORS).on(BOOKS.AUTHOR_ID.eq(AUTHORS.ID))
                .where(BOOKS.ID.eq(id))
                .fetchOptional()
                .map(r -> new BookDto(
                        r.get(BOOKS.ID),
                        r.get(BOOKS.TITLE),
                        r.get(BOOKS.ISBN),
                        r.get(BOOKS.GENRE),
                        r.get(BOOKS.PRICE),
                        r.get(BOOKS.STOCK_QUANTITY),
                        r.get(BOOKS.AUTHOR_ID),
                        r.get(AUTHORS.NAME.as("author_name"), String.class),
                        r.get(BOOKS.CREATED_AT) != null ? r.get(BOOKS.CREATED_AT) : null
                ));
    }

    public List<BookDto> search(BookSearchFilter filter) {
        List<Condition> conditions = new ArrayList<>();

        if (filter.titleLike() != null && !filter.titleLike().isBlank()) {
            conditions.add(BOOKS.TITLE.likeIgnoreCase("%" + filter.titleLike() + "%"));
        }
        if (filter.genre() != null && !filter.genre().isBlank()) {
            conditions.add(BOOKS.GENRE.equalIgnoreCase(filter.genre()));
        }
        if (filter.minPrice() != null) {
            conditions.add(BOOKS.PRICE.ge(filter.minPrice()));
        }
        if (filter.maxPrice() != null) {
            conditions.add(BOOKS.PRICE.le(filter.maxPrice()));
        }
        if (filter.authorId() != null) {
            conditions.add(BOOKS.AUTHOR_ID.eq(filter.authorId()));
        }
        if (Boolean.TRUE.equals(filter.inStockOnly())) {
            conditions.add(BOOKS.STOCK_QUANTITY.gt(0));
        }

        Condition where = conditions.isEmpty() ? DSL.trueCondition() : DSL.and(conditions);
        int page = filter.pageOrDefault();
        int size = filter.sizeOrDefault();

        return dsl.select(BOOKS.fields())
                .select(AUTHORS.NAME.as("author_name"))
                .from(BOOKS)
                .leftJoin(AUTHORS).on(BOOKS.AUTHOR_ID.eq(AUTHORS.ID))
                .where(where)
                .orderBy(BOOKS.TITLE.asc())
                .limit(size)
                .offset((long) page * size)
                .fetch()
                .map(r -> new BookDto(
                        r.get(BOOKS.ID),
                        r.get(BOOKS.TITLE),
                        r.get(BOOKS.ISBN),
                        r.get(BOOKS.GENRE),
                        r.get(BOOKS.PRICE),
                        r.get(BOOKS.STOCK_QUANTITY),
                        r.get(BOOKS.AUTHOR_ID),
                        r.get(AUTHORS.NAME.as("author_name"), String.class),
                        r.get(BOOKS.CREATED_AT) != null ? r.get(BOOKS.CREATED_AT) : null
                ));
    }

    public BookDto create(BookDto dto) {
        BooksRecord record = dsl.insertInto(BOOKS)
                .set(BOOKS.TITLE,          dto.title())
                .set(BOOKS.ISBN,           dto.isbn())
                .set(BOOKS.GENRE,          dto.genre())
                .set(BOOKS.PRICE,          dto.price())
                .set(BOOKS.STOCK_QUANTITY, dto.stockQuantity() != null ? dto.stockQuantity() : 0)
                .set(BOOKS.AUTHOR_ID,      dto.authorId())
                .returning()
                .fetchOne();

        if (record == null) throw new RuntimeException("Ошибка при создании книги");
        return findById(record.getId()).orElseThrow();
    }

    public BookDto update(Long id, BookDto dto) {
        int updated = dsl.update(BOOKS)
                .set(BOOKS.TITLE,          dto.title())
                .set(BOOKS.ISBN,           dto.isbn())
                .set(BOOKS.GENRE,          dto.genre())
                .set(BOOKS.PRICE,          dto.price())
                .set(BOOKS.STOCK_QUANTITY, dto.stockQuantity())
                .set(BOOKS.AUTHOR_ID,      dto.authorId())
                .where(BOOKS.ID.eq(id))
                .execute();

        if (updated == 0) throw new ResourceNotFoundException("Книга", id);
        return findById(id).orElseThrow();
    }

    public boolean deleteById(Long id) {
        return dsl.deleteFrom(BOOKS).where(BOOKS.ID.eq(id)).execute() > 0;
    }


    public boolean decrementStock(Long bookId, int quantity) {
        int updated = dsl.update(BOOKS)
                .set(BOOKS.STOCK_QUANTITY, BOOKS.STOCK_QUANTITY.minus(quantity))
                .where(BOOKS.ID.eq(bookId))
                .and(BOOKS.STOCK_QUANTITY.ge(quantity))
                .execute();
        return updated > 0;
    }

    public Optional<Integer> getStockQuantity(Long bookId) {
        return dsl.select(BOOKS.STOCK_QUANTITY)
                .from(BOOKS)
                .where(BOOKS.ID.eq(bookId))
                .fetchOptional(BOOKS.STOCK_QUANTITY);
    }
}
