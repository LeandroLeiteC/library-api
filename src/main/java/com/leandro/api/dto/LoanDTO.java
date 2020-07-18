package com.leandro.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoanDTO {

    private Long id;

    private BookDTO book;

    @NotEmpty
    private String customer;

    @NotEmpty
    @Email
    private String customerEmail;
}
