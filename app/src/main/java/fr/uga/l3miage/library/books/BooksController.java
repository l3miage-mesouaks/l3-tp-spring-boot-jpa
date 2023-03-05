package fr.uga.l3miage.library.books;

import fr.uga.l3miage.data.domain.Author;
import fr.uga.l3miage.data.domain.Book;
import fr.uga.l3miage.library.authors.AuthorDTO;
import fr.uga.l3miage.library.service.AuthorService;
import fr.uga.l3miage.library.service.BookService;
import fr.uga.l3miage.library.service.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collection;
import java.util.Set;

@RestController
@RequestMapping(value = "/api", produces = "application/json")
public class BooksController {

    private final BookService bookService;
    private final AuthorService authorService;
    private final BooksMapper booksMapper;

    @Autowired
    public BooksController(BookService bookService, BooksMapper booksMapper, AuthorService authorService) {
        this.bookService = bookService;
        this.booksMapper = booksMapper;
        this.authorService = authorService;
    }

    @GetMapping("/books")
    public Collection<BookDTO> books(@RequestParam("q") String query) {
        Collection<Book> books;
        if (query == null) {
            books = bookService.list();
        } else {
            books = bookService.findByTitle(query);
        }
        return booksMapper.entityToDTO(books);
    }

    @GetMapping("/book/{id}")
    public BookDTO book(@PathVariable("id") Long id) throws EntityNotFoundException {
        Book book = bookService.get(id);
        if (book == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return booksMapper.entityToDTO(book);
    }

    @PostMapping("/newBook")
    public BookDTO newBook(Long authorId, @RequestBody BookDTO book) throws EntityNotFoundException {

        Book newBook = booksMapper.dtoToEntity(book);
        Author author = authorService.get(authorId);
        newBook.addAuthor(author);
        newBook.setIsbn(newBook.getIsbn());
        newBook.setYear(newBook.getYear());
        newBook.setTitle(newBook.getTitle());
        newBook.setLanguage(newBook.getLanguage());
        newBook.setPublisher(newBook.getPublisher());

        return booksMapper.entityToDTO(newBook);

    }

    @PutMapping("/v1/books/{bookId}")
    public BookDTO updateBook(@PathVariable("bookId") Long bookId, @RequestBody BookDTO book)
            throws EntityNotFoundException {
        // attention BookDTO.id() doit être égale à id, sinon la requête utilisateur est
        // mauvaise
        if (book.id() == bookId) {
            var existingBook = bookService.get(bookId);
            existingBook.setYear(book.year());
            existingBook.setIsbn(book.isbn());
            existingBook.setTitle(book.title());
            existingBook.setId(book.id());
            existingBook.setPublisher(book.publisher());
            bookService.update(existingBook);
            return booksMapper.entityToDTO(existingBook);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }

    public void deleteBook(Long id) {

    }

    public void addAuthor(Long authorId, AuthorDTO author) {

    }
}
