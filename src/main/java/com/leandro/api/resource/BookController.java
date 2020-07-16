package com.leandro.api.resource;

import com.leandro.api.dto.BookDTO;
import com.leandro.api.dto.LoanDTO;
import com.leandro.model.entity.Book;
import com.leandro.service.BookService;
import com.leandro.service.LoanService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private BookService service;
    private ModelMapper mapper;
    private LoanService loanService;

    @Autowired
    public BookController(BookService service, ModelMapper mapper, LoanService loanService) {
        this.service = service;
        this.mapper = mapper;
        this.loanService = loanService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookDTO create(@RequestBody @Valid BookDTO bookDTO) {
        Book entity = mapper.map(bookDTO, Book.class);
        entity = service.save(entity);
        return mapper.map(entity, BookDTO.class);
    }

    @GetMapping("{id}")
    public BookDTO getDetails(@PathVariable("id") Long id) {
        Book book = service.getById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        return mapper.map(book, BookDTO.class);
    }

    @GetMapping
    public Page<BookDTO> find(BookDTO dto, Pageable pageable) {
        Page<Book> result = service.find(mapper.map(dto, Book.class), pageable);
        List<BookDTO> content = result.getContent()
                .stream()
                .map(book -> mapper.map(book, BookDTO.class))
                .collect(Collectors.toList());
        return new PageImpl<>(content, result.getPageable(), result.getTotalElements());
    }

    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") Long id) {
        service.delete(service.getById(id).orElseThrow(NoSuchElementException::new));
    }

    @PutMapping("{id}")
    @ResponseStatus(HttpStatus.OK)
    public BookDTO update(@RequestBody BookDTO bookDTO, @PathVariable("id") Long id) {
        Book book = service.getById(id).orElseThrow(NoSuchElementException::new);

        book.setAuthor(bookDTO.getAuthor());
        book.setTitle(bookDTO.getTitle());

        book = service.update(book);

        return mapper.map(book, BookDTO.class);
    }

    @GetMapping("{id}/loans")
    public Page<LoanDTO> loansByBook(@PathVariable Long id, Pageable pageable) {
        Book book = service.getById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found"));
        return loanService.findLoansByBook(book, pageable).map(loan -> mapper.map(loan, LoanDTO.class));
    }
}
