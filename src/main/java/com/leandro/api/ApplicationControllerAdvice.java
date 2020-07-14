package com.leandro.api;

import com.leandro.api.exceptions.ApiErrors;
import com.leandro.api.exceptions.BussinesException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.NoSuchElementException;

@RestControllerAdvice
public class ApplicationControllerAdvice {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrors handleValidationException(MethodArgumentNotValidException exception) {
        BindingResult bindingResult = exception.getBindingResult();
        return new ApiErrors(bindingResult);
    }

    @ExceptionHandler(BussinesException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrors handleBussinesException(BussinesException exception) {
        return new ApiErrors(exception);
    }

    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiErrors handleNotFoundException(NoSuchElementException exception) {
        return new ApiErrors(exception);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiErrors> handleResponseStatusException(ResponseStatusException exception) {
        return new ResponseEntity<>(new ApiErrors(exception), exception.getStatus());
    }
}
