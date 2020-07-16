package com.leandro.service;

import com.leandro.api.dto.LoanFilterDTO;
import com.leandro.model.entity.Book;
import com.leandro.model.entity.Loan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface LoanService {

    Loan save(Loan loan);

    Optional<Loan> findById(Long id);

    Loan update(Loan loan);

    Page<Loan> find(LoanFilterDTO loan, Pageable pageable);

    Page<Loan> findLoansByBook(Book book, Pageable pageable);
}
