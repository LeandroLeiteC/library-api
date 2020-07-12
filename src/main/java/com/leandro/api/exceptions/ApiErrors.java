package com.leandro.api.exceptions;

import lombok.Getter;
import org.springframework.validation.BindingResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

@Getter
public class ApiErrors {

    private List<String> errors;

    public ApiErrors(BindingResult bindingResult){
        this.errors = new ArrayList<>();
        bindingResult.getAllErrors().forEach( error -> this.errors.add(error.getDefaultMessage()));
    }

    public ApiErrors(BussinesException exception){
        this.errors = Arrays.asList(exception.getMessage());
    }

    public ApiErrors(NoSuchElementException exception){
        this.errors = Arrays.asList(exception.getMessage());
    }
}
