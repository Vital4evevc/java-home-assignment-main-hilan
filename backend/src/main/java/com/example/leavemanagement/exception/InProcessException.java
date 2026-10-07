package com.example.leavemanagement.exception;

import org.springframework.http.HttpStatus;

public class InProcessException extends CustomException {

    public InProcessException() {
        super("Not enough vacation balance");
        status = HttpStatus.BAD_REQUEST;
    }
}
