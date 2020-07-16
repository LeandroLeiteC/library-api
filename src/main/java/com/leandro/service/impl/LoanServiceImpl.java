package com.leandro.service.impl;

import com.leandro.api.dto.LoanFilterDTO;
import com.leandro.api.exceptions.BussinesException;
import com.leandro.model.entity.Book;
import com.leandro.model.entity.Loan;
import com.leandro.model.repository.LoanRepository;
import com.leandro.service.LoanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class LoanServiceImpl implements LoanService {

    private LoanRepository repository;

    @Autowired
    public LoanServiceImpl(LoanRepository repository) {
        this.repository = repository;
    }

    @Override
    public Loan save(Loan loan) {
        if (repository.existsByBookAndNotReturned(loan.getBook())) {
            throw new BussinesException("Book already loaned");
        }
        return repository.save(loan);
    }

    @Override
    public Optional<Loan> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public Loan update(Loan loan) {
        return repository.save(loan);
    }

    @Override
    public Page<Loan> find(LoanFilterDTO filter, Pageable pageable) {
        Loan loan = Loan.builder()
                .book(Book.builder().isbn(filter.getIsbn()).build())
                .customer(filter.getCustomer()).build();

        ExampleMatcher matching = ExampleMatcher.matching()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING)
                .withIgnoreNullValues()
                .withIgnoreCase();

        return repository.findAll(Example.of(loan, matching), pageable);
    }

    @Override
    public Page<Loan> findLoansByBook(Book book, Pageable pageable) {
        return repository.findByBook(book, pageable);
    }
}
