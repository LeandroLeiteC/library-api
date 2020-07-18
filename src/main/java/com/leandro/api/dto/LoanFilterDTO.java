package com.leandro.api.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanFilterDTO {

    private String customer;
    private String isbn;
}
