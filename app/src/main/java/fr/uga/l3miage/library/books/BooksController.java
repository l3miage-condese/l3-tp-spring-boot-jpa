package fr.uga.l3miage.library.books;

import fr.uga.l3miage.data.domain.Author;
import fr.uga.l3miage.data.domain.Book;
import fr.uga.l3miage.library.authors.AuthorDTO;
import fr.uga.l3miage.library.authors.AuthorMapper;
import fr.uga.l3miage.library.service.AuthorService;
import fr.uga.l3miage.library.service.BookService;
import fr.uga.l3miage.library.service.EntityNotFoundException;
import io.micrometer.common.lang.Nullable;
import jakarta.validation.Valid;

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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collection;

@RestController
@RequestMapping(value = "api/v1", produces = "application/json")
public class BooksController {

    private final BookService bookService;
    private final BooksMapper booksMapper;
    private AuthorService authorService;
    private AuthorMapper authorMapper;

    @Autowired
    public BooksController(BookService bookService, BooksMapper booksMapper, AuthorService authorService,
            AuthorMapper authorMapper) {
        this.bookService = bookService;
        this.booksMapper = booksMapper;
        this.authorService = authorService;
        this.authorMapper = authorMapper;
    }

    @GetMapping("/books")
    public Collection<BookDTO> books(@RequestParam(value = "q", required = false) @Nullable String query) {
        Collection<Book> books;
        if (query == null) {
            books = bookService.list();
        } else {
            books = bookService.findByTitle(query);
        }
        return books.stream()
                .map(booksMapper::entityToDTO)
                .toList();

    }

    @GetMapping("books/{id}")
    public BookDTO book(@PathVariable("id") Long id) throws EntityNotFoundException {
        try {
            Book book = bookService.get(id);
            if (book == null || book.getId() == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND);
            }
            return booksMapper.entityToDTO(book);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);

        }

    }

    @PostMapping("authors/{id}/books")
    @ResponseStatus(HttpStatus.CREATED)
    public BookDTO newBook(@PathVariable("id") Long authorId, @RequestBody BookDTO book)
            throws EntityNotFoundException {
        try {

            this.authorService.get(authorId);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        try {
            if (book == null || book.title() == null || book.title() == "" ||
                    ((String.valueOf(book.isbn())).length() < 10 || (String.valueOf(book.isbn())).length() > 13)
                    || (String.valueOf(book.year())).length() != 4) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
            }
            Book book_new = booksMapper.dtoToEntity(book);
            Book book_author = bookService.save(authorId, book_new);
            BookDTO book_saved = booksMapper.entityToDTO(book_author);
            return book_saved;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("books/{id}")
    public BookDTO updateBook(@PathVariable("id") Long authorId, @RequestBody BookDTO book)
            throws EntityNotFoundException {
        try {

            Author author = authorService.get(authorId);
            Collection<Book> author_books = author.getBooks();
            for (Book book_put : author_books) {
                if (book_put.getId() == book.id()) {

                    Book book_add = booksMapper.dtoToEntity(book);
                    book_add.addAuthor(author);
                    Book book_Updated = bookService.update(book_add);
                    return booksMapper.entityToDTO(book_Updated);
                }

            }

        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        throw new ResponseStatusException(HttpStatus.BAD_REQUEST);

    }

    @DeleteMapping("books/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBook(@PathVariable("id") Long id) throws EntityNotFoundException {
        try {
            Book book = bookService.get(id);
            if (book == null) {

                throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
            }
            this.bookService.delete(id);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NO_CONTENT);
        }

    }

    @PutMapping("books/{id}/authors")
    public BookDTO addAuthor(@PathVariable("id") Long bookId, @RequestBody @Valid AuthorDTO author) {
        try {
            Book book = bookService.addAuthor(bookId, author.id());
            return booksMapper.entityToDTO(book);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }
}
