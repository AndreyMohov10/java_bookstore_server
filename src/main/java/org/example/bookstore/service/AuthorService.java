package org.example.bookstore.service;

import org.example.bookstore.dto.AuthorDto;
import org.example.bookstore.exception.ResourceNotFoundException;
import org.example.bookstore.repository.AuthorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class AuthorService {

    private final AuthorRepository authorRepository;

    public AuthorService(AuthorRepository authorRepository) {
        this.authorRepository = authorRepository;
    }

    public List<AuthorDto> getAllAuthors() {
        return authorRepository.findAll();
    }

    public AuthorDto getAuthorById(Long id) {
        return authorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Автор", id));
    }

    @Transactional
    public AuthorDto createAuthor(AuthorDto dto) {
        return authorRepository.create(dto);
    }

    @Transactional
    public AuthorDto updateAuthor(Long id, AuthorDto dto) {
        return authorRepository.update(id, dto);
    }

    @Transactional
    public void deleteAuthor(Long id) {
        if (!authorRepository.deleteById(id)) {
            throw new ResourceNotFoundException("Автор", id);
        }
    }
}
