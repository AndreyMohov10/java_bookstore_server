package org.example.bookstore.repository;

import org.example.bookstore.dto.AuthorDto;
import org.example.bookstore.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import io.zonky.test.db.AutoConfigureEmbeddedDatabase;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@AutoConfigureEmbeddedDatabase(type = AutoConfigureEmbeddedDatabase.DatabaseType.POSTGRES)
@Transactional
@ActiveProfiles("test")
class AuthorRepositoryTest {

    @Autowired
    private AuthorRepository authorRepository;

    private AuthorDto createTestAuthor(String name, String bio) {
        return new AuthorDto(null, name, bio, LocalDate.of(1990, 1, 1), null);
    }

    @Test
    void createShouldSaveAuthorAndReturnDtoWithId() {
        AuthorDto dto = createTestAuthor("Лев Толстой", "Великий русский писатель");

        AuthorDto saved = authorRepository.create(dto);

        assertThat(saved.id()).isNotNull();
        assertThat(saved.name()).isEqualTo("Лев Толстой");
        assertThat(saved.bio()).isEqualTo("Великий русский писатель");
        assertThat(saved.birthDate()).isEqualTo(LocalDate.of(1990, 1, 1));
        assertThat(saved.createdAt()).isNotNull();
    }

    @Test
    void findByIdShouldReturnExistingAuthor() {
        AuthorDto saved = authorRepository.create(createTestAuthor("Фёдор Достоевский", "Писатель"));

        Optional<AuthorDto> found = authorRepository.findById(saved.id());

        assertThat(found).isPresent();
        assertThat(found.get().name()).isEqualTo("Фёдор Достоевский");
    }

    @Test
    void findByIdShouldReturnEmptyForNonExisting() {
        Optional<AuthorDto> found = authorRepository.findById(999L);

        assertThat(found).isEmpty();
    }

    @Test
    void findAllShouldReturnAllAuthorsSortedByName() {
        authorRepository.create(createTestAuthor("Иван Тургенев", ""));
        authorRepository.create(createTestAuthor("Александр Пушкин", ""));

        List<AuthorDto> authors = authorRepository.findAll();

        assertThat(authors).hasSize(2);
        assertThat(authors).extracting(AuthorDto::name)
                .containsExactly("Александр Пушкин", "Иван Тургенев");
    }

    @Test
    void updateShouldModifyExistingAuthor() {
        AuthorDto saved = authorRepository.create(createTestAuthor("Михаил Лермонтов", "Поэт"));
        AuthorDto updateDto = new AuthorDto(
                saved.id(),
                "Михаил Юрьевич Лермонтов",
                "Великий поэт и прозаик",
                LocalDate.of(1814, 10, 15),
                null
        );

        AuthorDto updated = authorRepository.update(saved.id(), updateDto);

        assertThat(updated.name()).isEqualTo("Михаил Юрьевич Лермонтов");
        assertThat(updated.bio()).isEqualTo("Великий поэт и прозаик");
        assertThat(updated.birthDate()).isEqualTo(LocalDate.of(1814, 10, 15));
        assertThat(updated.createdAt()).isEqualTo(saved.createdAt());
    }

    @Test
    void updateShouldThrowResourceNotFoundExceptionWhenAuthorNotFound() {
        AuthorDto updateDto = new AuthorDto(null, "Имя", "", null, null);

        assertThatThrownBy(() -> authorRepository.update(999L, updateDto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Автор");
    }

    @Test
    void deleteByIdShouldRemoveAuthor() {
        AuthorDto saved = authorRepository.create(createTestAuthor("Николай Гоголь", ""));

        boolean deleted = authorRepository.deleteById(saved.id());

        assertThat(deleted).isTrue();
        Optional<AuthorDto> found = authorRepository.findById(saved.id());
        assertThat(found).isEmpty();
    }

    @Test
    void deleteByIdShouldReturnFalseIfAuthorNotExists() {
        boolean deleted = authorRepository.deleteById(999L);

        assertThat(deleted).isFalse();
    }

    @Test
    void secondDeleteByIdShouldReturnFalse() {
        AuthorDto saved = authorRepository.create(createTestAuthor("Николай Гоголь", ""));

        authorRepository.deleteById(saved.id());
        boolean deleted = authorRepository.deleteById(saved.id());

        assertThat(deleted).isFalse();
    }
}