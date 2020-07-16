package com.leandro.api.resource;

import com.leandro.api.dto.LoanDTO;
import com.leandro.api.dto.LoanFilterDTO;
import com.leandro.api.dto.ReturnedLoanDTO;
import com.leandro.api.exceptions.BussinesException;
import com.leandro.model.entity.Book;
import com.leandro.model.entity.Loan;
import com.leandro.service.BookService;
import com.leandro.service.LoanService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
        Book book = bookService.findByIsbn(dto.getBook().getIsbn()).orElseThrow(() -> new BussinesException("Book not found for passed isbn"));
        Loan loan = loanService.save(Loan.builder().book(book).customer(dto.getCustomer()).loanDate(LocalDate.now()).build());
        return mapper.map(loan, LoanDTO.class);
    }

    @PatchMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void returnBook(@PathVariable Long id, @RequestBody ReturnedLoanDTO dto) {
        Loan loan = loanService.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Loan not found"));
        loan.setReturned(true);
        loanService.update(loan);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Page<LoanDTO> findByFilter(LoanFilterDTO dto, Pageable pageable) {
        Page<Loan> result = loanService.find(dto, pageable);
        return result.map( loan -> mapper.map(loan, LoanDTO.class));
    }

}
