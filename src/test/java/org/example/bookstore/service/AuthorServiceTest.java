package org.example.bookstore.service;

import org.example.bookstore.dto.AuthorDto;
import org.example.bookstore.exception.ResourceNotFoundException;
import org.example.bookstore.repository.AuthorRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.WARN)
public class AuthorServiceTest {

    @Mock
    AuthorRepository repository;

    @InjectMocks
    AuthorService service;

    static List<AuthorDto> rep = new ArrayList<>();

    @BeforeEach
    void setUp() {
        when(repository.findAll()).thenAnswer(
                inv ->
                        rep.stream().filter(item -> item.id() != -1).toList()
        );
        when(repository.create(any(AuthorDto.class))).thenAnswer(
                invocation -> {
                    AuthorDto author = invocation.getArgument(0);
                    long id = rep.size();
                    AuthorDto authorWithId = new AuthorDto(
                            id, author.name(), author.bio(),
                            author.birthDate(), author.createdAt()
                    );
                    rep.add(authorWithId);
                    return authorWithId;
                }
        );
        when(repository.deleteById(any(long.class))).thenAnswer(invocation -> {
            int id = Math.toIntExact(invocation.getArgument(0));
            if (rep.size() <= id) return false;
            AuthorDto author = rep.get(id);
            if (author.id() < 0) return false;
            rep.set(id, new AuthorDto(-1L, "", "", LocalDate.now(), LocalDateTime.now()));
            return true;
        });

        when(repository.findById(any(long.class))).thenAnswer(invocation -> {
            int id = Math.toIntExact(invocation.getArgument(0));
            if (rep.size() <= id) return Optional.empty();
            AuthorDto author = rep.get(id);
            if (author.id() == id) return Optional.of(author);
            return Optional.empty();
        });

        when(repository.update(any(long.class), any(AuthorDto.class))).thenAnswer(invocation -> {
            long id = invocation.getArgument(0);
            if (id > rep.size()) throw new ResourceNotFoundException("Автор", id);
            AuthorDto authorWithoutId = invocation.getArgument(1);
            AuthorDto author = new AuthorDto(id, authorWithoutId.name(), authorWithoutId.bio(),
                    authorWithoutId.birthDate(), authorWithoutId.createdAt()
            );
            int intId = Math.toIntExact(id);
            AuthorDto prev = rep.set(intId, author);
            if (prev.id() == id) return author;
            else throw new ResourceNotFoundException("Автор", id);
        });
    }

    @AfterEach
    void tearDown() {
        rep.clear();
    }

    @Test
    void testGetAllAuthors_shouldReturnAllNonDeletedAuthors() {
        AuthorDto author1 = new AuthorDto(null, "Author1", "Bio1", LocalDate.of(2000, 1, 1), LocalDateTime.now());
        AuthorDto author2 = new AuthorDto(null, "Author2", "Bio2", LocalDate.of(2001, 2, 2), LocalDateTime.now());
        service.createAuthor(author1);
        service.createAuthor(author2);

        List<AuthorDto> result = service.getAllAuthors();

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(a -> "Author1".equals(a.name())));
        assertTrue(result.stream().anyMatch(a -> "Author2".equals(a.name())));
    }

    @Test
    void testGetAuthorById_shouldReturnAuthor_whenExists() {
        AuthorDto created = service.createAuthor(
                new AuthorDto(null, "Test Author", "Bio", LocalDate.of(1990, 5, 15), LocalDateTime.now())
        );
        Long id = created.id();

        AuthorDto found = service.getAuthorById(id);

        assertNotNull(found);
        assertEquals(id, found.id());
        assertEquals("Test Author", found.name());
        assertEquals("Bio", found.bio());
    }

    @Test
    void testGetAuthorById_shouldThrowResourceNotFoundException_whenNotFound() {
        Long nonExistentId = 999L;

        assertThrows(ResourceNotFoundException.class, () -> service.getAuthorById(nonExistentId));
    }

    @Test
    void testCreateAuthor_shouldSaveAndReturnWithId() {
        AuthorDto dto = new AuthorDto(null, "New Author", "Bio", LocalDate.of(1985, 3, 10), LocalDateTime.now());

        AuthorDto created = service.createAuthor(dto);

        assertNotNull(created);
        assertNotNull(created.id());
        assertEquals("New Author", created.name());
        assertEquals("Bio", created.bio());
    }

    @Test
    void testUpdateAuthor_shouldUpdateAndReturnOldAuthor_whenExists() {
        AuthorDto original = service.createAuthor(
                new AuthorDto(null, "Old Name", "Old Bio", LocalDate.of(1990, 1, 1), LocalDateTime.now())
        );
        Long id = original.id();
        AuthorDto updateDto = new AuthorDto(null, "New Name", "New Bio", LocalDate.of(2000, 2, 2), LocalDateTime.now());

        AuthorDto updated = service.updateAuthor(id, updateDto);

        assertNotNull(updated);
        assertEquals(id, updated.id());

        assertEquals("New Name", updated.name());
        assertEquals("New Bio", updated.bio());
    }

    @Test
    void testUpdateAuthor_shouldThrowResourceNotFoundException_whenAuthorNotFound() {
        Long nonExistentId = 999L;
        AuthorDto updateDto = new AuthorDto(null, "Any", "Any", LocalDate.now(), LocalDateTime.now());

        assertThrows(ResourceNotFoundException.class, () -> service.updateAuthor(nonExistentId, updateDto));
    }

    @Test
    void testDeleteAuthor_shouldDeleteSuccessfully_whenExists() {
        AuthorDto created = service.createAuthor(
                new AuthorDto(null, "To Delete", "Bio", LocalDate.now(), LocalDateTime.now())
        );
        Long id = created.id();

        service.deleteAuthor(id);

        assertThrows(ResourceNotFoundException.class, () -> service.getAuthorById(id));
        List<AuthorDto> all = service.getAllAuthors();
        assertTrue(all.stream().noneMatch(a -> a.id().equals(id)));
    }

    @Test
    void testDeleteAuthor_shouldThrowResourceNotFoundException_whenNotFound() {
        Long nonExistentId = 999L;

        assertThrows(ResourceNotFoundException.class, () -> service.deleteAuthor(nonExistentId));
    }
}
