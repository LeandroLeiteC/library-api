package com.leandro.api.resouce;

import com.leandro.api.dto.BookDTO;
import com.leandro.api.exceptions.ApiErrors;
import com.leandro.api.exceptions.BussinesException;
import com.leandro.model.entity.Book;
import com.leandro.service.BookService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private BookService service;
    private ModelMapper mapper;

    @Autowired
    public BookController(BookService service, ModelMapper mapper) {
        this.service = service;
        this.mapper = mapper;
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

    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") Long id) {
        service.delete(service.getById(id).orElseThrow(NoSuchElementException::new));
    }

    @PutMapping("{id}")
    @ResponseStatus(HttpStatus.OK)
    public BookDTO update(@RequestBody BookDTO bookDTO, @PathVariable("id") Long id) {
        Book book =  service.getById(id).orElseThrow(NoSuchElementException::new);

        book.setAuthor(bookDTO.getAuthor());
        book.setTitle(bookDTO.getTitle());

        book = service.update(book);

        return mapper.map(book, BookDTO.class);
    }

    // Exceptions Handlers
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrors handleValidationException(MethodArgumentNotValidException exception) {
        BindingResult bindingResult = exception.getBindingResult();
        return new ApiErrors(bindingResult);
    }

    @ExceptionHandler(BussinesException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrors handleBussinesException(BussinesException exception) {
        return new ApiErrors(exception);
    }

    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiErrors handleNotFoundException(NoSuchElementException exception) {
        return new ApiErrors(exception);
    }
}
