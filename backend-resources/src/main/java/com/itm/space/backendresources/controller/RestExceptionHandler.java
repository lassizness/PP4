package com.itm.space.backendresources.controller;

import com.itm.space.backendresources.exception.BackendResourcesException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(BackendResourcesException.class) //Это объявление метода handleException, который принимает объект BackendResourcesException в качестве параметра и возвращает объект ResponseEntity<String>. Метод будет использоваться для обработки исключения BackendResourcesException.
    public ResponseEntity<String> handleException(BackendResourcesException backendResourcesException) { //Возвращается новый объект ResponseEntity с переданным сообщением и статусом HTTP из исключения BackendResourcesException.
        return new ResponseEntity<>(backendResourcesException.getMessage(), backendResourcesException.getHttpStatus());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST) //указывает, что метод должен возвращать статус HTTP "BAD_REQUEST" (400) в случае возникновения исключения.
    @ExceptionHandler(MethodArgumentNotValidException.class) //Этот метод обрабатывает исключение типа MethodArgumentNotValidException.
    public Map<String, String> handleInvalidArgument(MethodArgumentNotValidException ex) { //Это объявление метода handleInvalidArgument, который принимает объект MethodArgumentNotValidException в качестве параметра и возвращает объект Map<String, String>. Метод будет использоваться для обработки исключения MethodArgumentNotValidException.
        Map<String, String> errorMap = new HashMap<>(); //Создается новый объект HashMap, который будет содержать ошибки валидации.
        ex.getBindingResult().getFieldErrors() // Из объекта MethodArgumentNotValidException получаются ошибки валидации полей и добавляются в errorMap с ключом, равным имени поля, и значением, равным сообщению об ошибке.
                .forEach(error -> errorMap.put(error.getField(), error.getDefaultMessage()));
        return errorMap;
    }

}
