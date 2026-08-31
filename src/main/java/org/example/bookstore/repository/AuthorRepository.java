package org.example.bookstore.repository;

import org.example.bookstore.dto.AuthorDto;
import org.example.bookstore.exception.ResourceNotFoundException;
import org.example.bookstore.jooq.tables.Authors;
import org.example.bookstore.jooq.tables.records.AuthorsRecord;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public class AuthorRepository {

    private static final Authors AUTHORS = Authors.AUTHORS;

    private final DSLContext dsl;

    public AuthorRepository(DSLContext dsl) {
        this.dsl = dsl;
    }


    public AuthorDto create(AuthorDto dto) {
        AuthorsRecord record = dsl.insertInto(AUTHORS)
                .set(AUTHORS.NAME,       dto.name())
                .set(AUTHORS.BIO,        dto.bio())
                .set(AUTHORS.BIRTH_DATE, dto.birthDate())
                .returning()
                .fetchOne();

        if (record == null) throw new RuntimeException("Ошибка при создании автора");
        return toDto(record);
    }

    public Optional<AuthorDto> findById(Long id) {
        return dsl.selectFrom(AUTHORS)
                .where(AUTHORS.ID.eq(id))
                .fetchOptional()
                .map(this::toDto);
    }

    public List<AuthorDto> findAll() {
        return dsl.selectFrom(AUTHORS)
                .orderBy(AUTHORS.NAME.asc())
                .fetch()
                .map(this::toDto);
    }

    public AuthorDto update(Long id, AuthorDto dto) {
        AuthorsRecord record = dsl.update(AUTHORS)
                .set(AUTHORS.NAME,       dto.name())
                .set(AUTHORS.BIO,        dto.bio())
                .set(AUTHORS.BIRTH_DATE, dto.birthDate())
                .where(AUTHORS.ID.eq(id))
                .returning()
                .fetchOne();

        if (record == null) throw new ResourceNotFoundException("Автор", id);
        return toDto(record);
    }

    public boolean deleteById(Long id) {
        return dsl.deleteFrom(AUTHORS).where(AUTHORS.ID.eq(id)).execute() > 0;
    }

    private AuthorDto toDto(AuthorsRecord r) {
        return new AuthorDto(
                r.getId(),
                r.getName(),
                r.getBio(),
                r.getBirthDate(),
                r.getCreatedAt()
        );
    }
}
