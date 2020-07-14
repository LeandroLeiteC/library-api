package com.leandro.api.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.leandro.api.dto.LoanDTO;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDate;
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

        LoanDTO dto = LoanDTO.builder().isbn("123").customer("Fulano").build();
        Book book = Book.builder().isbn("123").build();
        Loan loan = Loan.builder().id(1L).customer("Fulano").book(book).loanDate(LocalDate.now()).build();

        String json = new ObjectMapper().writeValueAsString(dto);

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
                .andExpect(jsonPath("customer").value("Fulano"));
    }

    @Test
    @DisplayName("Deve retornar erro ao tentar fazer empréstimo de livro inexistente")
    void invalidIsbnCreateLoanTest() throws Exception {
        LoanDTO dto = LoanDTO.builder().isbn("123").customer("Fulano").build();
        String json = new ObjectMapper().writeValueAsString(dto);

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
        LoanDTO dto = LoanDTO.builder().isbn("123").customer("Fulano").build();
        String json = new ObjectMapper().writeValueAsString(dto);
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
}
