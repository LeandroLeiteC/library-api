package com.leandro.api.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.leandro.api.dto.BookDTO;
import com.leandro.api.dto.LoanDTO;
import com.leandro.api.dto.LoanFilterDTO;
import com.leandro.api.dto.ReturnedLoanDTO;
import com.leandro.api.exceptions.BussinesException;
import com.leandro.model.entity.Book;
import com.leandro.model.entity.Loan;
import com.leandro.service.BookService;
import com.leandro.service.LoanService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@WebMvcTest(controllers = LoanController.class)
@AutoConfigureMockMvc
class LoanControllerTest {

    private final static String LOAN_API = "/api/loans";

    @Autowired
    MockMvc mvc;

    @MockBean
    BookService bookService;

    @MockBean
    LoanService loanService;

    @Test
    @DisplayName("Deve realizar um empréstimo")
    void createLoanTest() throws Exception {

        BookDTO bookDTO = BookDTO.builder().id(2L).isbn("123").author("Author").title("Title").build();
        LoanDTO loanDTO = LoanDTO.builder().book(bookDTO).customer("Fulano").customerEmail("customer@email.com").build();

        Book book = Book.builder().id(2L).isbn("123").author("Author").title("Title").build();
        Loan loan = Loan.builder().id(1L).book(book).loanDate(LocalDate.now()).customer("Fulano").customerEmail("customer@email.com").build();

        String json = new ObjectMapper().writeValueAsString(loanDTO);

        BDDMockito.given(bookService.findByIsbn("123")).willReturn(Optional.of(book));
        BDDMockito.given(loanService.save(Mockito.any(Loan.class))).willReturn(loan);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(LOAN_API)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);

        mvc.perform(request)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("id").value(1L))
                .andExpect(jsonPath("customer").value("Fulano"))
                .andExpect(jsonPath("customerEmail").value("customer@email.com"))
                .andExpect(jsonPath("book.id").value(2L))
                .andExpect(jsonPath("book.isbn").value("123"))
                .andExpect(jsonPath("book.title").value("Title"))
                .andExpect(jsonPath("book.author").value("Author"));
    }

    @Test
    @DisplayName("Deve retornar erro ao tentar fazer empréstimo de livro inexistente")
    void invalidIsbnCreateLoanTest() throws Exception {
        BookDTO bookDTO = BookDTO.builder().id(2L).isbn("123").author("Author").title("Title").build();
        LoanDTO loanDTO = LoanDTO.builder().book(bookDTO).customer("Fulano").customerEmail("customer@email.com").build();
        String json = new ObjectMapper().writeValueAsString(loanDTO);

        BDDMockito.given(bookService.findByIsbn("123")).willReturn(Optional.empty());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(LOAN_API)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);

        mvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors", hasSize(1)))
                .andExpect(jsonPath("errors[0]").value("Book not found for passed isbn"));

        Mockito.verify(loanService, Mockito.never()).save(Mockito.any(Loan.class));
    }

    @Test
    @DisplayName("Deve retornar erro ao tentar fazer empréstimo de livro emprestado")
    void loanedBookErrorOnCreateLoanTest() throws Exception {
        BookDTO bookDTO = BookDTO.builder().id(2L).isbn("123").author("Author").title("Title").build();
        LoanDTO loanDTO = LoanDTO.builder().book(bookDTO).customer("Fulano").customerEmail("customer@email.com").build();
        String json = new ObjectMapper().writeValueAsString(loanDTO);
        Book book = Book.builder().isbn("123").build();

        BDDMockito.given(bookService.findByIsbn("123")).willReturn(Optional.of(book));
        BDDMockito.given(loanService.save(Mockito.any(Loan.class)) ).willThrow(new BussinesException("Book already loaned"));

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(LOAN_API)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);

        mvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors", hasSize(1)))
                .andExpect(jsonPath("errors[0]").value("Book already loaned"));
    }

    @Test
    @DisplayName("Deve retornar um livro")
    void returnBookTest() throws Exception {
        ReturnedLoanDTO dto = ReturnedLoanDTO.builder().build();
        String json = new ObjectMapper().writeValueAsString(dto);

        BDDMockito.given(loanService.findById(Mockito.anyLong())).willReturn(Optional.of(Loan.builder().id(1L).build()));
        BDDMockito.given(loanService.update(Mockito.any(Loan.class))).willReturn(Mockito.any(Loan.class));

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .patch(LOAN_API.concat("/1"))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);


        mvc.perform(request)
                .andExpect(status().isNoContent());

        Mockito.verify(loanService, Mockito.times(1)).update(Mockito.any(Loan.class));
    }

    @Test
    @DisplayName("Deve retornar erro ao tentar retornar empréstimo inexistente")
    void returnInexistenceLoan() throws Exception {

        String json = new ObjectMapper().writeValueAsString(Loan.builder().build());

        BDDMockito.given(loanService.findById(Mockito.anyLong())).willReturn(Optional.empty());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .patch(LOAN_API.concat("/1"))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        mvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("errors", hasSize(1)))
                .andExpect(jsonPath("errors[0]").value("Loan not found"));

        Mockito.verify(loanService, Mockito.never()).update(Mockito.any(Loan.class));
    }

    @Test
    @DisplayName("Deve filtrar empréstimos")
    void findLoanTest() throws Exception {
        // cenário
        Long id = 1L;

        Loan loan = Loan.builder()
                .id(id)
                .book(Book.builder().id(1L).isbn("123").build())
                .customer("Customer")
                .loanDate(LocalDate.now())
                .build();

        BDDMockito.given(loanService.find(Mockito.any(LoanFilterDTO.class), Mockito.any(Pageable.class)))
                .willReturn(new PageImpl<Loan>(Arrays.asList(loan), PageRequest.of(0, 100), 1));

        String queryString = String.format("?customer=%s&isbn=%s&page=0&size=100",
                loan.getCustomer(), loan.getBook().getIsbn());

        // execução
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get(LOAN_API.concat(queryString))
                .accept(MediaType.APPLICATION_JSON);

        mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("content", hasSize(1)))
                .andExpect(jsonPath("totalElements").value(1))
                .andExpect(jsonPath("pageable.pageSize").value(100))
                .andExpect(jsonPath("pageable.pageNumber").value(0));
    }
}
