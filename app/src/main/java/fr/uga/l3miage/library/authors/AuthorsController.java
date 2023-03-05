package fr.uga.l3miage.library.authors;

import fr.uga.l3miage.data.domain.Author;
import fr.uga.l3miage.library.books.BookDTO;
import fr.uga.l3miage.library.books.BooksMapper;
import fr.uga.l3miage.library.service.AuthorService;
import fr.uga.l3miage.library.service.DeleteAuthorException;
import fr.uga.l3miage.library.service.EntityNotFoundException;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

@RestController
@RequestMapping(value = "/api/v1", produces = "application/json")
public class AuthorsController {

    private final AuthorService authorService;
    private final AuthorMapper authorMapper;
    private final BooksMapper booksMapper;

    @Autowired
    public AuthorsController(AuthorService authorService, AuthorMapper authorMapper, BooksMapper booksMapper) {
        this.authorService = authorService;
        this.authorMapper = authorMapper;
        this.booksMapper = booksMapper;
    }

    @GetMapping("/authors")
    public Collection<AuthorDTO> authors(@RequestParam(value = "q", required = false) String query) {
        Collection<Author> authors;
        if (query == null) {
            authors = authorService.list();
        } else {
            authors = authorService.searchByName(query);
        }
        return authors.stream()
                .map(authorMapper::entityToDTO)
                .toList();
    }

    @GetMapping("/author/{id}")
    public AuthorDTO author(@PathVariable("id") Long id) throws EntityNotFoundException {
        var author = this.authorService.get(id);
        if (author == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return authorMapper.entityToDTO(author);
    }

    @PostMapping("/newAuthor")
    public AuthorDTO newAuthor(@RequestBody AuthorDTO author) {
        Author newAuthor = authorMapper.dtoToEntity(author);
        newAuthor.setFullName(newAuthor.getFullName());
        authorService.save(newAuthor);
        return authorMapper.entityToDTO(newAuthor);
    }

    @PutMapping("/upAuthor/{id}")
    public AuthorDTO updateAuthor(@RequestBody AuthorDTO author, @PathVariable("id") Long id)
            throws EntityNotFoundException {

        if (author.id() == id) {
            var existingAuthor = authorService.get(id);
            existingAuthor.setFullName(author.fullName());
            authorService.update(existingAuthor);
            return authorMapper.entityToDTO(existingAuthor);
        } else {
            throw new EntityNotFoundException("Author with id " + id + " not found.");
        }
    }

    @DeleteMapping("/delAuthor/{id}")
    public void deleteAuthor(@PathVariable("id") Long id, HttpServletResponse response)
            throws EntityNotFoundException, DeleteAuthorException, IOException {
        var deletedAuthor = authorService.get(id);
        if (deletedAuthor == null) {
            throw new EntityNotFoundException("Author with id " + id + " not found.");
        } else {
            authorService.delete(id);
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write("Author with id " + id + " deleted successfully");
        }
    }

    public Collection<BookDTO> books(Long authorId) {
        return Collections.emptyList();
    }

}
