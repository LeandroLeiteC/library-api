package com.leandro.service;

import com.leandro.model.entity.Book;
import com.leandro.model.entity.Loan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface BookService {

    Book save(Book book);

    Optional<Book> getById(Long id);

    void delete(Book book);

    Book update(Book book);

    Page<Book> find(Book filter, Pageable pageRequest);

    Optional<Book> findByIsbn(String isbn);
}
