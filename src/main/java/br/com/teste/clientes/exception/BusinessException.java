package br.com.teste.clientes.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

public class BusinessException extends RuntimeException {

    public BusinessException(String errorMessage) {
        super(errorMessage);
    }

    public BusinessException(String errorMessage, Throwable throwable) {
        super(errorMessage, throwable);
    }
}

