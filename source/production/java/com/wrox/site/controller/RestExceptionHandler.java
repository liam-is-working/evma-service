package com.wrox.site.controller;

import com.wrox.config.annotation.RestEndpointAdvice;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@RestEndpointAdvice
public class RestExceptionHandler {
    @ExceptionHandler({javax.validation.ConstraintViolationException.class})
    public ResponseEntity<ErrorResponse> handle(ConstraintViolationException e)
    {
        ErrorResponse errors = new ErrorResponse();
        for(ConstraintViolation violation : e.getConstraintViolations())
        {
            ErrorItem error = new ErrorItem();
            error.setCode(violation.getMessageTemplate());
            error.setMessage(violation.getMessage());
            errors.addError(error);
        }
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({org.springframework.web.bind.MethodArgumentNotValidException.class})
    public ResponseEntity<ErrorResponse> handle(MethodArgumentNotValidException e)
    {
        ErrorResponse errors = new ErrorResponse();
        for(String errorField : e.getBindingResult().getSuppressedFields())
        {
            ErrorItem error = new ErrorItem();
            error.setMessage(errorField);
            errors.addError(error);
        }
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({DataIntegrityViolationException.class})
    public ResponseEntity<ErrorItem> handle(DataIntegrityViolationException e){
        ErrorItem item = new ErrorItem();
        item.setMessage(e.getMessage());
        //item.setCode(e.get);
        return new ResponseEntity<>(item, HttpStatus.BAD_REQUEST);
    }

    public static class ErrorItem
    {
        private String code;
        private String message;
        public String getCode() {return code;}
        public void setCode(String code) { this.code = code ;}
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    public static class ErrorResponse
    {
        private List<ErrorItem> errors = new ArrayList<>();

        public List<ErrorItem> getErrors() {
            return errors;
        }

        public void setErrors(List<ErrorItem> errors) {
            this.errors = errors;
        }

        public void addError(ErrorItem error) { errors.add(error); }
    }
}

