package com.leandro.service;

import com.leandro.api.exceptions.BussinesException;
import com.leandro.model.entity.Book;
import com.leandro.model.repository.BookRepository;
import com.leandro.service.impl.BookServiceImpl;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
class BookServiceTest {

    BookService service;

    @MockBean
    BookRepository repository;

    @BeforeEach
    void setUp() {
        this.service = new BookServiceImpl(repository);
    }

    private Book createValidBook() {
        return Book.builder().isbn("123").author("Autor").title("Titulo").build();
    }

    @Test
    @DisplayName("Deve salvar um livro")
    void saveBookTest() {
        // cenário
        Book book = createValidBook();
        Book repositoryBook = Book.builder().author("Autor").isbn("123").title("Titulo").id(1L).build();
        Mockito.when(repository.existsByIsbn(Mockito.anyString())).thenReturn(false);
        Mockito.when(repository.save(book)).thenReturn(repositoryBook);

        // execução
        Book savedBook = service.save(book);

        // verificação
        assertThat(savedBook.getId()).isNotNull();
        assertThat(savedBook.getAuthor()).isEqualTo("Autor");
        assertThat(savedBook.getIsbn()).isEqualTo("123");
        assertThat(savedBook.getTitle()).isEqualTo("Titulo");
    }


    @Test
    @DisplayName("Deve lançar erro de negócio ao tentar salvar um livro com isbn duplicado")
    void shouldNotSaveABookWithDuplicateIsbn() {
        // cenário
        Book book = createValidBook();
        Mockito.when(repository.existsByIsbn(Mockito.anyString())).thenReturn(true);

        // execução
        Throwable exception = Assertions.catchThrowable(() -> service.save(book));

        // verificação
        assertThat(exception)
                .isInstanceOf(BussinesException.class)
                .hasMessage("Isbn já cadastrado.");

        Mockito.verify(repository, Mockito.never()).save(book);
    }

    @Test
    @DisplayName("Deve buscar um livro pelo id")
    void getByIdTest() {
        // cenário
        Long id = 1L;
        Book savedBook = createValidBook();
        savedBook.setId(id);

        Mockito.when(repository.findById(Mockito.anyLong())).thenReturn(Optional.of(savedBook));

        // execução
        Optional<Book> foundBook = service.getById(id);

        //verificação
        assertThat(foundBook).isPresent();
        assertThat(foundBook.get().getId()).isEqualTo(id);
        assertThat(foundBook.get().getAuthor()).isEqualTo(savedBook.getAuthor());
        assertThat(foundBook.get().getIsbn()).isEqualTo(savedBook.getIsbn());
        assertThat(foundBook.get().getTitle()).isEqualTo(savedBook.getTitle());
    }

    @Test
    @DisplayName("Deve retornar vazio ao obter um livro por um id que não existe")
    void getByInexistenceIdTest() {
        // cenário
        Long id = 1L;

        Mockito.when(repository.findById(Mockito.anyLong())).thenReturn(Optional.empty());

        // execução
        Optional<Book> foundBook = service.getById(id);

        //verificação
        assertThat(foundBook).isNotPresent();
    }

    @Test
    @DisplayName("Deve deletar um livro")
    void deleteTest() {
        // cenário
        Long id = 1L;
        Book savedBook = createValidBook();
        savedBook.setId(id);

        // execução
        assertDoesNotThrow(() -> service.delete(savedBook));

        // verificação
        Mockito.verify(repository, Mockito.times(1)).delete(savedBook);
    }

    @Test
    @DisplayName("Deve lançar erro se o id do livro for nulo")
    void deleteInexistenceBookTest() {
        // cenário
        Book book = new Book();

        // execução
        Throwable exception = Assertions.catchThrowable(() -> service.delete(book));

        // verificação
        assertThat(exception)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Book id cant be null.");

        Mockito.verify(repository, Mockito.never()).delete(book);
    }

    @Test
    @DisplayName("Deve atualizar os dados de um livro")
    void updateTest() {
        // cenário
        Long id = 1L;
        Book updatingbook = createValidBook();
        updatingbook.setId(id);

        Book updatedBook = createValidBook();
        updatedBook.setId(id);

        Mockito.when(repository.save(updatingbook)).thenReturn(updatedBook);

        // execução
        Book book = service.update(updatingbook);

        // verificação
        assertThat(book.getId()).isEqualTo(updatedBook.getId());
        assertThat(book.getAuthor()).isEqualTo(updatedBook.getAuthor());
        assertThat(book.getIsbn()).isEqualTo(updatedBook.getIsbn());
        assertThat(book.getTitle()).isEqualTo(updatedBook.getTitle());
        Mockito.verify(repository, Mockito.times(1)).save(updatingbook);
    }

    @Test
    @DisplayName("Deve lançar um erro quando atualizar um livro com id nulo")
    void updateInexistenceIdBookTest() {
        // cenário
        Book book = createValidBook();

        // execução
        Throwable exception = Assertions.catchThrowable(() -> service.update(book));

        // verificação
        assertThat(exception)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Book id cant be null.");

        Mockito.verify(repository, Mockito.never()).save(book);
    }
}
