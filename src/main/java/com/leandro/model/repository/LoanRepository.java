package com.leandro.model.repository;

import com.leandro.model.entity.Book;
import com.leandro.model.entity.Loan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface LoanRepository extends JpaRepository<Loan, Long> {

    @Query(value = "select case when ( count(l.id) > 0 ) then true else false end from Loan l where l.book = :book AND (l.returned is null or l.returned is not true )")
    boolean existsByBookAndNotReturned(@Param("book") Book book);

    Page<Loan> findByBook(Book book, Pageable pageable);

    List<Loan> findAllByLoanDateBeforeAndReturnedIsFalse(LocalDate localDate);
}
