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

    @ExceptionHandler(BackendResourcesException.class) //��� ���������� ������ handleException, ������� ��������� ������ BackendResourcesException � �������� ��������� � ���������� ������ ResponseEntity<String>. ����� ����� �������������� ��� ��������� ���������� BackendResourcesException.
    public ResponseEntity<String> handleException(BackendResourcesException backendResourcesException) { //������������ ����� ������ ResponseEntity � ���������� ���������� � �������� HTTP �� ���������� BackendResourcesException.
        return new ResponseEntity<>(backendResourcesException.getMessage(), backendResourcesException.getHttpStatus());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST) //���������, ��� ����� ������ ���������� ������ HTTP "BAD_REQUEST" (400) � ������ ������������� ����������.
    @ExceptionHandler(MethodArgumentNotValidException.class) //���� ����� ������������ ���������� ���� MethodArgumentNotValidException.
    public Map<String, String> handleInvalidArgument(MethodArgumentNotValidException ex) { //��� ���������� ������ handleInvalidArgument, ������� ��������� ������ MethodArgumentNotValidException � �������� ��������� � ���������� ������ Map<String, String>. ����� ����� �������������� ��� ��������� ���������� MethodArgumentNotValidException.
        Map<String, String> errorMap = new HashMap<>(); //��������� ����� ������ HashMap, ������� ����� ��������� ������ ���������.
        ex.getBindingResult().getFieldErrors() // �� ������� MethodArgumentNotValidException ���������� ������ ��������� ����� � ����������� � errorMap � ������, ������ ����� ����, � ���������, ������ ��������� �� ������.
                .forEach(error -> errorMap.put(error.getField(), error.getDefaultMessage()));
        return errorMap;
    }

}
