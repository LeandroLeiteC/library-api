package com.leandro.api.resource;

import com.leandro.api.dto.LoanDTO;
import com.leandro.model.entity.Book;
import com.leandro.model.entity.Loan;
import com.leandro.service.BookService;
import com.leandro.service.LoanService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/loans")
public class LoanController {

    private BookService bookService;
    private LoanService loanService;
    private ModelMapper mapper;

    @Autowired
    public LoanController(BookService bookService, LoanService loanService, ModelMapper mapper) {
        this.bookService = bookService;
        this.loanService = loanService;
        this.mapper = mapper;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LoanDTO create(@RequestBody LoanDTO dto) {
        Book book = bookService.findByIsbn(dto.getIsbn()).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Book not found for passed isbn"));
        Loan loan = loanService.save(Loan.builder().book(book).customer(dto.getCustomer()).loanDate(LocalDate.now()).build());
        return mapper.map(loan, LoanDTO.class);
    }
}
