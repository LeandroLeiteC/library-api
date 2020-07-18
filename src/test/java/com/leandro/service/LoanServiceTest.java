package com.leandro.service;

import com.leandro.api.dto.LoanFilterDTO;
import com.leandro.api.exceptions.BussinesException;
import com.leandro.model.entity.Book;
import com.leandro.model.entity.Loan;
import com.leandro.model.repository.LoanRepository;
import com.leandro.service.impl.LoanServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
class LoanServiceTest {

    LoanService service;

    @MockBean
    LoanRepository repository;

    @BeforeEach
    private void setUp() {
        this.service = new LoanServiceImpl(repository);
    }

    @Test
    @DisplayName("Deve salvar um empréstimo")
    void saveTest() {
        Book book = Book.builder().id(1L).build();
        Loan loanToSave = Loan.builder()
                .customer("Leandro")
                .book(book)
                .build();

        Loan savedLoan = Loan.builder()
                .id(1L)
                .loanDate(LocalDate.now())
                .customer("Leandro")
                .book(book)
                .build();

        Mockito.when(repository.existsByBookAndNotReturned(book)).thenReturn(false);
        Mockito.when(repository.save(loanToSave)).thenReturn(savedLoan);

        Loan returnedLoan = service.save(loanToSave);

        assertThat(returnedLoan.getId()).isNotNull().isEqualTo(1L);
        assertThat(returnedLoan.getCustomer()).isEqualTo("Leandro");
        assertThat(returnedLoan.getLoanDate()).isEqualTo(LocalDate.now());
        assertThat(returnedLoan.getBook().getId()).isEqualTo(book.getId());

        Mockito.verify(repository, Mockito.times(1)).save(loanToSave);
    }

    @Test
    @DisplayName("Deve lançar erro de negócio ao salvar um empréstimo com o livro já emprestado")
    void loanedBookSaveTest() {
        Book book = Book.builder().id(1L).build();
        Loan loanToSave = Loan.builder()
                .book(book)
                .customer("Leandro")
                .build();

        Mockito.when(repository.existsByBookAndNotReturned(book)).thenReturn(true);

        Throwable exception = catchThrowable(() -> service.save(loanToSave));

        assertThat(exception)
                .isInstanceOf(BussinesException.class)
                .hasMessage("Book already loaned");

        Mockito.verify(repository, Mockito.never()).save(loanToSave);
    }

    @Test
    @DisplayName("Deve encontrar um empréstimo pelo id")
    void findByIdTest() {
        Book book = Book.builder().title("Title").author("Author").isbn("123").id(10L).build();
        Loan loan = Loan.builder().id(1L).book(book).customer("Customer").loanDate(LocalDate.now()).build();

        Mockito.when(repository.findById(Mockito.anyLong())).thenReturn(Optional.of(loan));

        Optional<Loan> savedLoan = service.findById(1L);

        assertThat(savedLoan).isPresent();
        assertThat(savedLoan.get().getId()).isEqualTo(1L);
        assertThat(savedLoan.get().getCustomer()).isEqualTo("Customer");
        assertThat(savedLoan.get().getLoanDate()).isEqualTo(LocalDate.now());
        assertThat(savedLoan.get().getBook().getId()).isEqualTo(10L);
        assertThat(savedLoan.get().getBook().getIsbn()).isEqualTo("123");
        assertThat(savedLoan.get().getBook().getAuthor()).isEqualTo("Author");
        assertThat(savedLoan.get().getBook().getTitle()).isEqualTo("Title");

        Mockito.verify(repository, Mockito.times(1)).findById(Mockito.anyLong());
    }

    @Test
    @DisplayName("Deve retornar um optional vazio ao procurar um empréstimo inexistente")
    void findByInexistenceLoan() {
        Mockito.when(repository.findById(Mockito.anyLong())).thenReturn(Optional.empty());

        Optional<Loan> loan = service.findById(1L);

        assertThat(loan).isNotPresent();

        Mockito.verify(repository, Mockito.times(1)).findById(Mockito.anyLong());
    }

    @Test
    @DisplayName("Deve atualizar um empréstimo")
    void updateTest() {


        Loan loan = Loan.builder().id(1L).returned(true).build();

        Mockito.when(repository.save(Mockito.any(Loan.class))).thenReturn(loan);

        Loan updatedLoan = service.update(loan);

        assertThat(updatedLoan.getId()).isEqualTo(loan.getId());
        assertThat(updatedLoan.getReturned()).isTrue();

        Mockito.verify(repository, Mockito.times(1)).save(Mockito.any(Loan.class));
    }

    @Test
    @DisplayName("Deve encontrar um empréstimo")
    void findTest() {
        Loan loan = Loan.builder().id(1L).build();
        List<Loan> list = Arrays.asList(loan);

        Page<Loan> page = new PageImpl(list, PageRequest.of(0, 10), 1);

        Mockito.when(repository.findAll(Mockito.any(Example.class), Mockito.any(PageRequest.class))).thenReturn(page);

        Page<Loan> result = service.find(LoanFilterDTO.builder().customer("Customer").isbn("123").build(), PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).isEqualTo(list);
        assertThat(result.getPageable().getPageNumber()).isZero();
        assertThat(result.getPageable().getPageSize()).isEqualTo(10);
    }

    @Test
    @DisplayName("Deve obter os empréstimos de um livro")
    void findLoansByBook() {
        Loan loan = Loan.builder().build();
        Mockito.when(repository.findByBook(Mockito.any(Book.class), Mockito.any(Pageable.class)))
                .thenReturn(new PageImpl<Loan>(Arrays.asList(loan), PageRequest.of(0, 100), 1));

        Page<Loan> loans = service.findLoansByBook(Book.builder().build(), PageRequest.of(0, 100));

        assertThat(loans.getContent()).hasSize(1);
        assertThat(loans.getPageable().getPageNumber()).isZero();
        assertThat(loans.getPageable().getPageSize()).isEqualTo(100);
        assertThat(loans.getTotalElements()).isOne();
    }

    @Test
    @DisplayName("Deve obter todos os empréstimos atrasados")
    void findAllLateLoansTest() {
        Loan loan = Loan.builder().id(1L).loanDate(LocalDate.now().minusDays(10)).build();

        Mockito.when(repository.findAllByLoanDateBeforeAndReturnedIsFalse(Mockito.any(LocalDate.class))).thenReturn(Arrays.asList(loan));

        List<Loan> lateLoans = service.getAllLateLoans();

        assertThat(lateLoans).hasSize(1).contains(loan);

        Mockito.verify(repository, Mockito.times(1)).findAllByLoanDateBeforeAndReturnedIsFalse(Mockito.any(LocalDate.class));
    }
}
