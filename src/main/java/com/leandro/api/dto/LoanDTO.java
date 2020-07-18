package com.leandro.api.dto;

import lombok.*;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanDTO {

    private Long id;

    private BookDTO book;

    @NotEmpty
    private String customer;

    @NotEmpty
    @Email
    private String customerEmail;
}
