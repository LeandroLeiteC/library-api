package com.leandro.repository;

import com.leandro.api.dto.LoanFilterDTO;
import com.leandro.model.entity.Book;
import com.leandro.model.entity.Loan;
import com.leandro.model.repository.LoanRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@DataJpaTest
class LoanRepositoryTest {

    @Autowired
    TestEntityManager entityManager;

    @Autowired
    LoanRepository repository;

    @Test
    @DisplayName("Deve verificar se existe empréstimo não devolvido para o livro")
    void existsByBookAndNotReturnedTest() {

        Book book = Book.builder().isbn("123").author("Author").title("Title").build();
        entityManager.persist(book);

        Loan loan = Loan.builder().book(book).customer("Leandro").build();
        entityManager.persist(loan);

        boolean exists = repository.existsByBookAndNotReturned(loan.getBook());

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Deve achar todos os empréstimos pelo customer e isbn")
    void FindAllWithFilterTest() {
        Book book = Book.builder().isbn("123").build();

        entityManager.persist(book);

        Loan loan = Loan.builder().customer("Customer").book(book).build();
        Loan loan2 = Loan.builder().customer("Fulano").book(book).build();

        entityManager.persist(loan);
        entityManager.persist(loan2);

        LoanFilterDTO filter = LoanFilterDTO.builder().isbn("123").customer("Customer").build();
        Loan loanFilter = Loan.builder().book(Book.builder().isbn(filter.getIsbn()).build()).customer(filter.getCustomer()).build();

        ExampleMatcher matching = ExampleMatcher.matching()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING)
                .withIgnoreNullValues()
                .withIgnoreCase();

        Page<Loan> result = repository.findAll(Example.of(loan, matching), PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent()).contains(loan);
        assertThat(result.getPageable().getPageSize()).isEqualTo(10);
        assertThat(result.getPageable().getPageNumber()).isZero();
    }

    @Test
    @DisplayName("Deve retornar todos os loans de um book")
    void findByBookTest() {
        Book book = Book.builder().isbn("123").build();
        entityManager.persist(book);

        Loan loan = Loan.builder().book(book).build();
        entityManager.persist(loan);

        Page<Loan> result = repository.findByBook(book, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent()).contains(loan);
        assertThat(result.getPageable().getPageSize()).isEqualTo(10);
        assertThat(result.getPageable().getPageNumber()).isZero();
    }

    @Test
    @DisplayName("Deve encontrar todos os empréstimos atrasados")
    void findAllLateLoansTest() {
        Loan loanToBeFound = Loan.builder().returned(false).build();
        Loan loanToNotBeFound = Loan.builder().returned(false).build();

        entityManager.persist(loanToBeFound);
        loanToBeFound.setLoanDate(LocalDate.now().minusDays(10));
        entityManager.persist(loanToNotBeFound);

        List<Loan> lateLoans = repository.findAllByLoanDateBeforeAndReturnedIsFalse(LocalDate.now().minusDays(4));

        assertThat(lateLoans).hasSize(1).contains(loanToBeFound);
    }
}
