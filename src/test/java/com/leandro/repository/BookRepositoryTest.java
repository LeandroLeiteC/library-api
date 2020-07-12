package com.leandro.repository;

import com.leandro.model.entity.Book;
import com.leandro.model.repository.BookRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@DataJpaTest
class BookRepositoryTest {

    @Autowired
    TestEntityManager entityManager;

    @Autowired
    BookRepository repository;

    private Book createNewBook(String isbn) {
        return Book.builder().author("Autor").title("Titulo").isbn(isbn).build();
    }

    @Test
    @DisplayName("Deve retornar verdadeiro quando existir um livro na base com o isbn informado")
    void returnTrueWhenIsbnExists() {
        // cenário
        String isbn = "123";
        Book book = createNewBook(isbn);
        entityManager.persist(book);

        // execução
        boolean exists = repository.existsByIsbn(isbn);

        // verificação
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Deve retornar falso quando não existir um livro na base com o isbn informado")
    void returnFalseWhenIsbnDoesntExists() {
        // cenário
        String isbn = "123";

        // execução
        boolean exists = repository.existsByIsbn(isbn);

        // verificação
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Deve obter um livro pelo id")
    void findByIdTest() {
        // cenário
        Book book = createNewBook("123");
        entityManager.persist(book);

        // execução
        Optional<Book> foundBook = repository.findById(book.getId());

        // verificação
        assertThat(foundBook).isPresent();
        assertThat(foundBook.get().getId()).isEqualTo(book.getId());
        assertThat(foundBook.get().getTitle()).isEqualTo(book.getTitle());
        assertThat(foundBook.get().getIsbn()).isEqualTo(book.getIsbn());
        assertThat(foundBook.get().getAuthor()).isEqualTo(book.getAuthor());

    }

    @Test
    @DisplayName("Deve salvar um livro")
    void saveBookTest() {
        // cenário
        Book book = createNewBook("123");

        // execução
        Book savedBook = repository.save(book);

        // verificação
        assertThat(savedBook.getId()).isNotNull();
    }

    @Test
    @DisplayName("Deve deletar um livro")
    void deleteBookTest() {
        // cenário
        Book book = createNewBook("123");
        entityManager.persist(book);

        Book foundBook = entityManager.find(Book.class, book.getId());

        // execução
        repository.delete(foundBook);
        Book deletedBook = entityManager.find(Book.class, book.getId());

        // verificação
        assertThat(deletedBook).isNull();
    }

}
