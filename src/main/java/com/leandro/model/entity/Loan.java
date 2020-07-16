package com.leandro.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Long id;

    @Column
    private String customer;

    @ManyToOne
    @JoinColumn(name = "id_book")
    private Book book;

    @Column
    private LocalDate loanDate;

    @Column
    private Boolean returned = false;

}
