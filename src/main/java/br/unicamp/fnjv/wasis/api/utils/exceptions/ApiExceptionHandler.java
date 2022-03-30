package br.unicamp.fnjv.wasis.api.utils.exceptions;

import br.unicamp.fnjv.wasis.api.utils.api.ApiResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.io.IOException;

@ControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity handleException(Exception e) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR, "Ocorreu um erro na aplicação. Tente novamente.", e.getMessage()));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity handleRuntimeException(RuntimeException e) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR, "Ocorreu um erro na aplicação. Tente novamente.", e.getMessage()));
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity handleIOException(IOException e) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR, "Ocorreu um erro na aplicação. Tente novamente.", e.getMessage()));
    }

    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity handleHttpClientErrorException(HttpClientErrorException e) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new ApiResponseEntity<>(HttpStatus.UNAUTHORIZED, "Falha na autenticação.", e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponseEntity<>(HttpStatus.BAD_REQUEST, "Erro nos parâmetros fornecidos para a requisição solicitada.", e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponseEntity<>(HttpStatus.BAD_REQUEST, "Erro nos parâmetros fornecidos para a requisição solicitada.", e.getMessage()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponseEntity<>(HttpStatus.BAD_REQUEST, "Erro nos parâmetros fornecidos para a requisição solicitada.", e.getMessage()));
    }

    @ExceptionHandler(GeneralException.class)
    public ResponseEntity handleGeneralException(GeneralException e) {
        return ResponseEntity
                .status(e.getHttpStatus())
                .body(new ApiResponseEntity<>(e.getHttpStatus(), e.getMessage(), e.getErrors()));
    }

}