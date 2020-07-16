package com.leandro;

import com.leandro.api.resource.BookController;
import com.leandro.api.resource.LoanController;
import com.leandro.model.repository.BookRepository;
import com.leandro.model.repository.LoanRepository;
import com.leandro.service.BookService;
import com.leandro.service.LoanService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class LibraryApiApplicationTests {

	@Autowired
	ModelMapper mapper;

	@Autowired
	BookService bookService;

	@Autowired
	LoanService loanService;

	@Autowired
	BookController bookController;

	@Autowired
	LoanController loanController;

	@Autowired
	BookRepository bookRepository;

	@Autowired
	LoanRepository loanRepository;

	@Test
	void contextLoads() {
		assertThat(mapper).isNotNull();
		assertThat(bookService).isNotNull();
		assertThat(loanService).isNotNull();
		assertThat(bookController).isNotNull();
		assertThat(loanController).isNotNull();
		assertThat(bookRepository).isNotNull();
		assertThat(loanRepository).isNotNull();
	}

	@Test
	void mainTest() {
		LibraryApiApplication.main(new String[] {});
	}

}
