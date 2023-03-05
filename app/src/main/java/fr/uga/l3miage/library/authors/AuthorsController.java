package fr.uga.l3miage.library.authors;

import fr.uga.l3miage.data.domain.Author;
import fr.uga.l3miage.data.domain.Book;
import fr.uga.l3miage.library.books.BookDTO;
import fr.uga.l3miage.library.books.BooksMapper;
import fr.uga.l3miage.library.service.AuthorService;
import fr.uga.l3miage.library.service.DeleteAuthorException;
import fr.uga.l3miage.library.service.EntityNotFoundException;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;

import java.util.Collection;

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

    @GetMapping("/authors/{id}")
    @ResponseStatus(HttpStatus.OK)
    public AuthorDTO author(@PathVariable("id") Long id) {

        try {
            Author author;
            author = this.authorService.get(id);
            return this.authorMapper.entityToDTO(author);
        } catch (EntityNotFoundException e) {
            // TODO Auto-generated catch block
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

    }

    @PostMapping("/authors")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthorDTO newAuthor(@RequestBody @Valid AuthorDTO author, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    bindingResult.getAllErrors().get(0).getDefaultMessage());

        }
        Author new_author = this.authorMapper.dtoToEntity(author);
        new_author = this.authorService.save(new_author);
        return this.authorMapper.entityToDTO(new_author);

    }

    @PutMapping("/authors/{id}")
    @ResponseStatus(HttpStatus.OK)
    public AuthorDTO updateAuthor(@RequestBody AuthorDTO author, @PathVariable("id") Long id)
            throws EntityNotFoundException {
        // attention AuthorDTO.id() doit être égale à id, sinon la requête utilisateur
        // est mauvaise
        if (id != author.id()) {

            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        Author new_author = this.authorService.get(id);
        if (new_author == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        Author author_updated = this.authorService.update(this.authorMapper.dtoToEntity(author));

        return this.authorMapper.entityToDTO(author_updated);
    }

    @DeleteMapping("/authors/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAuthor(@PathVariable("id") @Valid Long id)
            throws DeleteAuthorException {

        try {
            this.authorService.delete(id);
        } catch (EntityNotFoundException e) {
            // TODO Auto-generated catch block
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/authors/{id}/books")
    public Collection<BookDTO> books(@PathVariable("id") Long authorId) {
        Author author;
        try {
            author = this.authorService.get(authorId);
            Collection<Book> books = author.getBooks();
            return books.stream()
                    .map(booksMapper::entityToDTO)
                    .toList();
        } catch (EntityNotFoundException e) {
            // TODO Auto-generated catch block
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        
    }

    

    

}
