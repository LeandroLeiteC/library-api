package com.leandro.model.entity;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Long id;

    @Column
    private String customer;

    @Column(name = "customer_email")
    private String customerEmail;

    @ManyToOne
    @JoinColumn(name = "id_book")
    private Book book;

    @Column(updatable = true)
    @CreationTimestamp
    private LocalDate loanDate;

    @Column(columnDefinition = "boolean default false", insertable = false, updatable = true)
    private Boolean returned;

}
